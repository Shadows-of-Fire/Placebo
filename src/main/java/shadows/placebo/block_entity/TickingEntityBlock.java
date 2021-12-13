package shadows.placebo.block_entity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public interface TickingEntityBlock extends EntityBlock {

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> type) {
		return ((TickingBlockEntityType) type).getTicker(pLevel.isClientSide);
	}

}
