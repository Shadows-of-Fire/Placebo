package dev.shadowsoffire.placebo.block_entity;

import java.util.Set;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TickingBlockEntityType<T extends BlockEntity & TickingBlockEntity>extends BlockEntityType<T> {

    protected final boolean clientTick, serverTick;

    public TickingBlockEntityType(BlockEntitySupplier<? extends T> pFactory, Set<Block> pValidBlocks, boolean clientTick, boolean serverTick) {
        super(pFactory, pValidBlocks, null);
        this.clientTick = clientTick;
        this.serverTick = serverTick;
    }

    public boolean ticksOnClient() {
        return this.clientTick;
    }

    public boolean ticksOnServer() {
        return this.serverTick;
    }

    public BlockEntityTicker<T> getTicker(boolean client) {
        if (client && this.clientTick) return (level, pos, state, entity) -> entity.clientTick(level, pos, state);
        else if (!client && this.serverTick) return (level, pos, state, entity) -> entity.serverTick(level, pos, state);
        return null;
    }

}
