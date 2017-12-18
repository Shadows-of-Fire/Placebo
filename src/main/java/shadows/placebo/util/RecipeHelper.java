package shadows.placebo.util;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RecipeHelper {

	private int j = 0;
	private final String modid;
	private final String modname;
	private final List<IRecipe> recipes;

	public RecipeHelper(String modid, String modname, List<IRecipe> recipes) {
		this.modid = modid;
		this.modname = modname;
		this.recipes = recipes;
	}

	/**
	 * This adds the recipe to the list of crafting recipes.  Since who cares about names, it adds it as recipeX, where X is the current recipe you are adding.
	 */
	public void addRecipe(int j, IRecipe rec) {
		if (rec.getRegistryName() == null) recipes.add(rec.setRegistryName(new ResourceLocation(modid, "recipe" + j)));
		else recipes.add(rec);
	}

	/**
	 * This adds the recipe to the list of crafting recipes.  Cares about names.
	 */
	public void addRecipe(String name, IRecipe rec) {
		if (rec.getRegistryName() == null) recipes.add(rec.setRegistryName(new ResourceLocation(modid, name)));
		else recipes.add(rec);
	}

	/**
	 * Adds a shapeless recipe with X output using an array of inputs. Use Strings for OreDictionary support. This array is not ordered.  Can take a List in place of inputs.
	 */
	public void addShapeless(ItemStack output, Object... inputs) {
		addRecipe(j++, new ShapelessRecipes(modid + ":" + j, output, createInput(inputs)));
	}

	public <T extends IForgeRegistryEntry<?>> void addShapeless(T output, Object... inputs) {
		addShapeless(makeStack(output), inputs);
	}

	/**
	 * Adds a shapeless recipe with X output using an array of inputs. Use Strings for OreDictionary support. This array is not ordered.  This has a custom group.
	 */
	public void addShapeless(String group, ItemStack output, Object... inputs) {
		addRecipe(j++, new ShapelessRecipes(modid + ":" + group, output, createInput(inputs)));
	}

	public <T extends IForgeRegistryEntry<?>> void addShapeless(String group, T output, Object... inputs) {
		addShapeless(group, makeStack(output), inputs);
	}

	/**
	 * Adds a shapeless recipe with X output on a crafting grid that is W x H, using an array of inputs.  Use null for nothing, use Strings for OreDictionary support, this array must have a length of width * height.
	 * This array is ordered, and items must follow from left to right, top to bottom of the crafting grid.
	 */
	public void addShaped(ItemStack output, int width, int height, Object... input) {
		addRecipe(j++, genShaped(output, width, height, input));
	}

	public <T extends IForgeRegistryEntry<?>> void addShaped(T output, int width, int height, Object... input) {
		addShaped(makeStack(output), width, height, input);
	}

	/**
	 * Adds a shapeless recipe with X output on a crafting grid that is W x H, using an array of inputs.  Use null for nothing, use Strings for OreDictionary support, this array must have a length of width * height.
	 * This array is ordered, and items must follow from left to right, top to bottom of the crafting grid. This has a custom group.
	 */
	public void addShaped(String group, ItemStack output, int width, int height, Object... input) {
		addRecipe(j++, genShaped(modid + ":" + group, output, width, height, input));
	}

	public <T extends IForgeRegistryEntry<?>> void addShaped(String group, T output, int width, int height, Object... input) {
		addShaped(group, makeStack(output), width, height, input);
	}

	/**
	 * Generates a shaped recipe with a specific width and height. The Object[] is the ingredients, in order from left to right, top to bottom.
	 */
	public ShapedRecipes genShaped(ItemStack output, int width, int height, Object... input) {
		if (input[0] instanceof List) input = ((List<?>) input[0]).toArray();
		if (width * height != input.length) throw new UnsupportedOperationException("Attempted to add invalid shaped recipe.  Complain to the author of " + modname);
		NonNullList<Ingredient> inputL = NonNullList.create();
		for (int i = 0; i < input.length; i++) {
			Object k = input[i];
			if (k instanceof String) inputL.add(i, new OreIngredient((String) k));
			else if (k instanceof ItemStack && !((ItemStack) k).isEmpty()) inputL.add(i, Ingredient.fromStacks((ItemStack) k));
			else if (k instanceof IForgeRegistryEntry) inputL.add(i, Ingredient.fromStacks(makeStack((IForgeRegistryEntry<?>) k)));
			else if (k instanceof Ingredient) inputL.add(i, (Ingredient) k);
			else inputL.add(i, Ingredient.EMPTY);

		}

		return new ShapedRecipes(modid + ":" + j, width, height, inputL, output);
	}

	/**
	 * Same thing as genShaped above, but uses a specific group.
	 */
	public ShapedRecipes genShaped(String group, ItemStack output, int l, int w, Object... input) {
		if (input[0] instanceof List) input = ((List<?>) input[0]).toArray();
		if (l * w != input.length) throw new UnsupportedOperationException("Attempted to add invalid shaped recipe.  Complain to the author of " + modname);
		NonNullList<Ingredient> inputL = NonNullList.create();
		for (int i = 0; i < input.length; i++) {
			Object k = input[i];
			if (k instanceof String) inputL.add(i, new OreIngredient((String) k));
			else if (k instanceof ItemStack && !((ItemStack) k).isEmpty()) inputL.add(i, Ingredient.fromStacks((ItemStack) k));
			else if (k instanceof IForgeRegistryEntry) inputL.add(i, Ingredient.fromStacks(makeStack((IForgeRegistryEntry<?>) k)));
			else if (k instanceof Ingredient) inputL.add(i, (Ingredient) k);
			else inputL.add(i, Ingredient.EMPTY);
		}

		return new ShapedRecipes(group, l, w, inputL, output);
	}

	/**
	 * Creates a list of ingredients based on an Object[].  Valid types are {@link String}, {@link ItemStack}, {@link Item}, and {@link Block}.
	 * Used for shapeless recipes.
	 */
	public NonNullList<Ingredient> createInput(Object... input) {
		if (input[0] instanceof List) input = ((List<?>) input[0]).toArray();
		else if (input[0] instanceof Object[]) input = (Object[]) input[0];
		NonNullList<Ingredient> inputL = NonNullList.create();
		for (int i = 0; i < input.length; i++) {
			Object k = input[i];
			if (k instanceof String) inputL.add(i, new OreIngredient((String) k));
			else if (k instanceof ItemStack && !((ItemStack) k).isEmpty()) inputL.add(i, Ingredient.fromStacks((ItemStack) k));
			else if (k instanceof IForgeRegistryEntry) inputL.add(i, Ingredient.fromStacks(makeStack((IForgeRegistryEntry<?>) k)));
			else if (k instanceof Ingredient) inputL.add(i, (Ingredient) k);
			else throw new UnsupportedOperationException("Attempted to add invalid shapeless recipe.  Complain to the author of " + modname);
		}
		return inputL;
	}

	/**
	 * Adds a shapeless recipe with one output and x inputs, all inputs are the same.
	 */
	public void addSimpleShapeless(ItemStack output, ItemStack input, int numInputs) {
		addShapeless(output, NonNullList.withSize(numInputs, input));
	}

	public <T extends IForgeRegistryEntry<?>> void addSimpleShapeless(T output, T input, int numInputs) {
		addSimpleShapeless(makeStack(output), makeStack(input), numInputs);
	}

	public <T extends IForgeRegistryEntry<?>> void addSimpleShapeless(T output, ItemStack input, int numInputs) {
		addSimpleShapeless(makeStack(output), input, numInputs);
	}

	public <T extends IForgeRegistryEntry<?>> void addSimpleShapeless(ItemStack output, T input, int numInputs) {
		addSimpleShapeless(output, makeStack(input), numInputs);
	}

	/**
	 * Helper method to make an {@link ItemStack} from a block or item.
	 */
	public static <T extends IForgeRegistryEntry<?>> ItemStack makeStack(T thing, int size, int meta) {
		if (thing instanceof Item) return new ItemStack((Item) thing, size, meta);
		return new ItemStack((Block) thing, size, meta);
	}

	public static <T extends IForgeRegistryEntry<?>> ItemStack makeStack(T thing, int size) {
		return makeStack(thing, size, 0);
	}

	public static <T extends IForgeRegistryEntry<?>> ItemStack makeStack(T thing) {
		return makeStack(thing, 1, 0);
	}

	/**
	 * Goes through all recipes and replaces anything that matches with just the provided itemstack and replaces it with another {@link Ingredient}.
	 * @param old The itemstack to replace.
	 * @param newThing The ingredient to replace the stack with.
	 */
	public static void replaceInAllRecipes(ItemStack old, Ingredient newThing) {
		for (IRecipe rec : ForgeRegistries.RECIPES) {
			if (rec instanceof IShapedRecipe) {
				NonNullList<Ingredient> list = NonNullList.create();
				for (Ingredient ing : rec.getIngredients()) {
					if (ing.getMatchingStacks().length == 1 && OreDictionary.itemMatches(ing.getMatchingStacks()[0], old, false)) {
						list.add(newThing);
					} else list.add(ing);
				}
				ResourceLocation regname = rec.getRegistryName();
				int width = ((IShapedRecipe) rec).getRecipeWidth();
				int height = ((IShapedRecipe) rec).getRecipeHeight();
				ForgeRegistries.RECIPES.register(new ShapedRecipes(rec.getGroup(), width, height, list, rec.getRecipeOutput()).setRegistryName(regname));
			} else {
				NonNullList<Ingredient> list = NonNullList.create();
				for (Ingredient ing : rec.getIngredients()) {
					if (ing.getMatchingStacks().length == 1 && OreDictionary.itemMatches(ing.getMatchingStacks()[0], old, false)) {
						list.add(newThing);
					} else list.add(ing);
				}
				ResourceLocation regname = rec.getRegistryName();
				ForgeRegistries.RECIPES.register(new ShapelessRecipes(rec.getGroup(), rec.getRecipeOutput(), list).setRegistryName(regname));
			}
		}
	}

	/**
	 * Adds a potion recipe
	 * @param input The ItemStack input, this goes in the potion slot.  Must have a max stack size of 1.
	 * @param inputPot The PotionType that will go on the input ItemStack.
	 * @param reagent The ItemStack reagent, this goes in the top slot.
	 * @param output The ItemStack output, this is what the input transforms into after brewing.
	 * @param ontputPot The PotionType that will go on the output ItemStack.
	 */
	public static void addPotionRecipe(ItemStack input, PotionType inputPot, ItemStack reagent, ItemStack output, PotionType ontputPot) {
		BrewingRecipeRegistry.addRecipe(PotionUtils.addPotionToItemStack(input, inputPot), reagent, PotionUtils.addPotionToItemStack(output, ontputPot));
	}

	public static <T extends IForgeRegistryEntry<?>> void addPotionRecipe(ItemStack input, PotionType inputPot, ItemStack reagent, T output, PotionType outputPot) {
		addPotionRecipe(input, inputPot, reagent, makeStack(output), outputPot);
	}

	public static <T extends IForgeRegistryEntry<?>> void addPotionRecipe(ItemStack input, PotionType inputPot, T reagent, T output, PotionType outputPot) {
		addPotionRecipe(input, inputPot, makeStack(reagent), makeStack(output), outputPot);
	}

	public static <T extends IForgeRegistryEntry<?>> void addPotionRecipe(T input, PotionType inputPot, ItemStack reagent, ItemStack output, PotionType outputPot) {
		addPotionRecipe(makeStack(input), inputPot, reagent, output, outputPot);
	}

	public static <T extends IForgeRegistryEntry<?>> void addPotionRecipe(T input, PotionType inputPot, T reagent, ItemStack output, PotionType outputPot) {
		addPotionRecipe(makeStack(input), inputPot, makeStack(reagent), output, outputPot);
	}

	public static <T extends IForgeRegistryEntry<?>> void addPotionRecipe(ItemStack input, PotionType inputPot, T reagent, ItemStack output, PotionType outputPot) {
		addPotionRecipe(input, inputPot, makeStack(reagent), output, outputPot);
	}

	public static <T extends IForgeRegistryEntry<?>> void addPotionRecipe(T input, PotionType inputPot, ItemStack reagent, T output, PotionType outputPot) {
		addPotionRecipe(makeStack(input), inputPot, reagent, makeStack(output), outputPot);
	}

	public static <T extends IForgeRegistryEntry<?>> void addPotionRecipe(PotionType inputPot, ItemStack reagent, PotionType outputPot) {
		addPotionRecipe(Items.POTIONITEM, inputPot, reagent, Items.POTIONITEM, outputPot);
	}

	public static <T extends IForgeRegistryEntry<?>> void addPotionRecipe(PotionType inputPot, T reagent, PotionType outputPot) {
		addPotionRecipe(Items.POTIONITEM, inputPot, makeStack(reagent), Items.POTIONITEM, outputPot);
	}

}