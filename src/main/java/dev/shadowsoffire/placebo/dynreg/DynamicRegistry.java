package dev.shadowsoffire.placebo.dynreg;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.slf4j.Logger;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import dev.shadowsoffire.placebo.dynreg.tag.DynamicHolderSet;
import dev.shadowsoffire.placebo.dynreg.tag.DynamicTagKey;
import dev.shadowsoffire.placebo.dynreg.tag.DynamicTagManager;
import dev.shadowsoffire.placebo.json.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.CodecException;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * A Dynamic Registry is a reload listener which acts like a registry. Unlike datapack registries, it can reload.
 * <p>
 * To utilize this class, subclass it, and provide the appropriate constructor parameters.<br>
 * Then, create a single static instance of it and keep it around.
 * <p>
 * The de/serialization strategy (codec, sync, subtype dispatch) is supplied as a {@link RegistrySerializer}.
 * Once constructed, registration to the event bus is performed via {@link #registerToBus()}.
 * From then on, loading of files, condition checks, network sync, and everything else is automatically handled.
 *
 * @param <R> The base type of objects stored in this registry.
 */
public abstract class DynamicRegistry<R> extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {

    /**
     * Global registry of all {@link DynamicRegistry} instances, keyed by their {@link #getId() id}.
     * <p>
     * Populated automatically during construction. Used by the tag manager to enumerate registries that need their
     * tag content loaded.
     */
    private static final Map<Identifier, DynamicRegistry<?>> ALL_REGISTRIES = new ConcurrentHashMap<>();

    protected final Logger logger;
    protected final Identifier id;
    protected final RegistrySerializer<R> serializer;
    protected final Codec<DynamicHolder<R>> holderCodec;

    @Nullable
    protected final StreamCodec<ByteBuf, DynamicHolder<R>> holderStreamCodec;

    /**
     * Interned tag set instances, keyed by tag id. Created lazily by {@link #getOrCreateTag(DynamicTagKey)} the first
     * time a codec or consumer references the tag. Bound during tag-manager apply, unbound during tag-manager begin.
     * <p>
     * Concurrent because codec decoding may run on prepare-phase threads while tag-manager apply runs on the main
     * thread.
     */
    private final Map<Identifier, DynamicHolderSet.Named<R>> tags = new ConcurrentHashMap<>();

    /**
     * Internal registry. Immutable when outside of the registration phase.
     * <p>
     * This map is cleared in {@link #beginReload(ReloadType)} and frozen in {@link #onReload(ReloadType)}.
     */
    protected BiMap<Identifier, R> registry = ImmutableBiMap.of();

    /**
     * Staged data used during the sync process. Discarded when running an integrated server.
     */
    final Map<Identifier, R> staged = new HashMap<>();

    /**
     * Staged tag data used during the sync process. Discarded when running an integrated server.
     */
    final Map<Identifier, List<Identifier>> stagedTags = new HashMap<>();

    /**
     * Map of all holders that have ever been requested for this registry.
     */
    private final Map<Identifier, DynamicHolder<R>> holders = new ConcurrentHashMap<>();

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
     * @param logger     The logger used by this listener for all relevant messages.
     * @param id         The namespaced id of this registry. Used for the data directory layout
     *                   ({@code data/<datapack-ns>/<id.namespace>/<id.path>/}), the tag directory
     *                   ({@code data/<datapack-ns>/tags/<id.namespace>/<id.path>/}), the reload-listener id,
     *                   and the network sync key.
     * @param serializer The serialization strategy for entries of this registry.
     * @apiNote After construction, {@link #registerToBus()} must be called during setup.
     */
    public DynamicRegistry(Logger logger, Identifier id, RegistrySerializer<R> serializer) {
        this.logger = logger;
        this.id = id;
        this.serializer = serializer;
        this.holderCodec = Identifier.CODEC.xmap(this::holder, DynamicHolder::getId);
        this.holderStreamCodec = serializer.isSynced() ? Identifier.STREAM_CODEC.map(this::holder, DynamicHolder::getId) : null;
        if (ALL_REGISTRIES.putIfAbsent(id, this) != null) {
            throw new IllegalStateException("Attempted to construct two DynamicRegistry instances with the same id: " + id);
        }
    }

    /**
     * Walks the datapack and parses raw JSON files from {@code data/<ns>/<id.namespace>/<id.path>/}, returning a map of
     * resource id → parsed JSON. Codec-based decoding is deferred to {@link #apply}.
     */
    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager manager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> result = new HashMap<>();
        FileToIdConverter lister = FileToIdConverter.json(this.id.getNamespace() + "/" + this.id.getPath());
        for (Map.Entry<Identifier, Resource> entry : lister.listMatchingResources(manager).entrySet()) {
            Identifier location = entry.getKey();
            Identifier entryId = lister.fileToId(location);
            try (var reader = entry.getValue().openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                result.put(entryId, json);
            }
            catch (JsonParseException | java.io.IOException e) {
                this.logger.error("Couldn't parse data file '{}' from '{}': {}", entryId, location, e);
            }
        }
        return result;
    }

    /**
     * Processes all the json entries through the registration chain. That registration chain is as follows:
     * <ol>
     * <li>Empty JSON check: Empty values are discarded with a warning message.</li>
     * <li>Condition check: Values that are conditionally disabled are ignored. A note is logged at the trace level.</li>
     * <li>Deserialization: Performed by {@link RegistrySerializer#codec()}.</li>
     * <li>Validation: Certain states of the object are checked for sanity.</li>
     * <li>Registration: The item is added to the {@link #registry}.</li>
     * </ol>
     * This parsing step has to happen on the main thread because dynamic registries may have dependencies on other dynamic registries, which will not be respected
     * when deserializing in prepare().
     */
    @Override
    protected final void apply(Map<Identifier, JsonElement> objects, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        this.beginReload(ReloadType.SERVER);
        ConditionalOps<JsonElement> ops = this.makeConditionalOps();
        Codec<R> codec = this.serializer.codec();
        objects.forEach((key, ele) -> {
            try {
                if (JsonUtil.checkAndLogEmpty(ele, key, this.id, this.logger) && JsonUtil.checkConditions(ele, key, this.id, this.logger, ops)) {
                    JsonObject obj = ele.getAsJsonObject();
                    R deserialized = codec.decode(ops, obj).getOrThrow(this::makeCodecException).getFirst();
                    this.register(key, deserialized);
                }
            }
            catch (Exception e) {
                this.logger.error("Failed parsing {} file {}.", this.id, key);
                this.logger.error("Underlying Exception: ", e);
            }
        });
        this.onReload(ReloadType.SERVER);
    }

    /**
     * Called when this manager begins reloading all items.
     * Should handle clearing internal data caches.
     *
     * @see {@link ReloadType} for information on the reload types.
     */
    @MustBeInvokedByOverriders
    protected void beginReload(ReloadType type) {
        this.callbacks.forEach(l -> l.beginReload(this));
        this.registry = new DynRegBiMap<>();
        this.holders.values().forEach(DynamicHolder::unbind);
        if (type != ReloadType.INTEGRATED_CLIENT) {
            // We need to hold onto the tags on the Integrated Client since the tag manager won't run again to re-bind them.
            this.tags.values().forEach(DynamicHolderSet.Named::unbind);
        }
    }

    /**
     * Called after this manager has finished reloading all items.
     * Should handle any info logging, and data immutability.
     *
     * @see {@link ReloadType} for information on the reload types.
     */
    @MustBeInvokedByOverriders
    protected void onReload(ReloadType type) {
        this.registry = Maps.unmodifiableBiMap(this.registry);
        this.logger.info("Registered {} {}.", this.registry.size(), this.id);
        this.callbacks.forEach(l -> l.onReload(this));
        this.holders.values().forEach(DynamicHolder::bind);
    }

    /**
     * @return An immutable view of all keys registered for this type.
     */
    public Set<Identifier> getKeys() {
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
    public R getValue(Identifier key) {
        return this.registry.get(key);
    }

    /**
     * @return The key associated with this value, or null.
     */
    @Nullable
    public Identifier getKey(R value) {
        return this.registry.inverse().get(value);
    }

    /**
     * @return The item associated with this key, or the default value.
     */
    public R getOrDefault(Identifier key, R defValue) {
        return this.registry.getOrDefault(key, defValue);
    }

    /**
     * Registers this listener to the event bus as is appropriate.
     * This should be called for ALL listeners from common setup.
     */
    public void registerToBus() {
        if (this.serializer.isSynced()) {
            SyncManagement.registerForSync(this);
        }
        NeoForge.EVENT_BUS.addListener(this::addReloader);
    }

    /**
     * Creates a {@link DynamicHolder} pointing to a value stored in this reload listener.
     *
     * @param id The ID of the target value.
     * @return A dynamic registry object pointing to the target value.
     */
    public DynamicHolder<R> holder(@Nullable Identifier id) {
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
     * @see #holder(Identifier)
     */
    public DynamicHolder<R> holder(R value) {
        Identifier key = this.getKey(value);
        return this.holder(key == null ? DynamicHolder.EMPTY : key);
    }

    /**
     * Gets the empty {@link DynamicHolder}.
     *
     * @see #holder(Identifier)
     */
    public DynamicHolder<R> emptyHolder() {
        return this.holder(DynamicHolder.EMPTY);
    }

    /**
     * Returns a {@link Codec} that can handle {@link DynamicHolder}s for this registry.<br>
     * The serialized form is {@link Identifier}.
     *
     * @return The Dynamic Holder Codec for this registry.
     */
    public Codec<DynamicHolder<R>> holderCodec() {
        return this.holderCodec;
    }

    /**
     * Returns a {@link StreamCodec} that can handle {@link DynamicHolder}s for this registry.<br>
     * The dynamic holders will be transmitted as {@link Identifier}s using {@link Identifier#STREAM_CODEC}.
     *
     * @return The Dynamic Holder Stream Codec for this registry.
     * @throws UnsupportedOperationException if this is not a synced registry.
     */
    public StreamCodec<ByteBuf, DynamicHolder<R>> holderStreamCodec() {
        if (this.holderStreamCodec == null) {
            throw new UnsupportedOperationException("Cannot retrieve a stream codec for the non-synced DynamicRegistry: " + this.id);
        }
        return this.holderStreamCodec;
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
     * @return The namespaced id of this registry.
     */
    public final Identifier getId() {
        return this.id;
    }

    /**
     * Returns the logger used by this registry. Exposed for tag-loading and similar machinery in adjacent packages.
     */
    public final Logger getLogger() {
        return this.logger;
    }

    /**
     * Returns the direct element codec, which can be used for de/serializing an element known by this registry.
     */
    public final Codec<R> elementCodec() {
        return this.serializer.codec();
    }

    /**
     * Returns the interned {@link DynamicHolderSet.Named} for the given tag key, creating it lazily if it does not
     * yet exist.
     * <p>
     * The returned set may be unbound (empty contents, {@link DynamicHolderSet.Named#isBound()} returns false) until
     * the tag manager binds tags during a reload. Codecs that decode tag references should call this method, which
     * gives them a stable {@link DynamicHolderSet.Named} reference that becomes populated when tags load.
     */
    public final DynamicHolderSet.Named<R> getOrCreateTag(DynamicTagKey<R> key) {
        return this.tags.computeIfAbsent(key.id(), id -> new DynamicHolderSet.Named<>(this, key));
    }

    /**
     * @return The bound holder set for the given tag, or empty if no tag with that id is currently bound. Unbound
     *         interned tag sets are treated as absent.
     */
    public final Optional<DynamicHolderSet.Named<R>> getTag(DynamicTagKey<R> key) {
        DynamicHolderSet.Named<R> set = this.tags.get(key.id());
        return set != null && set.isBound() ? Optional.of(set) : Optional.empty();
    }

    /**
     * Replaces the entire tag set with the given resolved map. Called by the tag manager during reload apply.
     * <p>
     * Tags present in the previous reload but absent from {@code resolved} are left interned but unbound — any
     * outstanding references continue to resolve, but are empty until the tag is re-declared.
     */
    @ApiStatus.Internal
    public final void bindTags(Map<Identifier, List<Identifier>> resolved) {
        for (Map.Entry<Identifier, List<Identifier>> entry : resolved.entrySet()) {
            DynamicTagKey<R> tagKey = DynamicTagKey.create(this, entry.getKey());
            DynamicHolderSet.Named<R> set = this.tags.computeIfAbsent(entry.getKey(), tagId -> new DynamicHolderSet.Named<>(this, tagKey));
            List<DynamicHolder<R>> holders = entry.getValue().stream().map(this::holder).toList();
            set.bind(holders);
        }
    }

    /**
     * Validates that every created {@link DynamicHolder} is bound to a registry entry.
     * <p>
     * This is primarily used as a sanity check in data generation.
     *
     * @throws RuntimeException if any unbound holders are detected.
     */
    public final void validateExistingHolders() {
        String error = "";
        for (DynamicHolder<R> holder : this.holders.values()) {
            if (!holder.isBound() && holder != this.emptyHolder()) {
                error += "Failed to validate dynamic holder %s for registry %s\n".formatted(holder.getId(), this.id);
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
    protected final void register(Identifier key, R value) {
        if (this.registry.containsKey(key)) {
            throw new UnsupportedOperationException("Attempted to register a " + this.id + " with a duplicate registry ID! Key: " + key);
        }
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
    protected void validateItem(Identifier key, R value) {}

    /**
     * Adds this reload listener to the {@link ReloadableServerResources}.
     * <p>
     * Also adds a dependency edge to {@link DynamicTagManager} so that tag loading runs after registry content has
     * been deserialized.
     */
    private void addReloader(AddServerReloadListenersEvent e) {
        e.addListener(this.id, this);
        e.addDependency(this.id, DynamicTagManager.ID);
    }

    /**
     * Replaces the contents of the live registry with the staging registry.<br>
     * This triggers the full reload process for the client.
     *
     * @implNote Not executed when hosting a singleplayer world, as it would replace the server data.
     */
    void processDedicatedClientReload() {
        this.beginReload(ReloadType.DEDICATED_CLIENT);
        this.staged.forEach(this::register);
        this.onReload(ReloadType.DEDICATED_CLIENT);
        this.bindTags(this.stagedTags);
    }

    /**
     * Performs a fake reload by making a copy of {@link #registry} and re-registering the original contents.
     * This triggers the full reload process for the client.
     *
     * @implNote This is used instead of {@link #processDedicatedClientReload()} for singleplayer hosts to avoid data loss.
     */
    void processIntegratedClientReload() {
        this.staged.clear();
        this.staged.putAll(this.registry);
        this.beginReload(ReloadType.INTEGRATED_CLIENT);
        this.staged.forEach(this::register);
        this.onReload(ReloadType.INTEGRATED_CLIENT);
    }

    private CodecException makeCodecException(String msg) {
        return new CodecException("Codec failure for type %s, message: %s".formatted(this.id, msg));
    }

    /**
     * @return The currently-bound tag content, mapping tag id → list of entry ids. Used by the sync flow to ship
     *         resolved tags to clients.
     */
    private Map<Identifier, List<Identifier>> exportTags() {
        Map<Identifier, List<Identifier>> result = new HashMap<>();
        for (DynamicHolderSet.Named<R> named : this.tags.values()) {
            if (named.isBound()) {
                result.put(named.key().id(), named.stream().map(DynamicHolder::getId).toList());
            }
        }
        return result;
    }

    /**
     * Sync event handler. Sends the start packet, a content packet for each item, a tag-sync packet
     * (if any tags are bound), and then the end packet.
     */
    void sync(OnDatapackSyncEvent e) {
        ServerPlayer player = e.getPlayer();
        Consumer<CustomPacketPayload> target = player == null ? PacketDistributor::sendToAllPlayers : payload -> PacketDistributor.sendToPlayer(player, payload);

        target.accept(new DynRegPayloads.Start(this.id));
        this.registry.forEach((k, v) -> {
            target.accept(new DynRegPayloads.Content<>(this.id, k, Either.left(v)));
        });
        Map<Identifier, List<Identifier>> exported = this.exportTags();
        if (!exported.isEmpty()) {
            target.accept(new TagSyncPayload(this.id, exported));
        }
        target.accept(new DynRegPayloads.End(this.id));
    }

    /**
     * @return An unmodifiable view of every constructed {@link DynamicRegistry} keyed by id.
     */
    public static Map<Identifier, DynamicRegistry<?>> allRegistries() {
        return Collections.unmodifiableMap(ALL_REGISTRIES);
    }

    /**
     * Looks up a registry by its {@link #getId() id}.
     */
    @Nullable
    public static DynamicRegistry<?> byId(Identifier id) {
        return ALL_REGISTRIES.get(id);
    }

    /**
     * Marker used to differentiate between reload types for calls to {@link #beginReload(ReloadType)} and {@link #onReload(ReloadType)}.
     */
    public static enum ReloadType {
        /**
         * The reload is being performed on the server during the apply phase of the reload listener.
         * All incoming objects are brand-new after being deserialized from JSON.
         */
        SERVER,

        /**
         * The reload is being performed on the client while playing on an integrated server.
         * In this case, the incoming objects are reused from the server, as the registry is a singleton.
         *
         * @apiNote If your objects are mutable, you should avoid re-applying any modifications already applied.
         */
        INTEGRATED_CLIENT,

        /**
         * The reload is being performed on the client while playing on a dedicated server.
         * All incoming objects are brand-new after being deserialized over the network.
         */
        DEDICATED_CLIENT;
    }

    /**
     * Internal class to handle population of registry entries during data generation.
     */
    @ApiStatus.Internal
    public static class DataGenPopulator<R> {

        private final DynamicRegistry<R> registry;

        private DataGenPopulator(DynamicRegistry<R> registry) {
            this.registry = registry;
        }

        private DataGenPopulator<R> start() {
            BiMap<Identifier, R> old = registry.registry;
            registry.beginReload(ReloadType.INTEGRATED_CLIENT);
            old.forEach(this::register);
            return this;
        }

        public DataGenPopulator<R> register(Identifier id, R object) {
            registry.registry.put(id, object);
            return this;
        }

        private DataGenPopulator<R> end() {
            registry.onReload(ReloadType.INTEGRATED_CLIENT);
            return this;
        }

        public static <R> void runScoped(DynamicRegistry<R> registry, Consumer<DataGenPopulator<R>> consumer) {
            var populator = new DataGenPopulator<>(registry).start();
            consumer.accept(populator);
            populator.end();
        }

    }

}
