package dev.shadowsoffire.placebo.dynreg;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;

import dev.shadowsoffire.placebo.dynreg.WeightedDynamicRegistry.ILuckyWeighted;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.Level;

/**
 * An extension of {@link DynamicRegistry} with support for weighted entries, including various utilities for accessing items randomly.
 *
 * @param <V>
 */
public abstract class WeightedDynamicRegistry<V extends ILuckyWeighted> extends DynamicRegistry<V> {

    protected WeightedList<V> zeroLuckList = WeightedList.of();

    public WeightedDynamicRegistry(Logger logger, String path, RegistrySerializer<V> serializer) {
        super(logger, path, serializer);
    }

    @Override
    protected void beginReload(ReloadType type) {
        super.beginReload(type);
        this.zeroLuckList = WeightedList.of();
    }

    @Override
    protected void validateItem(Identifier key, V item) {
        super.validateItem(key, item);
        Preconditions.checkArgument(item.getQuality() >= 0, "Item may not have negative quality!");
        Preconditions.checkArgument(item.getWeight() >= 0, "Item may not have negative weight!");
    }

    @Override
    protected void onReload(ReloadType type) {
        super.onReload(type);
        WeightedList.Builder<V> builder = WeightedList.builder();
        for (V item : this.registry.values()) {
            if (item.getWeight() > 0) {
                builder.add(item, item.getWeight());
            }
        }
        this.zeroLuckList = builder.build();
    }

    /**
     * Gets a random item from this manager, ignoring luck.
     */
    @Nullable
    public V getRandomItem(RandomSource rand) {
        return this.getRandomItem(rand, 0);
    }

    /**
     * Gets a random item from this manager, re-calculating the weights based on luck.
     */
    @Nullable
    public V getRandomItem(RandomSource rand, float luck) {
        if (luck == 0) {
            return this.zeroLuckList.getRandom(rand).orElse(null);
        }
        return this.getRandomItem(rand, luck, Predicates.alwaysTrue());
    }

    /**
     * Gets a random item from this manager, re-calculating the weights based on luck and omitting items based on a filter.
     */
    @Nullable
    @SafeVarargs
    public final V getRandomItem(RandomSource rand, float luck, Predicate<V>... filters) {
        WeightedList.Builder<V> builder = WeightedList.builder();
        Stream<V> stream = this.registry.values().stream();
        for (Predicate<V> filter : filters) {
            stream = stream.filter(filter);
        }
        stream.forEach(item -> {
            int weight = Math.max(0, item.getWeight() + (int) (luck * item.getQuality()));
            if (weight > 0) {
                builder.add(item, weight);
            }
        });
        return builder.build().getRandom(rand).orElse(null);
    }

    /**
     * An item that will hold both a quality and a weight, for use with luck-based loot systems.
     * Luck increases the weight of an item by <quality> for each point of luck.
     */
    public static interface ILuckyWeighted {

        /**
         * @return The quality of this item. May not be negative.
         */
        public float getQuality();

        /**
         * @return The weight of this item. May not be negative.
         */
        public int getWeight();

        /**
         * Helper to wrap this object as a {@link Weighted} entry, with its weight adjusted by the given luck value.
         */
        @SuppressWarnings("unchecked")
        default <T extends ILuckyWeighted> Weighted<T> wrap(float luck) {
            return wrap((T) this, luck);
        }

        /**
         * Static (and more generic-safe) variant of {@link ILuckyWeighted#wrap(float)}
         */
        static <T extends ILuckyWeighted> Weighted<T> wrap(T item, float luck) {
            return new Weighted<>(item, Math.max(0, item.getWeight() + (int) (luck * item.getQuality())));
        }
    }

    /**
     * An item that is limited on a per-dimension basis.
     */
    public static interface IDimensional {

        /**
         * Null or empty means "all dimensions". To make an item invalid, return 0 weight.
         *
         * @return A set of the names of all dimensions this item is available in.
         */
        @Nullable
        Set<Identifier> getDimensions();

        /**
         * Creates a new predicate matching objects limited to the passed dimension.
         */
        public static <T extends IDimensional> Predicate<T> createPredicate(Identifier dimId) {
            return obj -> {
                Set<Identifier> dims = obj.getDimensions();
                return dims == null || dims.isEmpty() || dims.contains(dimId);
            };
        }

        public static <T extends IDimensional> Predicate<T> matches(Level level) {
            return createPredicate(level.dimension().identifier());
        }
    }

}
