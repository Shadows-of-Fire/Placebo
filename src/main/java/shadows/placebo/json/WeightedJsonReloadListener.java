package shadows.placebo.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;

import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyed;

public abstract class WeightedJsonReloadListener<T extends TypeKeyed<T> & WeightedEntry> extends PlaceboJsonReloadListener<T> {

	protected final List<T> entries = new ArrayList<>();
	protected volatile int weight = 0;

	public WeightedJsonReloadListener(Logger logger, String path, boolean synced, boolean subtypes) {
		super(logger, path, synced, subtypes);
	}

	@Override
	protected void beginReload() {
		super.beginReload();
		this.entries.clear();
		this.weight = 0;
	}

	@Override
	protected void onReload() {
		super.onReload();
		this.entries.addAll(this.registry.values());
		this.weight = WeightedRandom.getTotalWeight(this.entries);
	}

	public T getRandomItem(Random rand) {
		return WeightedRandom.getRandomItem(rand, entries, weight).orElseThrow();
	}

}
