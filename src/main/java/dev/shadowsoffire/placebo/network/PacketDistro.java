package dev.shadowsoffire.placebo.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;

public class PacketDistro {

    /**
     * Sends a payload to a specific player.
     */
    public static void sendTo(CustomPacketPayload payload, Player player) {
        PacketDistributor.PLAYER.with((ServerPlayer) player).send(payload);
    }

    /**
     * Sends a payload to all players in the given dimension.
     */
    public static void sendToDimension(CustomPacketPayload payload, ServerLevel level) {
        PacketDistributor.DIMENSION.with(level.dimension()).send(payload);
    }

    /**
     * Sends a payload to all players on the server.
     */
    public static void sendToAll(CustomPacketPayload payload) {
        PacketDistributor.ALL.noArg().send(payload);
    }

    /**
     * Sends a payload to all players who are watching a specific entity.
     * <p>
     * Does not send a packet to the entity itself.
     */
    public static void sendToTracking(CustomPacketPayload payload, Entity entity) {
        PacketDistributor.TRACKING_ENTITY.with(entity).send(payload);
    }

    /**
     * Sends a payload to all players who are watching a specific entity.
     * <p>
     * Also sends a payload to the entity, if it's a {@link ServerPlayer}.
     */
    public static void sendToTrackingAndSelf(CustomPacketPayload payload, Entity entity) {
        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entity).send(payload);
    }

    /**
     * Sends a payload to all players who are watching a specific chunk.
     */
    public static void sendToTracking(CustomPacketPayload payload, ServerLevel world, BlockPos pos) {
        sendToTracking(payload, world.getChunkAt(pos));
    }

    /**
     * Sends a payload to all players who are watching a specific chunk.
     */
    public static void sendToTracking(CustomPacketPayload payload, LevelChunk chunk) {
        PacketDistributor.TRACKING_CHUNK.with(chunk).send(payload);
    }

    /**
     * Sends a payload to the server.
     */
    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.SERVER.noArg().send(payload);
    }

}
