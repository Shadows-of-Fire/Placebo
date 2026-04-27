package dev.shadowsoffire.placebo.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import dev.shadowsoffire.placebo.mixin.DatagenModLoaderMixin;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * The Field Ordering Factory allows users to provide custom comparators for ordering fields during datagen.
 * <p>
 * We don't have a ton of context at the time we need to evaluate the ordering, but the file contents and the output path should be good enough.
 */
public interface FieldOrderingFactory {

    /**
     * Returns a comparator for ordering fields in a JSON object.
     * <p>
     * Alternatively, returns null to use the default ordering (by {@link DataProvider#FIXED_ORDER_FIELDS}).
     * <p>
     * When multiple factories are registered, the first non-null comparator will be used.
     *
     * @param json The JSON element being written (should be an object)
     * @param path The path the JSON is being written to
     * @return A comparator for ordering fields, or null to use the default ordering
     */
    @Nullable
    Comparator<String> getKeyComparator(JsonElement json, Path path);

    static void register(FieldOrderingFactory factory) {
        Objects.requireNonNull(factory, "Cannot register a null FieldOrderingFactory");
        Impl.FACTORIES.add(factory);
    }

    /**
     * Returns a factory that applies the ordering only to objects of the given type (based on the output path containing the type string).
     *
     * @param registryKey  The registry key for the type of object to reorder (e.g. "minecraft:recipe", "minecraft:advancement", etc)
     * @param orderBuilder A function that takes the base order map (a copy of {@link DataProvider#FIXED_ORDER_FIELDS}) and returns a modified map with the desired
     *                     ordering.
     */
    public static FieldOrderingFactory forType(Identifier registryKey, Consumer<Object2IntOpenHashMap<String>> orderBuilder) {
        return FilteredOrderingFactory.builder()
            .registries(registryKey)
            .orderMap(orderBuilder)
            .build();
    }

    public static FieldOrderingFactory forSubtypedObject(Identifier registryKey, String type, Consumer<Object2IntOpenHashMap<String>> orderBuilder) {
        return FilteredOrderingFactory.builder()
            .registries(registryKey)
            .objectSubtype(type)
            .orderMap(orderBuilder)
            .build();
    }

    public static class Impl {
        private static final List<FieldOrderingFactory> FACTORIES = new ArrayList<>();
        private static final Object INIT_LOCK = new Object();
        private static volatile boolean initialized = false;

        // Captured by DatagenModLoaderMixin at the start of each datagen run.
        @Nullable
        private static volatile Path packRoot = null;

        public static Comparator<String> getComparatorFor(JsonElement json, Path path) {
            ensureInitialized();
            for (FieldOrderingFactory factory : Impl.FACTORIES) {
                Comparator<String> comparator = factory.getKeyComparator(json, path);
                if (comparator != null) {
                    return comparator;
                }
            }
            return DataProvider.KEY_COMPARATOR;
        }

        /**
         * Records the datagen output root and pack layout, called from {@link DatagenModLoaderMixin} at the entry of {@code DatagenModLoader.begin}.
         * Runs once per datagen invocation, before any provider executes.
         */
        @ApiStatus.Internal
        public static void setPackRoot(Path root) {
            packRoot = root.toAbsolutePath().normalize();
        }

        /**
         * @return The datagen output root, or {@code null} if datagen has not been initialized via
         *         {@code DatagenModLoader.begin}. When non-null, paths passed to {@link #getComparatorFor}
         *         can be reliably stripped of the root before being decomposed into pack-type/namespace/path.
         */
        @Nullable
        public static Path getPackRoot() {
            return packRoot;
        }

        /**
         * Posts {@link RegisterFieldOrderingsEvent} the first time a comparator is requested. This can't
         * happen during {@link GatherDataEvent} because consumer mods running datagen are not required to include
         * Placebo in their {@code --mod} list, so Placebo's {@link GatherDataEvent} listener may never fire.
         * <p>
         * Saves run on concurrently on {@link Util#backgroundExecutor()} so we need a double-checked lock.
         */
        private static void ensureInitialized() {
            if (!initialized) {
                synchronized (INIT_LOCK) {
                    if (!initialized) {
                        ModLoader.postEvent(new RegisterFieldOrderingsEvent());
                        initialized = true;
                    }
                }
            }
        }
    }

}
