package shadows.placebo.loot;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import shadows.placebo.Placebo;

/**
 * In-Code LootTables.  This allows for the creation and automatic registration of tables without JSON files.
 * Tables are automatically ceded to JSON tables if any are present under the same name.
 */
public class LootSystem {

	/**
	 * All custom tables to be loaded into the game.
	 */
	private static final Map<ResourceLocation, LootTable> PLACEBO_TABLES = new HashMap<>();

	/**
	 * Registers a loot table.  Tables should be registered during the {@link FMLCommonSetupEvent}.
	 * However, tables will work as long as they are registered before initial reload.
	 * @param key The name of the table.
	 * @param table The table instance.
	 */
	public static void registerLootTable(ResourceLocation key, LootTable table) {
		if (!PLACEBO_TABLES.containsKey(key)) PLACEBO_TABLES.put(key, table);
		else Placebo.LOGGER.warn("Duplicate loot entry detected, this is not allowed!  Key: " + key);
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
		builder.addLootPool(poolBuilder(1, 1).addEntries(new StackLootEntry(new ItemStack(b))).acceptCondition(SurvivesExplosion.builder()));
		registerLootTable(new ResourceLocation(b.getRegistryName().getNamespace(), "blocks/" + b.getRegistryName().getPath()), builder.build());
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void serverStart(AddReloadListenerEvent e) {
		e.addListener(new Reloader());
	}

	private static class Reloader extends ReloadListener<Map<ResourceLocation, LootTable>> {

		@Override
		protected Map<ResourceLocation, LootTable> prepare(IResourceManager p_212854_1_, IProfiler p_212854_2_) {
			return PLACEBO_TABLES;
		}

		@Override
		protected void apply(Map<ResourceLocation, LootTable> tables, IResourceManager manager, IProfiler profiler) {
			LootTableManager mgr = ServerLifecycleHooks.getCurrentServer().getLootTableManager();
			mgr.registeredLootTables = new HashMap<>(mgr.registeredLootTables);
			tables.forEach((key, val) -> {
				if (!mgr.registeredLootTables.containsKey(key)) mgr.registeredLootTables.put(key, val);
			});
			Placebo.LOGGER.info("Registered {} additional loot tables.", tables.keySet().size());
		}
	}

}
