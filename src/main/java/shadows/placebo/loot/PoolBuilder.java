package shadows.placebo.loot;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class PoolBuilder extends LootPool.Builder {

	static int k = 0;

	public PoolBuilder(int minRolls, int maxRolls) {
		this.setRolls(UniformGenerator.between(minRolls, maxRolls));
		this.name("placebo_code_pool_" + k++);
	}

	public PoolBuilder addEntries(LootPoolEntryContainer... entries) {
		for (LootPoolEntryContainer e : entries)
			this.entries.add(e);
		return this;
	}

	public PoolBuilder addCondition(LootItemCondition... conditions) {
		for (LootItemCondition c : conditions)
			this.conditions.add(c);
		return this;
	}

	public PoolBuilder addFunc(LootItemFunction... conditions) {
		for (LootItemFunction c : conditions)
			this.functions.add(c);
		return this;
	}

}
