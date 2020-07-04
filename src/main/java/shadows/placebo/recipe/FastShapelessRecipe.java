package shadows.placebo.recipe;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import shadows.placebo.util.ReflectionHelper;

public class FastShapelessRecipe extends ShapelessRecipe {

	static int packEmpty = RecipeItemHelper.pack(ItemStack.EMPTY);

	final boolean isSimple;

	public FastShapelessRecipe(ResourceLocation id, String group, ItemStack output, NonNullList<Ingredient> ingredients) {
		super(id, group, output, ingredients);
		isSimple = ReflectionHelper.getPrivateValue(ShapelessRecipe.class, this, "isSimple");
		if (isSimple) for (Ingredient i : ingredients)
			i.getValidItemStacksPacked();
	}

	@Override
	public boolean matches(CraftingInventory inv, World world) {
		if (!isSimple) return super.matches(inv, world);
		IntList list = new IntArrayList();

		int is;
		int k = inv.getSizeInventory();

		for (int ix = 0; ix < k; ix++)
			if ((is = RecipeItemHelper.pack(inv.getStackInSlot(ix))) != packEmpty) list.add(is);

		if (list.size() != recipeItems.size()) return false;
		for (Ingredient i : recipeItems) {
			for (int ix = 0; ix < list.size(); ix++) {
				if (i.getValidItemStacksPacked().contains(list.getInt(ix))) {
					list.removeInt(ix);
					break;
				}
			}
		}
		return list.isEmpty();
	}

}
