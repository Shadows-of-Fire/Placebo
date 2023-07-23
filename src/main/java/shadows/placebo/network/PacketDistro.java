package shadows.placebo.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketDistro {

    /**
     * Sends a packet to all players who are watching a specific chunk.
     */
    public static void sendToTracking(SimpleChannel channel, Object packet, ServerLevel world, BlockPos pos) {
        world.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false).forEach(p -> {
            channel.sendTo(packet, p.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
        });
    }

    /**
     * Sends a packet to a specific player.
     */
    public static void sendTo(SimpleChannel channel, Object packet, Player player) {
        channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), packet);
    }

    /**
     * Sends a packet to all players on the server.
     */
    public static void sendToAll(SimpleChannel channel, Object packet) {
        channel.send(PacketDistributor.ALL.noArg(), packet);
    }

}
