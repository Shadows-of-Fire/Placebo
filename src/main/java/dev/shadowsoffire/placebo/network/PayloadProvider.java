package dev.shadowsoffire.placebo.network;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * A Payload Provider encapsulates the default components that make up a custom payload packet registration.
 *
 * @param <T> The type of the payload.
 * @param <C> The type of the payload context.
 */
public interface PayloadProvider<T extends CustomPacketPayload, C extends IPayloadContext> {

    /**
     * @return The ID of the payload registration being provided. Must match {@link CustomPacketPayload#id()}.
     */
    ResourceLocation id();

    /**
     * Reads the message from a byte buffer. Must construct a new message object.
     *
     * @param buf The byte buffer. Data must be read in the order it was written.
     * @return A new message instance with the read data.
     */
    T read(FriendlyByteBuf buf);

    /**
     * Handle the payload.
     * See {@link PayloadHelper#handlePacket(Supplier, Supplier)}
     *
     * @param msg The messsage to handle.
     * @param ctx Relevant network context information.
     */
    void handle(T msg, C ctx);

    /**
     * Gets a list of all supported connection protocols. This method may allocated a new list, as it is only called once.
     *
     * @apiNote Currently, only {@link ConnectionProtocol#CONFIGURATION} and {@link ConnectionProtocol#PLAY} are supported.
     */
    List<ConnectionProtocol> getSupportedProtocols();

    /**
     * Gets the network direction in which this payload may be sent.<br>
     * {@link Optional#empty()} means both directions are supported.
     *
     * @return The optional containing the valid network direction, or empty if both directions are supported.
     */
    Optional<PacketFlow> getFlow();

    /**
     * The version of this payload. If a version is provided, the versions must match on both sides, or the connection will fail.
     * <p>
     * You should always change the payload's version if the serialization changes.
     */
    default String getVersion() {
        return "1";
    }

    /**
     * {@return true if this payload is optional, and does not need to be present on the other side}
     */
    default boolean isOptional() {
        return true;
    }

}
