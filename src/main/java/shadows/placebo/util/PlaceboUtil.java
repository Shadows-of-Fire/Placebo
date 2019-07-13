package shadows.placebo.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

public class PlaceboUtil {

	/**
	 * Returns an ArrayList (non-fixed) with the provided elements.
	 */
	@SafeVarargs
	public static <T> List<T> asList(T... objs) {
		ArrayList<T> list = new ArrayList<>();
		for (T t : objs)
			list.add(t);
		return list;
	}

	public static CompoundNBT getStackNBT(ItemStack stack) {
		CompoundNBT tag = stack.getTag();
		if (tag == null) stack.setTag(tag = new CompoundNBT());
		return tag;
	}

}
