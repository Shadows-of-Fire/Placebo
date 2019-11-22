package shadows.placebo.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.registries.IForgeRegistryEntry;
import shadows.placebo.Placebo;

public class RecipeHelper {

	private static final List<IRecipe<?>> recipes = new ArrayList<>();

	protected String modid;
	protected Set<String> names = new HashSet<>();

	public RecipeHelper(String modid) {
		this.modid = modid;
	}

	public static void addRecipe(IRecipe<?> rec) {
		synchronized (recipes) {
			if (rec == null) {
				Placebo.LOGGER.error("Attempted to add null recipe, this is invalid behavior.");
				Thread.dumpStack();
			}
			recipes.add(rec);
		}
	}

	public void addShapeless(Object output, Object... inputs) {
		ItemStack out = makeStack(output);
		addRecipe(new FastShapelessRecipe(name(out), modid, out, createInput(false, inputs)));
	}

	public void addShaped(Object output, int width, int height, Object... input) {
		addRecipe(genShaped(makeStack(output), width, height, input));
	}

	public ShapedRecipe genShaped(ItemStack output, int l, int w, Object... input) {
		if (l * w != input.length) throw new UnsupportedOperationException("Attempted to add invalid shaped recipe.  Complain to the author of " + modid);
		return new ShapedRecipe(name(output), modid, l, w, createInput(true, input), output);
	}

	public NonNullList<Ingredient> createInput(boolean allowEmpty, Object... input) {
		NonNullList<Ingredient> inputL = NonNullList.create();
		for (int i = 0; i < input.length; i++) {
			Object k = input[i];
			if (k instanceof String) inputL.add(i, new TagIngredient(ItemTags.getCollection().get(new ResourceLocation((String) k))));
			else if (k instanceof ItemStack && !((ItemStack) k).isEmpty()) inputL.add(i, CachedIngredient.create((ItemStack) k));
			else if (k instanceof IForgeRegistryEntry) inputL.add(i, CachedIngredient.create(makeStack(k)));
			else if (k instanceof Ingredient) inputL.add(i, (Ingredient) k);
			else if (allowEmpty) inputL.add(i, Ingredient.EMPTY);
			else throw new UnsupportedOperationException("Attempted to add invalid shapeless recipe.  Complain to the author of " + modid);
		}
		return inputL;
	}

	public void addSimpleShapeless(Object output, Object input, int numInputs) {
		addShapeless(output, NonNullList.withSize(numInputs, makeStack(input)));
	}

	private ResourceLocation name(ItemStack out) {
		String name = out.getItem().getRegistryName().getPath();
		while (names.contains(name)) {
			name += "_";
		}
		names.add(name);
		return new ResourceLocation(modid, name);
	}

	public static ItemStack makeStack(Object thing, int size) {
		if (thing instanceof ItemStack) return (ItemStack) thing;
		if (thing instanceof Item) return new ItemStack((Item) thing, size);
		if (thing instanceof Block) return new ItemStack((Block) thing, size);
		throw new IllegalArgumentException("Attempted to create an ItemStack from something that cannot be converted: " + thing);
	}

	public static ItemStack makeStack(Object thing) {
		return makeStack(thing, 1);
	}

	@SubscribeEvent
	public static void serverStart(FMLServerAboutToStartEvent e) {
		SimpleReloadableResourceManager resMan = (SimpleReloadableResourceManager) e.getServer().getResourceManager();
		Reloader rel = new Reloader();
		for (int i = 0; i < resMan.reloadListeners.size(); i++) {
			if (resMan.reloadListeners.get(i) instanceof RecipeManager) {
				resMan.reloadListeners.add(i + 1, rel);
				break;
			}
		}
	}

	static void addRecipes(RecipeManager mgr) {
		recipes.forEach(r -> {
			Map<ResourceLocation, IRecipe<?>> map = mgr.recipes.computeIfAbsent(r.getType(), t -> new HashMap<>());
			IRecipe<?> old = map.get(r.getId());
			if (old == null) map.put(r.getId(), r);
		});
		Placebo.LOGGER.info("Registered {} additional recipes.", recipes.size());
	}

	public static void mutableManager(RecipeManager mgr) {
		mgr.recipes = new HashMap<>(mgr.recipes);
		for (IRecipeType<?> type : mgr.recipes.keySet()) {
			mgr.recipes.put(type, new HashMap<>(mgr.recipes.get(type)));
		}
	}

	public static void replaceShapeless(RecipeManager mgr) {
		Placebo.LOGGER.info("Beginning replacement of all shapeless recipes...");
		List<FastShapelessRecipe> fastRecipes = new ArrayList<>();
		for (IRecipe<?> r : mgr.getRecipes()) {
			if (r.getClass() == ShapelessRecipe.class) {
				fastRecipes.add(new FastShapelessRecipe(r.getId(), r.getGroup(), r.getRecipeOutput(), r.getIngredients()));
			}
		}
		for (FastShapelessRecipe r : fastRecipes)
			mgr.recipes.get(r.getType()).put(r.getId(), r);
		Placebo.LOGGER.info("Successfully replaced {} recipes with fast recipes.", fastRecipes.size());
	}

	public static class CachedIngredient extends Ingredient {

		private static Int2ObjectMap<CachedIngredient> ingredients = new Int2ObjectOpenHashMap<>();

		private CachedIngredient(ItemStack... matches) {
			super(Arrays.stream(matches).map(s -> new SingleItemList(s)));
			if (matches.length == 1) ingredients.put(RecipeItemHelper.pack(matches[0]), this);
		}

		public static CachedIngredient create(ItemStack... matches) {
			synchronized (ingredients) {
				if (matches.length == 1) {
					CachedIngredient coi = ingredients.get(RecipeItemHelper.pack(matches[0]));
					return coi != null ? coi : new CachedIngredient(matches);
				} else return new CachedIngredient(matches);
			}
		}

		@Override
		public IIngredientSerializer<? extends Ingredient> getSerializer() {
			return VanillaIngredientSerializer.INSTANCE;
		}

	}

	private static class Reloader extends ReloadListener<List<IRecipe<?>>> {

		@Override
		protected List<IRecipe<?>> prepare(IResourceManager p_212854_1_, IProfiler p_212854_2_) {
			return null;
		}

		@Override
		protected void apply(List<IRecipe<?>> recipes, IResourceManager manager, IProfiler profiler) {
			RecipeManager mgr = ServerLifecycleHooks.getCurrentServer().getRecipeManager();
			mutableManager(mgr);
			addRecipes(mgr);
			replaceShapeless(mgr);
		}
	}

}
