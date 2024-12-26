package dev.shadowsoffire.placebo.reload;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.json.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CodecException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * A Dynamic Registry is a reload listener which acts like a registry. Unlike datapack registries, it can reload.
 * <p>
 * To utilize this class, subclass it, and provide the appropriate constructor parameters.<br>
 * Then, create a single static instance of it and keep it around.
 * <p>
 * You will provide your serializers via {@link #registerBuiltinCodecs()}.<br>
 * You will then need to register it via {@link #registerToBus()}.<br>
 * From then on, loading of files, condition checks, network sync, and everything else is automatically handled.
 *
 * @param <R> The base type of objects stored in this registry.
 */
public abstract class DynamicRegistry<R extends CodecProvider<? super R>> extends SimpleJsonResourceReloadListener {

    protected final Logger logger;
    protected final String path;
    protected final boolean synced;
    protected final boolean subtypes;
    protected final CodecMap<R> codecs;
    protected final Codec<DynamicHolder<R>> holderCodec;
    protected final StreamCodec<ByteBuf, DynamicHolder<R>> holderStreamCodec;
    protected final BiMap<ResourceLocation, StreamCodec<RegistryFriendlyByteBuf, ? extends R>> streamCodecs;

    /**
     * Internal registry. Immutable when outside of the registration phase.
     * <p>
     * This map is cleared in {@link #beginReload()} and frozen in {@link #onReload()}
     */
    protected BiMap<ResourceLocation, R> registry = ImmutableBiMap.of();

    /**
     * Staged data used during the sync process. Discarded when running an integrated server.
     */
    private final Map<ResourceLocation, R> staged = new HashMap<>();

    /**
     * Map of all holders that have ever been requested for this registry.
     */
    private final Map<ResourceLocation, DynamicHolder<R>> holders = new ConcurrentHashMap<>();

    /**
     * List of callbacks attached to this registry.
     *
     * @see #addCallback(RegistryCallback)
     * @see #removeCallback(RegistryCallback)
     */
    private final Set<RegistryCallback<R>> callbacks = new HashSet<>();

    /**
     * Constructs a new dynamic registry.
     *
     * @param logger   The logger used by this listener for all relevant messages.
     * @param path     The datapack path used by this listener for loading files.
     * @param synced   If this listener will be synced over the network.
     * @param subtypes If this listener supports subtyped objects (and the "type" key on top-level objects).
     * @apiNote After construction, {@link #registerToBus()} must be called during setup.
     */
    public DynamicRegistry(Logger logger, String path, boolean synced, boolean subtypes) {
        super(new GsonBuilder().setLenient().create(), path);
        this.logger = logger;
        this.path = path;
        this.synced = synced;
        this.subtypes = subtypes;
        this.codecs = new CodecMap<>(path);
        this.streamCodecs = HashBiMap.create();
        this.registerBuiltinCodecs();
        if (this.codecs.isEmpty()) {
            throw new RuntimeException("Attempted to create a dynamic registry for " + path + " with no built-in codecs!");
        }
        this.holderCodec = ResourceLocation.CODEC.xmap(this::holder, DynamicHolder::getId);
        this.holderStreamCodec = ResourceLocation.STREAM_CODEC.map(this::holder, DynamicHolder::getId);
    }

    /**
     * Processes all the json entries through the registration chain. That registration chain is as follows:
     * <ol>
     * <li>Empty JSON check: Empty values are discarded with a warning message.</li>
     * <li>Condition check: Values that are conditionally disabled are ignored. A note is logged at the trace level.</li>
     * <li>Deserialization: The serializer is pulled from the 'type' field if subtypes is enabled, or the default serializer is used.</li>
     * <li>Validation: Certain states of the object are checked for sanity.</li>
     * <li>Registration: The item is added to the {@link #registry}.</li>
     * </ol>
     */
    @Override
    protected final void apply(Map<ResourceLocation, JsonElement> objects, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        this.beginReload();
        ConditionalOps<JsonElement> ops = this.makeConditionalOps();
        objects.forEach((key, ele) -> {
            try {
                if (JsonUtil.checkAndLogEmpty(ele, key, this.path, this.logger) && JsonUtil.checkConditions(ele, key, this.path, this.logger, ops)) {
                    JsonObject obj = ele.getAsJsonObject();
                    R deserialized = this.codecs.decode(ops, obj).getOrThrow(this::makeCodecException).getFirst();
                    Preconditions.checkNotNull(deserialized.getCodec(), "A " + this.path + " with id " + key + " is not declaring a codec.");
                    Preconditions.checkNotNull(this.codecs.getKey(deserialized.getCodec()), "A " + this.path + " with id " + key + " is declaring an unregistered codec.");
                    this.register(key, deserialized);
                }
            }
            catch (Exception e) {
                this.logger.error("Failed parsing {} file {}.", this.path, key);
                this.logger.error("Underlying Exception: ", e);
            }
        });
        this.onReload();
    }

    /**
     * Add all default serializers to this reload listener.
     * This should be a series of calls to {@link #registerCodec(ResourceLocation, Codec)}
     */
    protected abstract void registerBuiltinCodecs();

    /**
     * Called when this manager begins reloading all items.
     * Should handle clearing internal data caches.
     */
    protected void beginReload() {
        this.callbacks.forEach(l -> l.beginReload(this));
        this.registry = HashBiMap.create();
        this.holders.values().forEach(DynamicHolder::unbind);
    }

    /**
     * Called after this manager has finished reloading all items.
     * Should handle any info logging, and data immutability.
     */
    protected void onReload() {
        this.registry = ImmutableBiMap.copyOf(this.registry);
        this.logger.info("Registered {} {}.", this.registry.size(), this.path);
        this.callbacks.forEach(l -> l.onReload(this));
        this.holders.values().forEach(DynamicHolder::bind);
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
    public Collection<R> getValues() {
        return this.registry.values();
    }

    /**
     * @return The item associated with this key, or null.
     */
    @Nullable
    public R getValue(ResourceLocation key) {
        return this.registry.get(key);
    }

    /**
     * @return The key associated with this value, or null.
     */
    @Nullable
    public ResourceLocation getKey(R value) {
        return this.registry.inverse().get(value);
    }

    /**
     * @return The item associated with this key, or the default value.
     */
    public R getOrDefault(ResourceLocation key, R defValue) {
        return this.registry.getOrDefault(key, defValue);
    }

    /**
     * Registers this listener to the event bus as is appropriate.
     * This should be called for ALL listeners from common setup.
     */
    public void registerToBus() {
        if (this.synced) SyncManagement.registerForSync(this);
        NeoForge.EVENT_BUS.addListener(this::addReloader);
    }

    /**
     * Creates a {@link DynamicHolder} pointing to a value stored in this reload listener.
     *
     * @param id The ID of the target value.
     * @return A dynamic registry object pointing to the target value.
     */
    public DynamicHolder<R> holder(@Nullable ResourceLocation id) {
        if (id == null) {
            return this.emptyHolder();
        }
        return this.holders.computeIfAbsent(id, k -> new DynamicHolder<>(this, k));
    }

    /**
     * Gets the {@link DynamicHolder} associated with a particular value if it exists.
     * <p>
     * If the value is not present in the registry, instead returns {@linkplain #emptyHolder() the empty holder}.
     *
     * @see #holder(ResourceLocation)
     */
    public DynamicHolder<R> holder(R value) {
        ResourceLocation key = this.getKey(value);
        return this.holder(key == null ? DynamicHolder.EMPTY : key);
    }

    /**
     * Gets the empty {@link DynamicHolder}.
     *
     * @see #holder(ResourceLocation)
     */
    public DynamicHolder<R> emptyHolder() {
        return this.holder(DynamicHolder.EMPTY);
    }

    /**
     * Returns a {@link Codec} that can handle {@link DynamicHolder}s for this registry.<br>
     * The serialized form is {@link ResourceLocation}.
     *
     * @return The Dynamic Holder Codec for this registry.
     */
    public Codec<DynamicHolder<R>> holderCodec() {
        return this.holderCodec;
    }

    /**
     * Returns a {@link StreamCodec} that can handle {@link DynamicHolder}s for this registry.<br>
     * The dynamic holders will be transmitted as {@link ResourceLocation}s using {@link ResourceLocation#STREAM_CODEC}.
     *
     * @return The Dynamic Holder Stream Codec for this registry.
     * @throws UnsupportedOperationException if this is not a synced registry.
     */
    public StreamCodec<ByteBuf, DynamicHolder<R>> holderStreamCodec() {
        if (!this.synced) {
            throw new UnsupportedOperationException("Cannot retrieve a stream codec for the non-synced DynamicRegistry: " + this.path);
        }
        return this.holderStreamCodec;
    }

    /**
     * Registers a codec to this registry. Does not permit duplicates, and does not permit multiple registration. Not valid for registries that do not support
     * subtypes.
     *
     * @param key         The key of the codec.
     * @param codec       The codec being registered.
     * @param streamCodec A stream codec for synced registries.
     * @throws UnsupportedOperationException if this registry does not support subtypes. Use {@link #registerDefaultCodec(ResourceLocation, Codec)} instead.
     */
    public final void registerCodec(ResourceLocation key, Codec<? extends R> codec, StreamCodec<RegistryFriendlyByteBuf, ? extends R> streamCodec) {
        if (!this.subtypes) {
            throw new UnsupportedOperationException("Attempted to call registerCodec on a registry which does not support subtypes.");
        }
        this.registerInternal(key, codec, streamCodec);
    }

    /**
     * Variant of {@link #registerCodec(ResourceLocation, Codec, StreamCodec)} that automatically wraps the codec as a stream codec.
     * <p>
     * If this registry is synced, prefer providing a stream codec via the other overload.
     */
    public final void registerCodec(ResourceLocation key, Codec<? extends R> codec) {
        registerCodec(key, codec, ByteBufCodecs.fromCodecWithRegistries(codec));
    }

    /**
     * Registers a default codec for this registry. Only one default codec can be registered, and it cannot be changed.
     *
     * @param key   The key of the codec.
     * @param codec The codec being registered.
     * @throws UnsupportedOperationException if a default codec has already been registered.
     */
    protected final void registerDefaultCodec(ResourceLocation key, Codec<? extends R> codec, StreamCodec<RegistryFriendlyByteBuf, ? extends R> streamCodec) {
        if (this.codecs.getDefaultCodec() != null) {
            throw new UnsupportedOperationException("Attempted to register a second " + this.path + " default codec with key " + key);
        }
        this.registerInternal(key, codec, streamCodec);
        this.codecs.setDefaultCodec(codec);
    }

    /**
     * Variant of {@link #registerDefaultCodec(ResourceLocation, Codec, StreamCodec)} that automatically wraps the codec as a stream codec.
     * <p>
     * If this registry is synced, prefer providing a stream codec via the other overload.
     */
    protected final void registerDefaultCodec(ResourceLocation key, Codec<? extends R> codec) {
        registerDefaultCodec(key, codec, ByteBufCodecs.fromCodecWithRegistries(codec));
    }

    /**
     * Registers a ListenerCallback to this reload listener.
     */
    public final boolean addCallback(RegistryCallback<R> callback) {
        return this.callbacks.add(callback);
    }

    /**
     * Removes a ListenerCallback from this reload listener.
     * Must be the same instance as one that was previously registered, or an object that implements equals/hashcode.
     */
    public final boolean removeCallback(RegistryCallback<R> callback) {
        return this.callbacks.remove(callback);
    }

    /**
     * Returns the path used by this registry.
     */
    public final String getPath() {
        return this.path;
    }

    /**
     * Returns the direct element codec, which can be used for de/serializing an element known by this registry.
     */
    public final Codec<R> elementCodec() {
        return this.codecs;
    }

    /**
     * Validates that every created {@link DynamicHolder} is bound to a regsitry entry.
     * <p>
     * This is primarily used as a sanity check in data generation.
     * 
     * @throws RuntimeException if any unbound holders are detected.
     */
    public final void validateExistingHolders() {
        String error = "";
        for (DynamicHolder<R> holder : this.holders.values()) {
            if (!holder.isBound()) {
                error += "Failed to validate dynamic holder %s for registry %s\n".formatted(holder.getId(), this.getPath());
            }
        }
        if (!error.isEmpty()) {
            throw new RuntimeException(error);
        }
    }

    /**
     * Registers a single item of this type to the registry during reload.
     * <p>
     * Override {@link #validateItem} to perform additional validation of registered objects.
     *
     * @param key   The key of the value being registered.
     * @param value The value being registered.
     * @throws UnsupportedOperationException if the key is already in use.
     */
    protected final void register(ResourceLocation key, R value) {
        if (this.registry.containsKey(key)) throw new UnsupportedOperationException("Attempted to register a " + this.path + " with a duplicate registry ID! Key: " + key);
        this.validateItem(key, value);
        this.registry.put(key, value);
        this.holders.computeIfAbsent(key, k -> new DynamicHolder<>(this, k));
    }

    /**
     * Validates that an individual item meets any criteria set by this reload listener.<br>
     * Called just before insertion into the registry.
     *
     * @param key   The key of the value being registered.
     * @param value The value being registered.
     */
    protected void validateItem(ResourceLocation key, R value) {}

    /**
     * Adds this reload listener to the {@link ReloadableServerResources}.
     */
    private void addReloader(AddReloadListenerEvent e) {
        e.addListener(this);
    }

    /**
     * Replaces the contents of the live registry with the staging registry.<br>
     * This triggers the full reload process for the client.
     *
     * @implNote Not executed when hosting a singleplayer world, as it would replace the server data.
     */
    private void pushStagedToLive() {
        this.beginReload();
        this.staged.forEach(this::register);
        this.onReload();
    }

    /**
     * Performs a fake reload by making a copy of {@link #registry} and re-registering the original contents.
     * This triggers the full reload process for the client.
     *
     * @implNote This is used instead of {@link #pushStagedToLive()} for singleplayer hosts to avoid data loss.
     */
    private void triggerClientsideReload() {
        this.staged.clear();
        this.staged.putAll(this.registry);
        this.beginReload();
        this.staged.forEach(this::register);
        this.onReload();
    }

    private CodecException makeCodecException(String msg) {
        return new CodecException("Codec failure for type %s, message: %s".formatted(this.path, msg));
    }

    /**
     * Sync event handler. Sends the start packet, a content packet for each item, and then the end packet.
     */
    private void sync(OnDatapackSyncEvent e) {
        ServerPlayer player = e.getPlayer();
        Consumer<CustomPacketPayload> target = player == null ? PacketDistributor::sendToAllPlayers : payload -> PacketDistributor.sendToPlayer(player, payload);

        target.accept(new ReloadListenerPayloads.Start(this.path));
        this.registry.forEach((k, v) -> {
            target.accept(new ReloadListenerPayloads.Content<>(this.path, k, Either.left(v)));
        });
        target.accept(new ReloadListenerPayloads.End(this.path));
    }

    private void registerInternal(ResourceLocation key, Codec<? extends R> codec, StreamCodec<RegistryFriendlyByteBuf, ? extends R> streamCodec) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(codec, "Attempted to register a null codec for key " + key);
        Preconditions.checkNotNull(streamCodec, "Attempted to register a null stream codec for key " + key);
        this.codecs.register(key, codec);
        this.streamCodecs.put(key, streamCodec);
    }

    /**
     * Internal class for sync management.
     */
    @ApiStatus.Internal
    static class SyncManagement {

        private static final Map<String, DynamicRegistry<?>> SYNC_REGISTRY = new LinkedHashMap<>();

        /**
         * Registers a {@link DynamicRegistry} for syncing.
         *
         * @param listener The listener to register.
         * @throws UnsupportedOperationException if the listener is not a synced listener.
         * @throws UnsupportedOperationException if the listener is already registered to the sync registry.
         */
        static void registerForSync(DynamicRegistry<?> listener) {
            if (!listener.synced) throw new UnsupportedOperationException("Attempted to register the non-synced JSON Reload Listener " + listener.path + " as a synced listener!");
            synchronized (SYNC_REGISTRY) {
                if (SYNC_REGISTRY.containsKey(listener.path)) throw new UnsupportedOperationException("Attempted to register the JSON Reload Listener for syncing " + listener.path + " but one already exists!");
                if (SYNC_REGISTRY.isEmpty()) NeoForge.EVENT_BUS.addListener(SyncManagement::syncAll);
                SYNC_REGISTRY.put(listener.path, listener);
            }
        }

        /**
         * Begins the sync for a specific listener.
         *
         * @param path The path of the listener being synced.
         */
        static void initSync(String path) {
            ifPresent(path, registry -> registry.staged.clear());
            Placebo.LOGGER.info("Starting sync for {}", path);
        }

        /**
         * Write an item (with the same type as the listener) to the network.
         *
         * @param <V>   The type of item being written.
         * @param path  The path of the listener.
         * @param value The value being written.
         * @param buf   The buffer being written to.
         */
        @SuppressWarnings("unchecked")
        static <V extends CodecProvider<? super V>> void writeItem(String path, V value, RegistryFriendlyByteBuf buf) {
            ifPresent(path, registry -> {
                ResourceLocation type = registry.codecs.getKey(value.getCodec());
                buf.writeResourceLocation(type);
                ((StreamCodec<RegistryFriendlyByteBuf, V>) registry.streamCodecs.get(type)).encode(buf, value);
            });
        }

        /**
         * Reads an item from the network, via the listener's codec.
         *
         * @param <V>  The type of item being read.
         * @param path The path of the listener.
         * @param buf  The buffer being read from.
         * @return An object of type V as deserialized from the network.
         */
        @SuppressWarnings("unchecked")
        static <V> V readItem(String path, RegistryFriendlyByteBuf buf) {
            var registry = SYNC_REGISTRY.get(path);
            if (registry == null) {
                throw new RuntimeException("Received sync packet for unknown registry!");
            }
            ResourceLocation type = buf.readResourceLocation();
            return ((StreamCodec<RegistryFriendlyByteBuf, V>) registry.streamCodecs.get(type)).decode(buf);
        }

        /**
         * Stages an item to a listener.
         *
         * @param <V>   The type of the item being staged.
         * @param path  The path of the listener.
         * @param value The object being staged.
         */
        @SuppressWarnings("unchecked")
        static <V> void acceptItem(String path, ResourceLocation key, V value) {
            ifPresent(path, registry -> ((Map<ResourceLocation, V>) registry.staged).put(key, value));
        }

        /**
         * Ends the sync for a specific listener.
         * This will delete current data, push staged data to live, and call the appropriate methods for reloading.
         *
         * @param path The path of the listener.
         * @implNote Only called on the logical client.
         */
        static void endSync(String path) {
            if (ServerLifecycleHooks.getCurrentServer() != null) {
                // On a singleplayer host, we have to re-register a copy of the original data instead of the synced data
                // since the synced data may not contain the "full" information from the server.
                ifPresent(path, DynamicRegistry::triggerClientsideReload);
            }
            else {
                ifPresent(path, DynamicRegistry::pushStagedToLive);
            }
            Placebo.LOGGER.info("Completed sync for {}", path);
        }

        /**
         * Executes an action if the specified path is present in the sync registry.
         */
        private static void ifPresent(String path, Consumer<DynamicRegistry<?>> consumer) {
            DynamicRegistry<?> value = SYNC_REGISTRY.get(path);
            if (value != null) {
                consumer.accept(value);
            }
        }

        private static void syncAll(OnDatapackSyncEvent e) {
            SYNC_REGISTRY.values().forEach(r -> r.sync(e));
        }
    }

    /**
     * Internal class to handle population of registry entries during data generation.
     */
    @ApiStatus.Internal
    public static class DataGenPopulator<R extends CodecProvider<? super R>> {

        private final DynamicRegistry<R> registry;

        private DataGenPopulator(DynamicRegistry<R> registry) {
            this.registry = registry;
        }

        private DataGenPopulator<R> start() {
            registry.beginReload();
            return this;
        }

        public DataGenPopulator<R> register(ResourceLocation id, R object) {
            registry.register(id, object);
            return this;
        }

        private DataGenPopulator<R> end() {
            registry.onReload();
            return this;
        }

        public static <R extends CodecProvider<? super R>> void runScoped(DynamicRegistry<R> registry, Consumer<DataGenPopulator<R>> consumer) {
            var populator = new DataGenPopulator<>(registry).start();
            consumer.accept(populator);
            populator.end();
        }

    }

}
