package dev.shadowsoffire.placebo.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Convenience interface for allowing {@linkplain BlockEntity block entities} to hold their own tick methods.
 * <p>
 * Static tick methods were a mistake™
 */
public interface TickingBlockEntity {

    public default void serverTick(Level level, BlockPos pos, BlockState state) {}

    public default void clientTick(Level level, BlockPos pos, BlockState state) {}
}
