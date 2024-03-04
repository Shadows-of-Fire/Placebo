package dev.shadowsoffire.placebo.block_entity;

import dev.shadowsoffire.placebo.Placebo;
import net.minecraft.core.registries.BuiltInRegistries;
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
        if (type instanceof TickingBlockEntityType tickType) return tickType.getTicker(pLevel.isClientSide);
        Placebo.LOGGER.error("##############################");
        Placebo.LOGGER.error("A Block {} with BlockEntityType {} has subscribed as a TickingEntityBlock but is not using TickingBlockEntityType!", BuiltInRegistries.BLOCK.getKey(pState.getBlock()), BlockEntityType.getKey(type));
        Placebo.LOGGER.error("##############################");
        return null;
    }

}
