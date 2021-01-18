package shadows.placebo.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.placebo.recipe.RecipeHelper;

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

	public static ItemStack[] toStackArray(Object... args) {
		ItemStack[] out = new ItemStack[args.length];
		for (int i = 0; i < args.length; i++)
			out[i] = RecipeHelper.makeStack(args[i]);
		return out;
	}

	@Deprecated
	public static void registerOverrideBlock(Block b, String modid) {
		Block old = ForgeRegistries.BLOCKS.getValue(b.getRegistryName());
		ForgeRegistries.BLOCKS.register(b);
		ForgeRegistries.ITEMS.register(new BlockItem(b, new Item.Properties().group(old.asItem().getGroup())) {
			@Override
			public String getCreatorModId(ItemStack itemStack) {
				return modid;
			}
		}.setRegistryName(b.getRegistryName()));
	}

	/**
	 * Replaces a block and item and provides the original states to the new block.
	 * States are updated such that the old state references are still valid.
	 */
	public static <B extends Block & IReplacementBlock> void registerOverride(B block, String modid) {
		Block old = ForgeRegistries.BLOCKS.getValue(block.getRegistryName());
		ForgeRegistries.BLOCKS.register(block);
		ForgeRegistries.ITEMS.register(new BlockItem(block, new Item.Properties().group(old.asItem().getGroup())) {
			@Override
			public String getCreatorModId(ItemStack itemStack) {
				return modid;
			}
		}.setRegistryName(block.getRegistryName()));
		overrideStates(old, block);
	}

	/**
	 * Updates the references for a replaced block such that original BlockState objects are still valid.
	 */
	public static <B extends Block & IReplacementBlock> void overrideStates(Block old, B block) {
		block.setStateContainer(old.getStateContainer());
		block._setDefaultState(old.getDefaultState());
		block.getStateContainer().getValidStates().forEach(b -> b.instance = block);
		block.getStateContainer().owner = block;
	}

	/**
	 * Returns a mutable version of the passed list.
	 * If the list is already mutable, the list is returned.
	 */
	public static <T> List<T> toMutable(List<T> list) {
		if (list instanceof ImmutableList) {
			list = new ArrayList<T>(list);
		}
		return list;
	}

}
