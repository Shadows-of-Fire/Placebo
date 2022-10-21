package shadows.placebo.recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import shadows.placebo.Placebo;
import shadows.placebo.util.RunnableReloader;

/**
 * This class is support for adding code recipes.<p>
 *
 * To add recipes, you register a provider which will be invoked during resource reload.
 * @see {@link RecipeHelper#registerProvider(Consumer)}.
 */
public final class RecipeHelper {

	private static final Multimap<String, Consumer<RecipeFactory>> PROVIDERS = HashMultimap.create();

	protected String modid;

	public RecipeHelper(String modid) {
		this.modid = modid;
	}

	/**
	 * Add a recipe provider, which will be invoked during registration time to instantiate recipe objects.
	 * @param provider The recipe provider you are adding.
	 * @see {@link RecipeFactory}
	 */
	public void registerProvider(Consumer<RecipeFactory> provider) {
		synchronized (PROVIDERS) {
			if (provider == null) {
				Placebo.LOGGER.error("Mod {} has attempted to add a null recipe provider.", this.modid);
				Thread.dumpStack();
			}
			PROVIDERS.put(modid, provider);
		}
	}

	/**
	 * Creates an ItemStack out of an appropriate stack-like object.<br>
	 * An {@link ItemLike} returns a new itemstack with a size of 1.<br>
	 * An {@link ItemStack} will a itself.
	 * @param thing An {@link ItemLike} or {@link ItemStack}
	 * @return An ItemStack representing <code>thing</code>.
	 * @throws IllegalArgumentException if <code>thing</code> is not a valid type.
	 */
	public static ItemStack makeStack(Object thing) {
		if (thing instanceof ItemStack stack) return stack;
		if (thing instanceof ItemLike il) return new ItemStack(il);
		if (thing instanceof RegistryObject<?> ro) return new ItemStack((ItemLike) ro.get());
		throw new IllegalArgumentException("Attempted to create an ItemStack from something that cannot be converted: " + thing);
	}

	/**
	 * Creates an ingredient list from an array of ingredient-like objects.<br>
	 * A {@link String} will become a tag ingredient.<br>
	 * An {@link ItemStack} will become a stack ingredient.<br>
	 * An {@link ItemLike} will become an itemstack and become a stack ingredient.<br>
	 * An {@link Ingredient} will be accepted directly without transformation.<br>
	 * All other objects will be treated as {@link Ingredient#EMPTY} if <code>allowEmpty</code> is true.
	 * @param modid The creator's modid for error logging.
	 * @param allowEmpty If empty inputs are permitted in the output list.
	 * @param inputArr An array of potential input objects that are in-order.
	 * @return A list of ingredients for a recipe.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static NonNullList<Ingredient> createInput(String modid, boolean allowEmpty, Object... inputArr) {
		NonNullList<Ingredient> inputL = NonNullList.create();
		for (int i = 0; i < inputArr.length; i++) {
			Object input = inputArr[i];
			if (input instanceof TagKey tag) inputL.add(i, Ingredient.of(tag));
			else if (input instanceof String str) inputL.add(i, Ingredient.of(ItemTags.create(new ResourceLocation(str))));
			else if (input instanceof ItemStack stack && !stack.isEmpty()) inputL.add(i, Ingredient.of(stack));
			else if (input instanceof ItemLike || input instanceof RegistryObject) inputL.add(i, Ingredient.of(makeStack(input)));
			else if (input instanceof Ingredient ing) inputL.add(i, ing);
			else if (allowEmpty) inputL.add(i, Ingredient.EMPTY);
			else throw new UnsupportedOperationException("Attempted to add invalid recipe.  Complain to the author of " + modid + ". (Input " + input + " not allowed.)");
		}
		return inputL;
	}

	/**
	 * Generates the reload listener which handles adding all RecipeHelper-based recipes.
	 */
	@ApiStatus.Internal
	public static PreparableReloadListener getReloader(RecipeManager mgr) {
		return RunnableReloader.of(() -> {
			mutableManager(mgr);
			addRecipes(mgr);
		});
	}

