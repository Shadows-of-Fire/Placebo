package shadows.placebo.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

/**
 * This class is likely not functional.
 * @author FatherToast
 *
 */
public class EffectHelper {

	public static void dye(ItemStack stack, int color) {
		NBTTagCompound tag = PlaceboUtil.getStackNBT(stack);

		if (!tag.hasKey("display")) {
			tag.setTag("display", new NBTTagCompound());
		}

		tag.getCompoundTag("display").setInteger("color", color);
	}

	/**
	 * Adds a custom attribute modifier to the item stack.
	 */
	public static void addModifier(ItemStack stack, String attribute, double value, int operation) {
		NBTTagCompound tag = PlaceboUtil.getStackNBT(stack);

		if (!tag.hasKey("AttributeModifiers")) {
			tag.setTag("AttributeModifiers", new NBTTagList());
		}

		NBTTagCompound attr = new NBTTagCompound();
		attr.setString("AttributeName", attribute);
		attr.setString("Name", "MobProperties|" + Integer.toString(ThreadLocalRandom.current().nextInt(), Character.MAX_RADIX));
		attr.setDouble("Amount", value);
		attr.setInteger("Operation", operation);
		UUID id = UUID.randomUUID();
		attr.setLong("UUIDMost", id.getMostSignificantBits());
		attr.setLong("UUIDLeast", id.getLeastSignificantBits());
		tag.getTagList("AttributeModifiers", tag.getId()).appendTag(tag);
	}
}