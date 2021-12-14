package shadows.placebo.network;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.simple.SimpleChannel;

public class MessageHelper {

	/**
	 * Registers a message directly using the base FML systems.  Requires providing each method individually.
	 * @param channel Channel to register for.
	 * @param id Message id.
	 * @param messageType Class of the message object.
	 * @param encoder Method to write the method to buf.
	 * @param decoder Method to read the message from buf.
	 * @param messageConsumer Executor for the read message.
	 */
	public static <MSG> void registerMessage(SimpleChannel channel, int id, Class<MSG> messageType, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
		channel.registerMessage(id, messageType, encoder, decoder, messageConsumer);
	}

	/**
	 * Registers a message using {@link MessageProvider}.
	 * @param channel Channel to register for.
	 * @param id Message id.
	 * @param prov An instance of the message provider.  Note that this object will be kept around, so try to keep all fields uninitialized if possible.
	 */
	public static <T> void registerMessage(SimpleChannel channel, int id, MessageProvider<T> prov) {
		channel.registerMessage(id, prov.getMsgClass(), prov::write, prov::read, prov::handle);
	}

	/**
	 * Handles a packet.  Enqueues the runnable to run on the main thread and calls {@link NetworkEvent.Context#setPacketHandled(boolean)}
	 * @param r Code to run to handle the packet.  Uses a supplier to try and combat some classloading stuff.
	 * @param ctx Context object.  Available from {@link MessageProvider#handle(Object, Supplier)}
	 */
	public static void handlePacket(Supplier<Runnable> r, Supplier<NetworkEvent.Context> ctxSup) {
		NetworkEvent.Context ctx = ctxSup.get();
		ctx.enqueueWork(r.get());
		ctx.setPacketHandled(true);
	}

}
