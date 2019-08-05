package shadows.placebo.loot;

import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.ILootCondition;
import net.minecraft.world.storage.loot.functions.ILootFunction;

public class PoolBuilder extends LootPool.Builder {

	static int k = 0;

	public PoolBuilder(int minRolls, int maxRolls) {
		rolls(new RandomValueRange(minRolls, maxRolls));
		name("placebo_code_pool_" + k++);
	}

	public PoolBuilder addEntries(LootEntry... entries) {
		for (LootEntry e : entries)
			this.entries.add(e);
		return this;
	}

	public PoolBuilder addCondition(ILootCondition... conditions) {
		for (ILootCondition c : conditions)
			this.conditions.add(c);
		return this;
	}

	public PoolBuilder addFunc(ILootFunction... conditions) {
		for (ILootFunction c : conditions)
			this.functions.add(c);
		return this;
	}

}
