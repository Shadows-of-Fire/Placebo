package shadows.placebo.util;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class FastShapelessRecipe extends ShapelessRecipes {

	static int packEmpty = RecipeItemHelper.pack(ItemStack.EMPTY);

	boolean isSimple = ReflectionHelper.getPrivateValue(ShapelessRecipes.class, this, "isSimple");

	public FastShapelessRecipe(String group, ItemStack output, NonNullList<Ingredient> ingredients) {
		super(group, output, ingredients);
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		if (!isSimple) return super.matches(inv, world);
		IntList list = new IntArrayList();

		ItemStack is;
		int k = inv.getSizeInventory();

		for (int ix = 0; ix < k; ix++)
			if (!(is = inv.getStackInSlot(ix)).isEmpty()) list.add(RecipeItemHelper.pack(is));

		if (list.size() != recipeItems.size()) return false;
		for (Ingredient i : recipeItems) {
			for (int ix = 0; ix < list.size(); ix++) {
				if (i.getValidItemStacksPacked().contains(list.getInt(ix))) {
					list.remove(ix);
					break;
				}
			}
		}
		return list.isEmpty();
	}

}
