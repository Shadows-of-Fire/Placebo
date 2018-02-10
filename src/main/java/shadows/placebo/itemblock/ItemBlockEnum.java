package shadows.placebo.itemblock;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import shadows.placebo.block.IEnumBlock;
import shadows.placebo.interfaces.IPropertyEnum;

public class ItemBlockEnum<E extends Enum<E> & IPropertyEnum> extends ItemBlockBase {

	protected final IEnumBlock<E> enumBlock;

	public ItemBlockEnum(IEnumBlock<E> enumBlock) {
		super((Block) enumBlock);
		this.enumBlock = enumBlock;
		setHasSubtypes(true);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		Block block = world.getBlockState(pos).getBlock();
		ItemStack stack = player.getHeldItem(hand);

		if (!block.isReplaceable(world, pos)) pos = pos.offset(facing);

		if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack) && world.mayPlace(this.block, pos, false, facing, player)) {
			IBlockState state = this.block.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, stack.getMetadata(), player, hand);

			if (enumBlock.placeStateAt(state, world, pos)) {
				state = world.getBlockState(pos);
				SoundType soundtype = state.getBlock().getSoundType(state, world, pos, player);
				world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, 1, 1);
				stack.shrink(1);
			}
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.FAIL;
	}

	@Override
	public int getMetadata(int damage) {
		return MathHelper.clamp(damage, 0, enumBlock.getTypes().size() - 1);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (stack.getMetadata() >= enumBlock.getTypes().size()) return "invalid";
		return getUnlocalizedName() + "." + enumBlock.getTypes().get(stack.getMetadata()).getName();
	}
}
