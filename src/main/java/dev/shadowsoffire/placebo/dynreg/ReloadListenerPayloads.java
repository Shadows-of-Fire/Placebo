package dev.shadowsoffire.placebo.dynreg;

import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.datafixers.util.Either;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry.SyncManagement;
import dev.shadowsoffire.placebo.network.PayloadProvider;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@ApiStatus.Internal
public class ReloadListenerPayloads {

    public static record Start(String path) implements CustomPacketPayload {

        public static final Type<Start> TYPE = new Type<>(Placebo.loc("reload_sync_start"));

        public static final StreamCodec<FriendlyByteBuf, Start> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Start::path,
            Start::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static class Provider implements PayloadProvider<Start> {

            @Override
            public Type<Start> getType() {
                return TYPE;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Start> getCodec() {
                return CODEC;
            }

            @Override
            public void handleClient(Start msg, IPayloadContext ctx) {
                SyncManagement.initSync(msg.path);
            }

            @Override
            public List<ConnectionProtocol> getSupportedProtocols() {
                return List.of(ConnectionProtocol.PLAY);
            }

            @Override
            public Optional<PacketFlow> getFlow() {
                return Optional.of(PacketFlow.CLIENTBOUND);
            }

            @Override
            public String getVersion() {
                return "1";
            }
        }
    }

    public static record Content<V>(String path, Identifier key, Either<V, ByteBuf> item) implements CustomPacketPayload {

        public static final Type<Content<?>> TYPE = new Type<>(Placebo.loc("reload_sync_content"));

        public static final StreamCodec<RegistryFriendlyByteBuf, Content<?>> CODEC = StreamCodec.of(Content::write, Content::read);

        public Content(String path, Identifier key, V item) {
            this(path, key, Either.left(item));
        }

        public Content(String path, Identifier key, ByteBuf buf) {
            this(path, key, Either.right(buf));
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static <V> void write(RegistryFriendlyByteBuf buf, Content<V> payload) {
            buf.writeUtf(payload.path, 50);
            buf.writeIdentifier(payload.key);
            SyncManagement.writeItem(payload.path, payload.item.orThrow(), buf);
        }

        /**
         * Reads a content payload. We defer deserialization of the underlying object, since it may depend on the state of
         * other registries that are being setup on the main thread.
         */
        public static <V> Content<V> read(RegistryFriendlyByteBuf buf) {
            String path = buf.readUtf(50);
            Identifier key = buf.readIdentifier();

            int size = buf.writerIndex() - buf.readerIndex();
            ByteBuf itemBuf = Unpooled.buffer(size, size);
            buf.readBytes(itemBuf);
            return new Content<>(path, key, itemBuf);
        }

        public static class Provider<V> implements PayloadProvider<Content<?>> {

            @Override
            public Type<Content<?>> getType() {
                return TYPE;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, Content<?>> getCodec() {
                return CODEC;
            }

            @Override
            public void handleClient(Content<?> msg, IPayloadContext ctx) {
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(msg.item.right().get(), ctx.player().registryAccess(), ConnectionType.NEOFORGE);

                try {
                    V value = SyncManagement.readItem(msg.path, buf);
                    SyncManagement.acceptItem(msg.path, msg.key, value);
                }
                catch (Exception ex) {
                    Placebo.LOGGER.error("Failure when deserializing a dynamic registry object via network: Registry: {}, Object ID: {}", msg.path, msg.key);
                    throw ex;
                }
            }

            @Override
            public List<ConnectionProtocol> getSupportedProtocols() {
                return List.of(ConnectionProtocol.PLAY);
            }

            @Override
            public Optional<PacketFlow> getFlow() {
                return Optional.of(PacketFlow.CLIENTBOUND);
            }

            @Override
            public String getVersion() {
                return "1";
            }
        }
    }

    public static record End(String path) implements CustomPacketPayload {

        public static final Type<End> TYPE = new Type<>(Placebo.loc("reload_sync_end"));

        public static final StreamCodec<FriendlyByteBuf, End> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, End::path,
            End::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static class Provider implements PayloadProvider<End> {

            @Override
            public Type<End> getType() {
                return TYPE;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, End> getCodec() {
                return CODEC;
            }

            @Override
            public void handleClient(End msg, IPayloadContext ctx) {
                SyncManagement.endSync(msg.path);
            }

            @Override
            public List<ConnectionProtocol> getSupportedProtocols() {
                return List.of(ConnectionProtocol.PLAY);
            }

            @Override
            public Optional<PacketFlow> getFlow() {
                return Optional.of(PacketFlow.CLIENTBOUND);
            }

            @Override
            public String getVersion() {
                return "1";
            }
        }
    }
}
