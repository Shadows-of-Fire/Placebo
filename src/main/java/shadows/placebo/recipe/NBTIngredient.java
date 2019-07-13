package shadows.placebo.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.common.crafting.IngredientNBT;

public class NBTIngredient extends IngredientNBT {

	public NBTIngredient(ItemStack stack) {
		super(stack);
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer() {
		return CraftingHelper.INGREDIENT_NBT;
	}

}
