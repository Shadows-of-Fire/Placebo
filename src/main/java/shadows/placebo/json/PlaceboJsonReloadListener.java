package shadows.placebo.json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import shadows.placebo.Placebo;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyed;
import shadows.placebo.network.PacketDistro;
import shadows.placebo.packets.ReloadListenerPacket;

public abstract class PlaceboJsonReloadListener<V extends TypeKeyed<V>> extends SimpleJsonResourceReloadListener {

	private static final Map<String, PlaceboJsonReloadListener<?>> SYNC_REGISTRY = new HashMap<>();

	public static final ResourceLocation DEFAULT = new ResourceLocation("default");

	protected final Logger logger;
	protected final String path;
	protected final boolean synced;
	protected final boolean subtypes;
	protected final BiMap<ResourceLocation, SerializerBuilder<V>.Serializer> serializers = HashBiMap.create();

	protected Map<ResourceLocation, V> registry = ImmutableMap.of();

	private final Map<ResourceLocation, V> staged = new HashMap<>();

	public PlaceboJsonReloadListener(Logger logger, String path, boolean synced, boolean subtypes) {
		super(new GsonBuilder().setLenient().create(), path);
		this.logger = logger;
		this.path = path;
		this.synced = synced;
		this.subtypes = subtypes;
		this.registerBuiltinSerializers();
		if (serializers.isEmpty()) throw new RuntimeException("Attempted to create a json reload listener for " + path + " with no top-level serializers!");
		if (synced) {
			if (SYNC_REGISTRY.containsKey(path)) throw new RuntimeException("Attempted to create a synced json reload listener for " + path + " but one already exists!");
			SYNC_REGISTRY.put(path, this);
			MinecraftForge.EVENT_BUS.addListener(this::sync);
		}
	}

