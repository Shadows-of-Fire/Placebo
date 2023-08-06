package dev.shadowsoffire.placebo.network;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

/**
 * A Message Provider encapsulates the default components that make up a SimpleChannel message.
 * That is, reading, writing, and handling.
 *
 * @param <T> The type of the message.
 */
public interface MessageProvider<T> {

    /**
     * @return The class of the message being provided, for registration to the channel.
     */
    Class<?> getMsgClass();

    /**
     * Writes the message to the byte buffer. Only use the parameter messager instance.
     *
     * @param msg The message to serialize.
     * @param buf The byte buffer.
     */
    void write(T msg, FriendlyByteBuf buf);

    /**
     * Reads the message from a byte buffer. Must construct a new message object.
     *
     * @param buf The byte buffer. Data must be read in the order it was written.
     * @return A new message instance with the read data.
     */
    T read(FriendlyByteBuf buf);

    /**
     * Handle the message. Must call {@link NetworkEvent.Context#setPacketHandled(boolean)} to avoid errors.
     * See {@link MessageHelper#handlePacket(Supplier, Supplier)}
     *
     * @param msg The messsage to handle.
     * @param ctx Relevant network context information.
     */
    void handle(T msg, Supplier<NetworkEvent.Context> ctx);

    /**
     * Gets the network direction in which this packet may be sent.<br>
     * {@link Optional#empty()} means both directions are supported.
     * 
     * @return The optional containing the valid network direction, or empty if both directions are supported.
     * @apiNote This method will not have a default return value in a future version.
     */
    default Optional<NetworkDirection> getNetworkDirection() {
        return Optional.empty();
    }
}
