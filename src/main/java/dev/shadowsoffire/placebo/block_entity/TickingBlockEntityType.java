package dev.shadowsoffire.placebo.block_entity;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * A custom {@link BlockEntityType} that will automatically provide {@link BlockEntityTicker}s for {@linkplain TickingBlockEntity ticking block entities}.
 *
 * @param <T> The type of the ticking block entity.
 * @see TickingEntityBlock
 */
public class TickingBlockEntityType<T extends BlockEntity & TickingBlockEntity> extends BlockEntityType<T> {

    protected final TickSide side;

    public TickingBlockEntityType(BlockEntitySupplier<? extends T> pFactory, Set<Block> pValidBlocks, TickSide side) {
        super(pFactory, pValidBlocks, null);
        this.side = side;
    }

    /**
     * Returns the ticker for the given side, or null if the block entity does not tick on the specified side.
     *
     * @param client True if the ticker for the client side is being requested
     */
    @Nullable
    public BlockEntityTicker<T> getTicker(boolean client) {
        if (client && this.side.ticksOnClient()) {
            return (level, pos, state, entity) -> entity.clientTick(level, pos, state);
        }
        else if (!client && this.side.ticksOnServer()) {
            return (level, pos, state, entity) -> entity.serverTick(level, pos, state);
        }
        return null;
    }

    public static enum TickSide {
        CLIENT,
        SERVER,
        CLIENT_AND_SERVER;

        /**
         * {@return true if this mode should tick on the client}
         */
        public boolean ticksOnClient() {
            return this == CLIENT || this == CLIENT_AND_SERVER;
        }

        /**
         * {@return true if this mode should tick on the server}
         */
        public boolean ticksOnServer() {
            return this == SERVER || this == CLIENT_AND_SERVER;
        }
    }

}
