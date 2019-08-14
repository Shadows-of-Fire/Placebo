package shadows.placebo.trading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Provides a system for editing the wandering merchant trade lists.
 * @author Shadows
 *
 */
public class VillagerTradingManager {

	private static boolean run = false;

	public static void postEvents() {
		if (run) return;
		postWandererEvents();
		postVillagerEvents();
		run = true;
	}

	private static void postWandererEvents() {
		List<ITrade> randTrades = new ArrayList<>();
		List<ITrade> goodTrades = new ArrayList<>();
		randTrades.addAll(Arrays.asList(VillagerTrades.field_221240_b.get(1)));
		goodTrades.addAll(Arrays.asList(VillagerTrades.field_221240_b.get(2)));

		MinecraftForge.EVENT_BUS.post(new WandererTradesEvent(randTrades, TradeType.GENERIC));
		MinecraftForge.EVENT_BUS.post(new WandererTradesEvent(goodTrades, TradeType.RARE));

		VillagerTrades.field_221240_b.put(1, randTrades.toArray(new ITrade[0]));
		VillagerTrades.field_221240_b.put(2, goodTrades.toArray(new ITrade[0]));
	}

	private static void postVillagerEvents() {
		for (VillagerProfession prof : ForgeRegistries.PROFESSIONS) {
			Int2ObjectMap<ITrade[]> trades = VillagerTrades.field_221239_a.computeIfAbsent(prof, a -> new Int2ObjectOpenHashMap<>());
			Int2ObjectMap<List<ITrade>> mutableTrades = new Int2ObjectOpenHashMap<>();
			trades.int2ObjectEntrySet().forEach(e -> mutableTrades.put(e.getIntKey(), Lists.newArrayList(e.getValue())));
			for (int i = 1; i < 6; i++)
				mutableTrades.computeIfAbsent(i, e -> new ArrayList<>());
			MinecraftForge.EVENT_BUS.post(new VillagerTradesEvent(mutableTrades, prof));
			mutableTrades.int2ObjectEntrySet().forEach(e -> trades.put(e.getIntKey(), e.getValue().toArray(new ITrade[0])));
		}
	}

	/**
	 * Event class for editing Wandering Merchant trade lists.
	 * The wandering merchant picks a few trades from {@link TradeType#GENERIC} and a single trade from {@link TradeType#RARE}.
	 * @author Shadows
	 *
	 */
	public static class WandererTradesEvent extends Event {

		protected List<ITrade> trades;
		protected TradeType type;

		public WandererTradesEvent(List<ITrade> trades, TradeType type) {
			this.trades = trades;
			this.type = type;
		}

		public List<ITrade> getTrades() {
			return trades;
		}

		public TradeType getType() {
			return type;
		}

	}

	public enum TradeType {
		GENERIC,
		RARE;
	}

	/**
	 * Event class for editing Villager trade lists.
	 * Villagers pick two trades from their trade map, based on their level.
	 * Villager level is increased by successful trades.
	 * The maps are populated (by default) for levels 1-5 (inclusive).  Uncertain if levels past 5 can be reached.
	 * @author Shadows
	 *
	 */
	public static class VillagerTradesEvent extends Event {

		protected Int2ObjectMap<List<ITrade>> trades;
		protected VillagerProfession type;

		public VillagerTradesEvent(Int2ObjectMap<List<ITrade>> trades, VillagerProfession type) {
			this.trades = trades;
			this.type = type;
		}

		public Int2ObjectMap<List<ITrade>> getTrades() {
			return trades;
		}

		public VillagerProfession getType() {
			return type;
		}

	}
}
