package dev.shadowsoffire.placebo.dynreg;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import dev.shadowsoffire.placebo.Placebo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

/**
 * Internal class for sync management.
 */
@ApiStatus.Internal
class SyncManagement {

    private static final Map<Identifier, DynamicRegistry<?>> SYNC_REGISTRY = new LinkedHashMap<>();

    /**
     * Registers a {@link DynamicRegistry} for syncing.
     *
     * @param listener The listener to register.
     * @throws UnsupportedOperationException if the listener is not a synced listener.
     * @throws UnsupportedOperationException if the listener is already registered to the sync registry.
     */
    static void registerForSync(DynamicRegistry<?> listener) {
        if (!listener.serializer.isSynced()) {
            throw new UnsupportedOperationException("Attempted to register the non-synced JSON Reload Listener " + listener.id + " as a synced listener!");
        }
        synchronized (SYNC_REGISTRY) {
            if (SYNC_REGISTRY.containsKey(listener.id)) {
                throw new UnsupportedOperationException("Attempted to register the JSON Reload Listener for syncing " + listener.id + " but one already exists!");
            }
            if (SYNC_REGISTRY.isEmpty()) {
                NeoForge.EVENT_BUS.addListener(SyncManagement::syncAll);
            }
            SYNC_REGISTRY.put(listener.id, listener);
        }
    }

    /**
     * Begins the sync for a specific listener.
     *
     * @param id The id of the listener being synced.
     */
    public static void initSync(Identifier id) {
        ifPresent(id, registry -> {
            registry.staged.clear();
            registry.stagedTags.clear();
        });
        Placebo.LOGGER.info("Starting sync for {}", id);
    }

    /**
     * Write an item (with the same type as the listener) to the network.
     *
     * @param <V>   The type of item being written.
     * @param id    The id of the listener.
     * @param value The value being written.
     * @param buf   The buffer being written to.
     */
    @SuppressWarnings("unchecked")
    public static <V> void writeItem(Identifier id, V value, RegistryFriendlyByteBuf buf) {
        ifPresent(id, registry -> {
            StreamCodec<RegistryFriendlyByteBuf, V> codec = (StreamCodec<RegistryFriendlyByteBuf, V>) registry.serializer.streamCodec();
            codec.encode(buf, value);
        });
    }

    /**
     * Reads an item from the network, via the listener's stream codec.
     *
     * @param <V> The type of item being read.
     * @param id  The id of the listener.
     * @param buf The buffer being read from.
     * @return An object of type V as deserialized from the network.
     */
    @SuppressWarnings("unchecked")
    public static <V> V readItem(Identifier id, RegistryFriendlyByteBuf buf) {
        var registry = SYNC_REGISTRY.get(id);
        if (registry == null) {
            throw new RuntimeException("Received sync packet for unknown registry: " + id);
        }
        return ((StreamCodec<RegistryFriendlyByteBuf, V>) registry.serializer.streamCodec()).decode(buf);
    }

    /**
     * Stages an item to a listener.
     *
     * @param <V>   The type of the item being staged.
     * @param id    The id of the listener.
     * @param key   The id of the entry being staged.
     * @param value The object being staged.
     */
    @SuppressWarnings("unchecked")
    public static <V> void acceptItem(Identifier id, Identifier key, V value) {
        ifPresent(id, registry -> ((Map<Identifier, V>) registry.staged).put(key, value));
    }

    /**
     * Stages the resolved tag map for a listener. Applied during {@link #endSync(Identifier)} via
     * {@link DynamicRegistry#bindTags}.
     *
     * @param id   The id of the listener.
     * @param tags The resolved tag map (tag id → list of entry ids).
     */
    public static void acceptTags(Identifier id, Map<Identifier, List<Identifier>> tags) {
        ifPresent(id, registry -> {
            registry.stagedTags.clear();
            registry.stagedTags.putAll(tags);
        });
    }

    /**
     * Ends the sync for a specific listener.
     * This will delete current data, push staged data to live, and call the appropriate methods for reloading.
     *
     * @param id The id of the listener.
     * @implNote Only called on the logical client.
     */
    public static void endSync(Identifier id) {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            // On a singleplayer host, we have to re-register a copy of the original data instead of the synced data
            // since the synced data may not contain the "full" information from the server.
            ifPresent(id, DynamicRegistry::processIntegratedClientReload);
        }
        else {
            ifPresent(id, DynamicRegistry::processDedicatedClientReload);
        }
        Placebo.LOGGER.info("Completed sync for {}", id);
    }

    /**
     * Executes an action if the specified id is present in the sync registry.
     */
    private static void ifPresent(Identifier id, Consumer<DynamicRegistry<?>> consumer) {
        DynamicRegistry<?> value = SYNC_REGISTRY.get(id);
        if (value != null) {
            consumer.accept(value);
        }
    }

    private static void syncAll(OnDatapackSyncEvent e) {
        SYNC_REGISTRY.values().forEach(r -> r.sync(e));
    }
}
