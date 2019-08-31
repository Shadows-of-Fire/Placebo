package shadows.placebo.loot;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraft.world.storage.loot.conditions.SurvivesExplosion;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import shadows.placebo.Placebo;

public class LootSystem {

	public static final Map<ResourceLocation, LootTable> PLACEBO_TABLES = new HashMap<>();

	public static void registerLootTable(ResourceLocation key, LootTable table) {
		if (!PLACEBO_TABLES.containsKey(key)) PLACEBO_TABLES.put(key, table);
		else Placebo.LOGGER.warn("Duplicate loot entry detected, this is not allowed!  Key: " + key);
	}

	public static LootTable.Builder tableBuilder() {
		return new LootTable.Builder();
	}

	public static PoolBuilder poolBuilder(int minRolls, int maxRolls) {
		return new PoolBuilder(minRolls, maxRolls);
	}

	public static void defaultBlockTable(Block b) {
		LootTable.Builder builder = tableBuilder();
		builder.addLootPool(poolBuilder(1, 1).addEntries(new StackLootEntry(new ItemStack(b))).acceptCondition(SurvivesExplosion.builder()));
		registerLootTable(new ResourceLocation(b.getRegistryName().getNamespace(), "blocks/" + b.getRegistryName().getPath()), builder.build());
	}

	@SubscribeEvent
	public static void serverStart(FMLServerStartedEvent e) {
		SimpleReloadableResourceManager resMan = (SimpleReloadableResourceManager) e.getServer().getResourceManager();
		Reloader rel = new Reloader();
		for (int i = 0; i < resMan.reloadListeners.size(); i++) {
			if (resMan.reloadListeners.get(i) instanceof LootTableManager) {
				resMan.reloadListeners.add(i + 1, rel);
				break;
			}
		}
		rel.apply(PLACEBO_TABLES, null, null);
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
