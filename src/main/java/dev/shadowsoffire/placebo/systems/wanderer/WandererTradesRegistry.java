package dev.shadowsoffire.placebo.systems.wanderer;

import java.util.ArrayList;
import java.util.List;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.PlaceboConfig;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

/**
 * Allows loading wandering trader trades from json.
 * <p>
 * Due to a limitation of {@link WandererTradesEvent}, reloading this registry requires restarting the server.
 */
@EventBusSubscriber(modid = Placebo.MODID, bus = Bus.FORGE)
public class WandererTradesRegistry extends DynamicRegistry<WandererTrade> {

    public static final WandererTradesRegistry INSTANCE = new WandererTradesRegistry();

    protected final List<ItemListing> normTrades = new ArrayList<>();
    protected final List<ItemListing> rareTrades = new ArrayList<>();

    public WandererTradesRegistry() {
        super(Placebo.LOGGER, "wanderer_trades", false, true);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Placebo.loc("basic_trade"), BasicWandererTrade.CODEC);
    }

    @Override
    protected void beginReload() {
        super.beginReload();
        this.normTrades.clear();
        this.rareTrades.clear();
    }

    @Override
    protected void onReload() {
        super.onReload();
        this.getValues().forEach(trade -> {
            if (trade.isRare()) this.rareTrades.add(trade);
            else this.normTrades.add(trade);
        });
    }

    @SubscribeEvent
    public static void replaceTrades(WandererTradesEvent e) {
        if (PlaceboConfig.clearWandererNormalTrades) e.getGenericTrades().clear();
        if (PlaceboConfig.clearWandererRareTrades) e.getRareTrades().clear();
        e.getGenericTrades().addAll(INSTANCE.normTrades);
        e.getRareTrades().addAll(INSTANCE.rareTrades);
    }

}
