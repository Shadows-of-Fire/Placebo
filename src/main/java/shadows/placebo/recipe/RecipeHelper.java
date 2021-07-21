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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.IForgeRegistryEntry;
import shadows.placebo.Placebo;
import shadows.placebo.util.RunnableReloader;

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
		addRecipe(new ShapelessRecipe(this.name(out), this.modid, out, this.createInput(false, inputs)));
	}

	public void addShaped(Object output, int width, int height, Object... input) {
		addRecipe(this.genShaped(makeStack(output), width, height, input));
	}

	public ShapedRecipe genShaped(ItemStack output, int l, int w, Object... input) {
		if (l * w != input.length) throw new UnsupportedOperationException("Attempted to add invalid shaped recipe.  Complain to the author of " + this.modid);
		return new ShapedRecipe(this.name(output), this.modid, l, w, this.createInput(true, input), output);
	}

	@SuppressWarnings("unchecked")
	public NonNullList<Ingredient> createInput(boolean allowEmpty, Object... input) {
		NonNullList<Ingredient> inputL = NonNullList.create();
		for (int i = 0; i < input.length; i++) {
			Object k = input[i];
			if (k instanceof String) inputL.add(i, new TagIngredient((String) k));
			else if (k instanceof ItemStack && !((ItemStack) k).isEmpty()) inputL.add(i, CachedIngredient.create((ItemStack) k));
			else if (k instanceof IForgeRegistryEntry) inputL.add(i, CachedIngredient.create(makeStack(k)));
			else if (k instanceof Ingredient) inputL.add(i, (Ingredient) k);
			else if (allowEmpty) inputL.add(i, Ingredient.EMPTY);
			else throw new UnsupportedOperationException("Attempted to add invalid recipe.  Complain to the author of " + this.modid + ". (Input " + k + " not allowed.)");
		}
		return inputL;
	}

	public void addSimpleShapeless(Object output, Object input, int numInputs) {
		this.addShapeless(output, NonNullList.withSize(numInputs, makeStack(input)).toArray(new Object[0]));
	}

	private ResourceLocation name(ItemStack out) {
		String name = out.getItem().getRegistryName().getPath();
		while (this.names.contains(name)) {
			name += "_";
		}
		this.names.add(name);
		return new ResourceLocation(this.modid, name);
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

	static void addRecipes(RecipeManager mgr) {
		recipes.forEach(r -> {
			Map<ResourceLocation, IRecipe<?>> map = mgr.recipes.computeIfAbsent(r.getType(), t -> new HashMap<>());
			IRecipe<?> old = map.get(r.getId());
			if (old == null) {
				r.getIngredients().stream().filter(i -> i instanceof TagIngredient).map(i -> (TagIngredient) i).forEach(TagIngredient::redefine);
				map.put(r.getId(), r);
			}
		});
		Placebo.LOGGER.info("Registered {} additional recipes.", recipes.size());
	}

	public static void mutableManager(RecipeManager mgr) {
		mgr.recipes = new HashMap<>(mgr.recipes);
		for (IRecipeType<?> type : mgr.recipes.keySet()) {
			mgr.recipes.put(type, new HashMap<>(mgr.recipes.get(type)));
		}
	}

	public static class CachedIngredient extends Ingredient {

		private static Int2ObjectMap<CachedIngredient> ingredients = new Int2ObjectOpenHashMap<>();

		private CachedIngredient(ItemStack... matches) {
			super(Arrays.stream(matches).map(SingleItemList::new));
			if (matches.length == 1) ingredients.put(RecipeItemHelper.getStackingIndex(matches[0]), this);
		}

		public static CachedIngredient create(ItemStack... matches) {
			synchronized (ingredients) {
				if (matches.length == 1) {
					CachedIngredient coi = ingredients.get(RecipeItemHelper.getStackingIndex(matches[0]));
					return coi != null ? coi : new CachedIngredient(matches);
				} else return new CachedIngredient(matches);
			}
		}

		@Override
		public IIngredientSerializer<? extends Ingredient> getSerializer() {
			return VanillaIngredientSerializer.INSTANCE;
		}

	}

	/**
	 * ASM Hook: Called from {@link #DataPackRegistries()}<br>
	 * Called right before {@link ForgeEventFactory#onResourceReload()}
	 * @param mgr The Recipe Manager, accessed from the DPR's constructor.
	 * @param rel The resource reload manager from the same location.
	 */
	public static void reload(RecipeManager mgr, IReloadableResourceManager rel) {
		rel.registerReloadListener(RunnableReloader.of(() -> {
			mutableManager(mgr);
			addRecipes(mgr);
		}));
	}

}
