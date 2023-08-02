package dev.shadowsoffire.placebo.reload;

import java.util.function.Supplier;

import dev.shadowsoffire.placebo.json.PSerializer.PSerializable;
import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.network.MessageProvider;
import dev.shadowsoffire.placebo.reload.PlaceboJsonReloadListener.SyncManagement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent.Context;

public abstract class ReloadListenerPacket<T extends ReloadListenerPacket<T>> {

    final String path;

    public ReloadListenerPacket(String path) {
        this.path = path;
    }

    public static class Start extends ReloadListenerPacket<Start> {

        public Start(String path) {
            super(path);
        }

        public static class Provider implements MessageProvider<Start> {

            @Override
            public Class<Start> getMsgClass() {
                return Start.class;
            }

            @Override
            public void write(Start msg, FriendlyByteBuf buf) {
                buf.writeUtf(msg.path, 50);
            }

            @Override
            public Start read(FriendlyByteBuf buf) {
                return new Start(buf.readUtf(50));
            }

            @Override
            public void handle(Start msg, Supplier<Context> ctx) {
                MessageHelper.handlePacket(() -> () -> SyncManagement.initSync(msg.path), ctx);
            }
        }
    }

    public static class Content<V extends TypeKeyed & PSerializable<? super V>> extends ReloadListenerPacket<Content<V>> {

        final ResourceLocation key;
        final V item;

        public Content(String path, ResourceLocation key, V item) {
            super(path);
            this.key = key;
            this.item = item;
        }

        public static class Provider<V extends TypeKeyed & PSerializable<? super V>> implements MessageProvider<Content<V>> {

            @Override
            @SuppressWarnings("rawtypes")
            public Class<Content> getMsgClass() {
                return Content.class;
            }

            @Override
            public void write(Content<V> msg, FriendlyByteBuf buf) {
                buf.writeUtf(msg.path, 50);
                buf.writeResourceLocation(msg.key);
                SyncManagement.writeItem(msg.path, msg.item, buf);
            }

            @Override
            public Content<V> read(FriendlyByteBuf buf) {
                String path = buf.readUtf(50);
                ResourceLocation key = buf.readResourceLocation();
                V item = SyncManagement.readItem(path, key, buf);
                return new Content<>(path, key, item);
            }

            @Override
            public void handle(Content<V> msg, Supplier<Context> ctx) {
                MessageHelper.handlePacket(() -> () -> SyncManagement.acceptItem(msg.path, msg.item), ctx);
            }
        }
    }

    public static class End extends ReloadListenerPacket<End> {

        public End(String path) {
            super(path);
        }

        public static class Provider implements MessageProvider<End> {

            @Override
            public Class<?> getMsgClass() {
                return End.class;
            }

            @Override
            public void write(End msg, FriendlyByteBuf buf) {
                buf.writeUtf(msg.path, 50);
            }

            @Override
            public End read(FriendlyByteBuf buf) {
                return new End(buf.readUtf(50));
            }

            @Override
            public void handle(End msg, Supplier<Context> ctx) {
                MessageHelper.handlePacket(() -> () -> SyncManagement.endSync(msg.path), ctx);
            }
        }
    }
}
