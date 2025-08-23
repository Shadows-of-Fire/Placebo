package dev.shadowsoffire.placebo.datagen;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.data.DataProvider;

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

    public static void register(FieldOrderingFactory factory) {
        Objects.requireNonNull(factory, "Cannot register a null FieldOrderingFactory");
        Impl.FACTORIES.add(factory);
    }

    /**
     * Returns a factory that applies the ordering only to objects of the given type (based on the output path containing the type string).
     * 
     * @param objPath      The output path for the type of object to reorder (e.g. "recipe", "advancement", etc)
     * @param orderBuilder A function that takes the base order map (a copy of {@link DataProvider#FIXED_ORDER_FIELDS}) and returns a modified map with the desired
     *                     ordering.
     */
    public static FieldOrderingFactory forType(String objPath, Consumer<Object2IntOpenHashMap<String>> orderBuilder) {
        return FilteredOrderingFactory.builder()
            .forObjectPath(objPath)
            .orderMap(orderBuilder)
            .build();
    }

    public static FieldOrderingFactory forSubtypedObject(String objPath, String type, Consumer<Object2IntOpenHashMap<String>> orderBuilder) {
        return FilteredOrderingFactory.builder()
            .forObjectPath(objPath)
            .forObjectSubtype(type)
            .orderMap(orderBuilder)
            .build();
    }

    static class Impl {
        private static final List<FieldOrderingFactory> FACTORIES = new ArrayList<>();

        public static Comparator<String> getComparatorFor(JsonElement json, Path path) {
            for (FieldOrderingFactory factory : Impl.FACTORIES) {
                Comparator<String> comparator = factory.getKeyComparator(json, path);
                if (comparator != null) {
                    return comparator;
                }
            }
            return DataProvider.KEY_COMPARATOR;
        }
    }

}
