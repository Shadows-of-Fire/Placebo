package shadows.placebo.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkUtils {

	public static void sendToTracking(SimpleChannel channel, Object packet, ServerWorld world, BlockPos pos) {
		world.getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false).forEach(p -> {
			channel.sendTo(packet, p.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
		});
	}
}
