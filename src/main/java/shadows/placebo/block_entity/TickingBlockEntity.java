package shadows.placebo.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface TickingBlockEntity {

	public default void serverTick(Level pLevel, BlockPos pPos, BlockState pState) {

	}

	public default void clientTick(Level pLevel, BlockPos pPos, BlockState pState) {

	}
}
