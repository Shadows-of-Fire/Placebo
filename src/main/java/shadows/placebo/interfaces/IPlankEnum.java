package shadows.placebo.interfaces;

import net.minecraft.item.ItemStack;

public interface IPlankEnum extends IPropertyEnum {

	public ItemStack genLogStack();

	public boolean isNether(); //TODO remove in 1.13;

}
