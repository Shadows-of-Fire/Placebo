package shadows.placebo.net;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import shadows.placebo.json.PlaceboJsonReloadListener;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyed;
import shadows.placebo.util.NetworkUtils;
import shadows.placebo.util.NetworkUtils.MessageProvider;

public abstract class ReloadListenerPacket<T extends ReloadListenerPacket<T>> extends MessageProvider<T> {

	final String path;

	public ReloadListenerPacket(String path) {
		this.path = path;
	}

	public static class Start extends ReloadListenerPacket<Start> {

		public Start(String path) {
			super(path);
		}

		@Override
		public void write(Start msg, PacketBuffer buf) {
			buf.writeUtf(msg.path, 50);
		}

		@Override
		public Start read(PacketBuffer buf) {
			return new Start(buf.readUtf(50));
		}

		@Override
		public void handle(Start msg, Supplier<Context> ctx) {
			NetworkUtils.handlePacket(() -> () -> PlaceboJsonReloadListener.initSync(msg.path), ctx.get());
		}
	}

	public static class Content<V extends TypeKeyed<V>> extends ReloadListenerPacket<Content<V>> {

		final ResourceLocation key;
		final V item;

		public Content(String path, ResourceLocation key, V item) {
			super(path);
			this.key = key;
			this.item = item;
		}

		@Override
		public void write(Content<V> msg, PacketBuffer buf) {
			buf.writeUtf(msg.path, 50);
			buf.writeResourceLocation(msg.key);
			PlaceboJsonReloadListener.writeItem(msg.path, msg.item, buf);
		}

		@Override
		public Content<V> read(PacketBuffer buf) {
			String path = buf.readUtf(50);
			ResourceLocation key = buf.readResourceLocation();
			V item = PlaceboJsonReloadListener.readItem(path, key, buf);
			return new Content<>(path, key, item);
		}

		@Override
		public void handle(Content<V> msg, Supplier<Context> ctx) {
			NetworkUtils.handlePacket(() -> () -> PlaceboJsonReloadListener.acceptItem(msg.path, msg.key, msg.item), ctx.get());
		}
	}

	public static class End extends ReloadListenerPacket<End> {

		public End(String path) {
			super(path);
		}

		@Override
		public void write(End msg, PacketBuffer buf) {
			buf.writeUtf(msg.path, 50);
		}

		@Override
		public End read(PacketBuffer buf) {
			return new End(buf.readUtf(50));
		}

		@Override
		public void handle(End msg, Supplier<Context> ctx) {
			NetworkUtils.handlePacket(() -> () -> PlaceboJsonReloadListener.endSync(msg.path), ctx.get());
		}
	}
}
