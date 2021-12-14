package shadows.placebo.network;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * A Message Provider encapsulates the default components that make up a SimpleChannel message.
 * That is, reading, writing, and handling. If "this" == "T" be sure to use the parameter fields and not "this".
 *
 * @param <T> The type of the message.  Can be this class or another class.
 */
public interface MessageProvider<T> {

	@SuppressWarnings("unchecked")
	default Class<T> getMsgClass() {
		return (Class<T>) this.getClass();
	}

	/**
	 * Writes the message to the byte buffer. Only use the parameter messager instance.<br>
	 * @param msg The message to serialize.
	 * @param buf The byte buffer.
	 */
	public abstract void write(T msg, FriendlyByteBuf buf);

	/**
	 * Reads the message from a byte buffer. Must construct a new message object.<br>
	 * @param buf The byte buffer. Data must be read in the order it was written.
	 * @return A new message instance with the read data.
	 */
	public abstract T read(FriendlyByteBuf buf);

	/**
	 * Handle the message. Must call {@link NetworkEvent.Context#setPacketHandled(boolean)} to avoid errors.
	 * See {@link MessageHelper#handlePacket(Supplier, Supplier)}
	 * @param msg The messsage to handle.
	 * @param ctx Relevant network context information.
	 */
	public abstract void handle(T msg, Supplier<NetworkEvent.Context> ctx);
}