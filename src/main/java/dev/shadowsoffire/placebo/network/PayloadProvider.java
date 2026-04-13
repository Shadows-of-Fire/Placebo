package dev.shadowsoffire.placebo.network;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;

/**
 * A Payload Provider encapsulates the default components that make up a custom payload packet registration.
 *
 * @param <T> The type of the payload.
 * @param <C> The type of the payload context.
 */
public interface PayloadProvider<T extends CustomPacketPayload> {

    /**
     * @return The type of the payload being registered. Must match {@link CustomPacketPayload#type()}.
     */
    CustomPacketPayload.Type<T> getType();

    /**
     * @return The {@link StreamCodec} responsible for encoding/decoding the payload.
     */
    StreamCodec<? super RegistryFriendlyByteBuf, T> getCodec();

    /**
     * Handle the payload when received on the client.
     * See {@link PayloadHelper#handlePacket(Supplier, Supplier)}
     *
     * @param msg The messsage to handle.
     * @param ctx Relevant network context information.
     */
    default void handleClient(T msg, IPayloadContext ctx) {}

    /**
     * Handle the payload when received on the server.
     * See {@link PayloadHelper#handlePacket(Supplier, Supplier)}
     *
     * @param msg The messsage to handle.
     * @param ctx Relevant network context information.
     */
    default void handleServer(T msg, IPayloadContext ctx) {}
    
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
    String getVersion();

    /**
     * {@return true if this payload is optional, and does not need to be present on the other side}
     */
    default boolean isOptional() {
        return false;
    }

    /**
     * @return The thread that will be used to execute the {@link #handle(CustomPacketPayload, IPayloadContext)} method.
     */
    default HandlerThread getHandlerThread() {
        return HandlerThread.MAIN;
    }

}
