package shadows.placebo.recipe;

import java.util.Set;

import net.minecraft.world.item.ItemStack;

public class NBTIngredient extends net.minecraftforge.common.crafting.PartialNBTIngredient {

	public NBTIngredient(ItemStack stack) {
		super(Set.of(stack.getItem()), stack.getTag());
	}

}