	@Override
	protected final void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
		this.beginReload();
		objects.forEach((key, ele) -> {
			try {
				if (checkAndLogEmpty(ele, key, path, logger)) {
					JsonObject obj = ele.getAsJsonObject();
					SerializerBuilder<V>.Serializer serializer;
					if (subtypes) {
						ResourceLocation type = new ResourceLocation(obj.get("type").getAsString());
						serializer = serializers.get(type);
						if (serializer == null) throw new RuntimeException("Attempted to deserialize a " + path + " with type " + type + " but no serializer exists!");
					} else {
						serializer = serializers.get(DEFAULT);
					}
					V deserialized = serializer.deserialize(obj);
					deserialized.setId(key);
					deserialized.setSerializer(serializer);
					Preconditions.checkNotNull(deserialized.getId(), "A " + path + " with id " + key + " failed to set ID.");
					Preconditions.checkNotNull(deserialized.getSerializer(), "A " + path + " with id " + key + " failed to set serializer.");
					this.register(key, deserialized);
				}
			} catch (Exception e) {
				logger.error("Failed parsing {} file {}.", path, key);
				e.printStackTrace();
			}
		});
		this.onReload();
	}

	/**
	 * Add all default serializers to this reload listener.
	 * This should be a series of calls to {@link registerSerializer}
	 */
	protected abstract void registerBuiltinSerializers();

	/**
	 * Called when this manager begins reloading all items.
	 * Should handle clearing internal data caches.
	 */
	protected void beginReload() {
		this.registry = new HashMap<>();
	}

	/**
	 * Called after this manager has finished reloading all items.
	 * Should handle any info logging, and data immutability.
	 */
	protected void onReload() {
		this.registry = ImmutableMap.copyOf(this.registry);
		logger.info("Registered {} {}.", this.registry.size(), path);
	}

	public final void registerSerializer(ResourceLocation id, SerializerBuilder<V> serializer) {
		if (!subtypes) {
			if (!serializers.isEmpty()) throw new RuntimeException("Attempted to register a " + path + " serializer with id " + id + " but subtypes are not supported!");
			serializers.put(DEFAULT, serializer.build(synced));
		} else {
			if (serializers.containsKey(id)) throw new RuntimeException("Attempted to register a " + path + " serializer with id " + id + " but one already exists!");
			serializers.put(id, serializer.build(this.synced));
		}
	}

	private final void sync(OnDatapackSyncEvent e) {
		ServerPlayer player = e.getPlayer();
		if (player == null) {
			PacketDistro.sendToAll(Placebo.CHANNEL, new ReloadListenerPacket.Start(this.path));
			this.registry.forEach((k, v) -> {
				PacketDistro.sendToAll(Placebo.CHANNEL, new ReloadListenerPacket.Content<>(this.path, k, v));
			});
			PacketDistro.sendToAll(Placebo.CHANNEL, new ReloadListenerPacket.End(this.path));
		} else {
			PacketDistro.sendTo(Placebo.CHANNEL, new ReloadListenerPacket.Start(this.path), player);
			this.registry.forEach((k, v) -> {
				PacketDistro.sendTo(Placebo.CHANNEL, new ReloadListenerPacket.Content<>(this.path, k, v), player);
			});
			PacketDistro.sendTo(Placebo.CHANNEL, new ReloadListenerPacket.End(this.path), player);
		}
	}

	/**
	 * Registers a single item of this type to the registry during reload.
	 * You can override this method to process things a bit differently.
	 */
	protected <T extends V> void register(ResourceLocation key, T item) {
		this.registry.put(key, item);
	}

	/**
	 * @return An immutable view of all keys registered for this type.
	 */
	public Set<ResourceLocation> getKeys() {
		return this.registry.keySet();
	}

	/**
	 * @return An immutable view of all items registered for this type.
	 */
	public Collection<V> getValues() {
		return this.registry.values();
	}

	/**
	 * @return The item associated with this key, or null.
	 */
	@Nullable
	public V getValue(ResourceLocation key) {
		return getOrDefault(key, null);
	}

	/**
	 * @return The item associated with this key, or the default value.
	 */
	public V getOrDefault(ResourceLocation key, V defValue) {
		return this.registry.getOrDefault(key, defValue);
	}

	/**
	 * Checks if an item is empty, and if it is, returns false and logs the key.
	 */
	public static boolean checkAndLogEmpty(JsonElement e, ResourceLocation id, String type, Logger logger) {
		String s = e.toString();
		if (s.isEmpty() || s.equals("{}")) {
			logger.debug("Ignoring {} item with id {} as it is empty.", type, id);
			return false;
		}
		return true;
	}

	public static interface TypeKeyed<V extends TypeKeyed<V>> {
		void setId(ResourceLocation id);

		void setSerializer(SerializerBuilder<V>.Serializer serializer);

		ResourceLocation getId();

		SerializerBuilder<V>.Serializer getSerializer();
	}

	public static abstract class TypeKeyedBase<V extends TypeKeyed<V>> implements TypeKeyed<V> {
		protected ResourceLocation id;
		protected SerializerBuilder<V>.Serializer serializer;

		@Override
		public void setId(ResourceLocation id) {
			if (this.id != null) throw new UnsupportedOperationException();
			this.id = id;
		}

		@Override
		public void setSerializer(SerializerBuilder<V>.Serializer serializer) {
			if (this.serializer != null) throw new UnsupportedOperationException();
			this.serializer = serializer;
		}

		@Override
		public ResourceLocation getId() {
			return id;
		}

		@Override
		public SerializerBuilder<V>.Serializer getSerializer() {
			return serializer;
		}
	}

	public static void initSync(String path) {
		SYNC_REGISTRY.computeIfPresent(path, (k, v) -> {
			v.staged.clear();
			return v;
		});
	}

	public static <V extends TypeKeyed<V>> void writeItem(String path, V value, FriendlyByteBuf buf) {
		SYNC_REGISTRY.computeIfPresent(path, (k, v) -> {
			ResourceLocation serId = v.serializers.inverse().get(value.getSerializer());
			buf.writeResourceLocation(serId);
			value.getSerializer().serialize(value, buf);
			return v;
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V extends TypeKeyed<V>> V readItem(String path, ResourceLocation key, FriendlyByteBuf buf) {
		var listener = SYNC_REGISTRY.get(path);
		if (listener == null) throw new RuntimeException("Received sync packet for unknown registry!");
		var serializer = listener.serializers.get(buf.readResourceLocation());
		V v = (V) serializer.deserialize(buf);
		v.setId(key);
		v.setSerializer((SerializerBuilder<V>.Serializer) serializer);
		return v;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V extends TypeKeyed<V>> void acceptItem(String path, ResourceLocation key, V value) {
		SYNC_REGISTRY.computeIfPresent(path, (k, v) -> {
			((Map) v.staged).put(key, value);
			return v;
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V extends TypeKeyed<V>> void endSync(String path) {
		SYNC_REGISTRY.computeIfPresent(path, (k, v) -> {
			v.beginReload();
			v.staged.forEach(((PlaceboJsonReloadListener) v)::register);
			v.onReload();
			return v;
		});
	}

}
