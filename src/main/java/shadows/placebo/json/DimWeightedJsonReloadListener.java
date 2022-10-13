package shadows.placebo.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.ServerLevelAccessor;
import shadows.placebo.json.DimWeightedJsonReloadListener.IDimWeighted;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyed;

public abstract class DimWeightedJsonReloadListener<V extends TypeKeyed<V> & IDimWeighted> extends WeightedJsonReloadListener<V> {

	public DimWeightedJsonReloadListener(Logger logger, String path, boolean synced, boolean subtypes) {
		super(logger, path, synced, subtypes);
	}

	/**
	 * Gets a random item from this manager that is valid for the dimension, while ignoring luck.
	 */
	@Nullable
	public V getRandomItem(RandomSource rand, ServerLevelAccessor level) {
		return getRandomItem(rand, level.getLevel().dimension().location());
	}

	/**
	 * Gets a random item from this manager that is valid for the dimension, while ignoring luck.
	 */
	@Nullable
	public V getRandomItem(RandomSource rand, ResourceLocation dimId) {
		return getRandomItem(rand, 0);
	}

	/**
	 * Gets a random item from this manager that is valid for the dimension, recomputing weights based on luck.
	 */
	@Nullable
	public V getRandomItem(RandomSource rand, float luck, ServerLevelAccessor level) {
		return getRandomItem(rand, luck, level.getLevel().dimension().location());
	}

	/**
	 * Gets a random item from this manager that is valid for the dimension, recomputing weights based on luck.
	 */
	@Nullable
	public V getRandomItem(RandomSource rand, float luck, ResourceLocation dimId) {
		List<Wrapper<V>> list = new ArrayList<>(zeroLuckList.size());
		this.registry.values().stream().filter(IDimWeighted.matches(dimId)).map(l -> l.<V>wrap(luck)).forEach(list::add);
		return WeightedRandom.getRandomItem(rand, list).map(Wrapper::getData).orElse(null);
	}

	/**
	 * An item that will hold both a quality and a weight, for use with luck-based loot systems.
	 * Luck increases the weight of an item by <quality> for each point of luck.
	 */
	public static interface IDimWeighted extends ILuckyWeighted {

		/**
		 * Null or empty means "all dimensions".  To make an item invalid, return 0 weight.
		 * @return A list of the names of all dimensions this item is available in.
		 */
		@Nullable
		Set<ResourceLocation> getDimensions();

		public static Predicate<IDimWeighted> matches(ResourceLocation dimId) {
			return obj -> {
				Set<ResourceLocation> dims = obj.getDimensions();
				return dims == null || dims.isEmpty() || dims.contains(dimId);
			};
		}

		public static Predicate<IDimWeighted> matches(ServerLevelAccessor level) {
			return matches(level.getLevel().dimension().location());
		}
	}
}
