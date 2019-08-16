package shadows.placebo.util;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkUtils {

	public static void sendToTracking(SimpleChannel channel, Object packet, ServerWorld world, BlockPos pos) {
		world.getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false).forEach(p -> {
			channel.sendTo(packet, p.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
		});
	}

	public static <MSG> void registerMessage(SimpleChannel channel, int id, Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
		channel.registerMessage(id, messageType, encoder, decoder, messageConsumer);
	}

	public static <T> void registerMessage(SimpleChannel channel, int id, MessageProvider<T> prov) {
		channel.registerMessage(id, prov.getMsgClass(), prov::write, prov::read, prov::handle);
	}

	public static abstract class MessageProvider<T> {

		public abstract Class<T> getMsgClass();

		public abstract T read(PacketBuffer buf);

		public abstract void write(T msg, PacketBuffer buf);

		public abstract void handle(T msg, Supplier<NetworkEvent.Context> ctx);
	}

	public static void handlePacket(Supplier<Runnable> r, NetworkEvent.Context ctx) {
		ctx.enqueueWork(r.get());
		ctx.setPacketHandled(true);
	}

}
