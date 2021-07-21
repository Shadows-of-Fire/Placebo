package shadows.placebo.loot;

import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;

public class PoolBuilder extends LootPool.Builder {

	static int k = 0;

	public PoolBuilder(int minRolls, int maxRolls) {
		this.setRolls(new RandomValueRange(minRolls, maxRolls));
		this.name("placebo_code_pool_" + k++);
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