	private static void addRecipes(RecipeManager mgr) {
		PROVIDERS.forEach((modid, provider) -> {
			RecipeFactory factory = new RecipeFactory(modid);
			provider.accept(factory);
			factory.registerAll(mgr);
		});
		Placebo.LOGGER.info("Registered {} additional recipes.", RecipeFactory.totalRecipes);
		RecipeFactory.resetCaches();
	}

	private static void mutableManager(RecipeManager mgr) {
		mgr.byName = new HashMap<>(mgr.byName);
		mgr.recipes = new HashMap<>(mgr.recipes);
		for (RecipeType<?> type : mgr.recipes.keySet()) {
			mgr.recipes.put(type, new HashMap<>(mgr.recipes.get(type)));
		}
	}

	public static final class RecipeFactory {

		private final String modid;
		private final List<Recipe<?>> recipes = new ArrayList<>();
		private static final Multimap<String, String> MODID_TO_NAMES = HashMultimap.create();
		private static int totalRecipes = 0;

		private RecipeFactory(String modid) {
			this.modid = modid;
		}

		/**
		 * Directly add a recipe for registration.
		 * @param rec The recipe to add.
		 */
		public void addRecipe(Recipe<?> rec) {
			if (rec == null || rec.getId() == null || rec.getSerializer() == null || ForgeRegistries.RECIPE_SERIALIZERS.getKey(rec.getSerializer()) == null) {
				Placebo.LOGGER.error("Attempted to add an invalid recipe {}.", rec);
				Thread.dumpStack();
			}
			recipes.add(rec);
		}

		/**
		 * Add a shapeless recipe with the provided input and output.
		 * @param output A stack-like output object.
		 * @param inputs An array of ingredient-like input objects.
		 * 
		 * @see RecipeHelper#makeStack(Object) for the definition of stack-like.
		 * @see RecipeHelper#createInput(String, boolean, Object...) for the definition of ingredient-like.
		 */
		public void addShapeless(Object output, Object... inputs) {
			ItemStack out = makeStack(output);
			addRecipe(new ShapelessRecipe(this.name(out), this.modid, out, createInput(modid, false, inputs)));
		}

		/**
		 * Add a shaped recipe with the provided input, output, and size.
		 * @param output A stack-like output object.
		 * @param width The grid width.
		 * @param height The grid height.
		 * @param inputs An array of ingredient-like input objects.
		 * 
		 * @see RecipeHelper#makeStack(Object) for the definition of stack-like.
		 * @see RecipeHelper#createInput(String, boolean, Object...) for the definition of ingredient-like.
		 * @throws UnsupportedOperationException if <code>width * height != input.length</code> (meaning not enough inputs).
		 */
		public void addShaped(Object output, int width, int height, Object... input) {
			addRecipe(this.genShaped(makeStack(output), width, height, input));
		}

		private ShapedRecipe genShaped(ItemStack output, int width, int height, Object... input) {
			if (width * height != input.length) throw new UnsupportedOperationException("Attempted to add invalid shaped recipe.  Complain to the author of " + this.modid);
			return new ShapedRecipe(this.name(output), this.modid, width, height, createInput(modid, true, input), output);
		}

		private ResourceLocation name(ItemStack out) {
			String name = ForgeRegistries.ITEMS.getKey(out.getItem()).getPath();
			while (MODID_TO_NAMES.get(this.modid).contains(name)) {
				name += "_";
			}
			MODID_TO_NAMES.put(modid, name);
			return new ResourceLocation(this.modid, name);
		}

		private void registerAll(RecipeManager mgr) {
			this.recipes.forEach(r -> {
				Map<ResourceLocation, Recipe<?>> map = mgr.recipes.computeIfAbsent(r.getType(), t -> new HashMap<>());
				Recipe<?> old = map.get(r.getId());
				if (old == null) {
					map.put(r.getId(), r);
					totalRecipes++;
				} else Placebo.LOGGER.debug("Skipping registration for code recipe {} as a json recipe already exists with that ID.", r.getId());
			});
		}

		private static void resetCaches() {
			MODID_TO_NAMES.clear();
			totalRecipes = 0;
		}
	}

}
