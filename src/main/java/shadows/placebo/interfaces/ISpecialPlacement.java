package shadows.placebo.interfaces;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISpecialPlacement {

	default public boolean placeStateAt(IBlockState state, World world, BlockPos pos) {
		return world.setBlockState(pos, state);
	}

}
