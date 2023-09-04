package dev.shadowsoffire.placebo.reload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.ILuckyWeighted;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.Level;

/**
 * An extension of {@link DynamicRegistry} with support for weighted entries, including various utilities for accessing items randomly.
 *
 * @param <V>
 */
public abstract class WeightedDynamicRegistry<V extends CodecProvider<? super V> & ILuckyWeighted> extends DynamicRegistry<V> {

    protected List<Wrapper<V>> zeroLuckList = Collections.emptyList();
    protected int zeroLuckTotalWeight = 0;

    public WeightedDynamicRegistry(Logger logger, String path, boolean synced, boolean subtypes) {
        super(logger, path, synced, subtypes);
    }

    @Override
    protected void beginReload() {
        super.beginReload();
        this.zeroLuckList = Collections.emptyList();
        this.zeroLuckTotalWeight = 0;
    }

    @Override
    protected void validateItem(ResourceLocation key, V item) {
        super.validateItem(key, item);
        Preconditions.checkArgument(item.getQuality() >= 0, "Item may not have negative quality!");
        Preconditions.checkArgument(item.getWeight() >= 0, "Item may not have negative weight!");
    }

    @Override
    protected void onReload() {
        super.onReload();
        this.zeroLuckList = this.registry.values().stream().map(item -> WeightedEntry.wrap(item, item.getWeight())).toList();
        this.zeroLuckTotalWeight = WeightedRandom.getTotalWeight(this.zeroLuckList);
        if (this.zeroLuckTotalWeight <= 0) throw new RuntimeException("The total weight for the " + this.path + " manager is zero!  This is not allowed.");
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
        if (luck == 0) return WeightedRandom.getRandomItem(rand, this.zeroLuckList, this.zeroLuckTotalWeight).map(Wrapper::getData).orElse(null);
        return this.getRandomItem(rand, luck, Predicates.alwaysTrue());
    }

    /**
     * Gets a random item from this manager, re-calculating the weights based on luck and omitting items based on a filter.
     */
    @Nullable
    @SafeVarargs
    public final V getRandomItem(RandomSource rand, float luck, Predicate<V>... filters) {
        List<Wrapper<V>> list = new ArrayList<>(this.zeroLuckList.size());
        var stream = this.registry.values().stream();
        for (Predicate<V> filter : filters) {
            stream = stream.filter(filter);
        }
        stream.map(l -> l.<V>wrap(luck)).forEach(list::add);
        return WeightedRandom.getRandomItem(rand, list).map(Wrapper::getData).orElse(null);
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
         * Helper to wrap this object as a WeightedEntry.
         */
        @SuppressWarnings("unchecked")
        default <T extends ILuckyWeighted> Wrapper<T> wrap(float luck) {
            return wrap((T) this, luck);
        }

        /**
         * Static (and more generic-safe) variant of {@link ILuckyWeighted#wrap(float)}
         */
        static <T extends ILuckyWeighted> Wrapper<T> wrap(T item, float luck) {
            return WeightedEntry.wrap(item, Math.max(0, item.getWeight() + (int) (luck * item.getQuality())));
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
        Set<ResourceLocation> getDimensions();

        public static <T extends IDimensional> Predicate<T> matches(ResourceLocation dimId) {
            return obj -> {
                Set<ResourceLocation> dims = obj.getDimensions();
                return dims == null || dims.isEmpty() || dims.contains(dimId);
            };
        }

        public static <T extends IDimensional> Predicate<T> matches(Level level) {
            return matches(level.dimension().location());
        }
    }

}
