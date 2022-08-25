package shadows.placebo.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;

import com.google.common.base.Preconditions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyed;
import shadows.placebo.json.WeightedJsonReloadListener.ILuckyWeighted;

public abstract class WeightedJsonReloadListener<V extends TypeKeyed<V> & ILuckyWeighted> extends PlaceboJsonReloadListener<V> {

	protected final List<Wrapper<V>> zeroLuckList = new ArrayList<>();
	protected volatile int zeroLuckTotalWeight = 0;

	public WeightedJsonReloadListener(Logger logger, String path, boolean synced, boolean subtypes) {
		super(logger, path, synced, subtypes);
	}

	@Override
	protected void beginReload() {
		super.beginReload();
		this.zeroLuckList.clear();
		this.zeroLuckTotalWeight = 0;
	}

	protected <T extends V> void validateItem(T item) {
		super.validateItem(item);
		Preconditions.checkArgument(item.getQuality() >= 0, "Item may not have negative quality!");
		Preconditions.checkArgument(item.getWeight() >= 0, "Item may not have negative weight!");
	};

	@Override
	protected <T extends V> void register(ResourceLocation key, T item) {
		super.register(key, item);
		this.zeroLuckList.add(WeightedEntry.wrap(item, item.getWeight()));
	}

	@Override
	protected void onReload() {
		super.onReload();
		this.zeroLuckTotalWeight = WeightedRandom.getTotalWeight(this.zeroLuckList);
		if (this.zeroLuckTotalWeight <= 0) throw new RuntimeException("The total weight for the " + this.path + " manager is zero!  This is not allowed.");
	}

	/**
	 * Gets a random item from this manager, ignoring luck.
	 */
	public V getRandomItem(Random rand) {
		return getRandomItem(rand, 0);
	}

	/**
	 * Gets a random item from this manager, re-calculating the weights based on luck.
	 */
	public V getRandomItem(Random rand, float luck) {
		if (luck == 0) return WeightedRandom.getRandomItem(rand, zeroLuckList, zeroLuckTotalWeight).get().getData();
		else {
			List<Wrapper<V>> list = new ArrayList<>(zeroLuckList.size());
			this.registry.values().stream().map(l -> l.<V>wrap(luck)).forEach(list::add);
			return WeightedRandom.getRandomItem(rand, list).get().getData();
		}
	}

	/**
	 * An item that will hold both a quality and a weight, for use with luck-based loot systems.
	 * Luck increases the weight of an item by <quality> for each point of luck.
	 */
	public static interface ILuckyWeighted {

		/**
		 * @return The quality of this item.  May not be negative.
		 */
		public float getQuality();

		/**
		 * @return The weight of this item.  May not be negative.
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
			return WeightedEntry.wrap(item, item.getWeight() + (int) (luck * item.getQuality()));
		}
	}

}
