package shadows.placebo.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.placebo.recipe.RecipeHelper;

/**
 * Collection of misc util stuff.
 */
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

	public static CompoundTag getStackNBT(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null) stack.setTag(tag = new CompoundTag());
		return tag;
	}

	public static ItemStack[] toStackArray(Object... args) {
		ItemStack[] out = new ItemStack[args.length];
		for (int i = 0; i < args.length; i++)
			out[i] = RecipeHelper.makeStack(args[i]);
		return out;
	}

	public static void registerOverrideBlock(Block b, String modid) {
		Block old = ForgeRegistries.BLOCKS.getValue(b.getRegistryName());
		ForgeRegistries.BLOCKS.register(b);
		ForgeRegistries.ITEMS.register(new BlockItem(b, new Item.Properties().tab(old.asItem().getItemCategory())) {
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
		ForgeRegistries.ITEMS.register(new BlockItem(block, new Item.Properties().tab(old.asItem().getItemCategory())) {
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
		block.setStateContainer(old.getStateDefinition());
		block._setDefaultState(old.defaultBlockState());
		block.getStateDefinition().getPossibleStates().forEach(b -> b.owner = block);
		block.getStateDefinition().owner = block;
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

	/**
	 * Attempt to break a block as this player.
	 * @param player The player breaking the block.
	 * @param pos The location of the block to break.
	 * @return If a block was successfully broken.
	 */
	public static boolean tryHarvestBlock(ServerPlayer player, BlockPos pos) {
		return player.gameMode.destroyBlock(pos);
	}

}
