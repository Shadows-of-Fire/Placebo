package shadows.placebo.loot;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import shadows.placebo.Placebo;

/**
 * In-Code LootTables.  This allows for the creation and automatic registration of tables without JSON files.
 * Tables are automatically ceded to JSON tables if any are present under the same name.
 */
public class LootSystem {

	/**
	 * All custom tables to be loaded into the game.
	 */
	public static final Map<ResourceLocation, LootTable> PLACEBO_TABLES = new HashMap<>();

	/**
	 * Registers a loot table.  Tables should be registered during the {@link FMLCommonSetupEvent}.
	 * However, tables will work as long as they are registered before initial reload.
	 * @param key The name of the table.
	 * @param table The table instance.
	 */
	public static void registerLootTable(ResourceLocation key, LootTable table) {
		if (!PLACEBO_TABLES.containsKey(key)) {
			table.setLootTableId(key);
			PLACEBO_TABLES.put(key, table);
		} else Placebo.LOGGER.warn("Duplicate loot entry detected, this is not allowed!  Key: " + key);
	}

	/**
	 * Helper function to get a loot table builder.
	 */
	public static LootTable.Builder tableBuilder() {
		return new LootTable.Builder();
	}

	/**
	 * Creates a new {@link PoolBuilder} which is used to create {@link LootPool}s
	 */
	public static PoolBuilder poolBuilder(int minRolls, int maxRolls) {
		return new PoolBuilder(minRolls, maxRolls);
	}

	/**
	 * Automatically creates and registers a "default" block loot table.
	 */
	public static void defaultBlockTable(Block b) {
		LootTable.Builder builder = tableBuilder();
		builder.withPool(poolBuilder(1, 1).addEntries(new StackLootEntry(new ItemStack(b))).when(ExplosionCondition.survivesExplosion()));
		registerLootTable(new ResourceLocation(b.getRegistryName().getNamespace(), "blocks/" + b.getRegistryName().getPath()), builder.build());
	}

}
