package dev.shadowsoffire.placebo.recipe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class VanillaPacketDispatcher {

    /**
     * Sends a {@link SUpdateTileEntityPacket} to all players watching this tile entity.
     */
    public static void dispatchTEToNearbyPlayers(BlockEntity tile) {
        ServerLevel world = (ServerLevel) tile.getLevel();
        world.getChunkSource().chunkMap.getPlayers(new ChunkPos(tile.getBlockPos()), false).forEach(player -> {
            player.connection.send(tile.getUpdatePacket());
        });
    }

    /**
     * Sends a {@link SUpdateTileEntityPacket} to all players watching this tile entity.
     */
    public static void dispatchTEToNearbyPlayers(Level world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile != null) dispatchTEToNearbyPlayers(tile);
    }
}
