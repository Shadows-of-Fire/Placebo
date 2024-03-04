package dev.shadowsoffire.placebo.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class PacketDistro {

    /**
     * Sends a packet to all players who are watching a specific chunk.
     */
    public static void sendToTracking(CustomPacketPayload payload, ServerLevel world, BlockPos pos) {
        PacketDistributor.TRACKING_CHUNK.with(world.getChunkAt(pos)).send(payload);
    }

    /**
     * Sends a packet to a specific player.
     */
    public static void sendTo(CustomPacketPayload payload, Player player) {
        PacketDistributor.PLAYER.with((ServerPlayer) player).send(payload);
    }

    /**
     * Sends a packet to all players on the server.
     */
    public static void sendToAll(CustomPacketPayload payload) {
        PacketDistributor.ALL.noArg().send(payload);
    }

}
