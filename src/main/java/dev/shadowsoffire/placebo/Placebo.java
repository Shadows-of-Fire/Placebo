package dev.shadowsoffire.placebo;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.commands.PlaceboCommand;
import dev.shadowsoffire.placebo.events.ResourceReloadEvent;
import dev.shadowsoffire.placebo.loot.StackLootEntry;
import dev.shadowsoffire.placebo.network.PayloadHelper;
import dev.shadowsoffire.placebo.payloads.ButtonClickPayload;
import dev.shadowsoffire.placebo.payloads.PatreonDisablePayload;
import dev.shadowsoffire.placebo.reload.ReloadListenerPayloads;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import dev.shadowsoffire.placebo.systems.mixes.MixRegistry;
import dev.shadowsoffire.placebo.systems.wanderer.WandererTradesRegistry;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import dev.shadowsoffire.placebo.util.PlaceboUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Placebo.MODID)
@SuppressWarnings("deprecation")
public class Placebo {

    public static final String MODID = "placebo";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public Placebo(IEventBus bus) {
        bus.register(this);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(this::serverReload);
        NeoForge.EVENT_BUS.addListener(this::serverStart);
        TextColor.NAMED_COLORS = new HashMap<>(TextColor.NAMED_COLORS);
        bus.addListener(TabFillingRegistry::fillTabs);
        bus.register(new PayloadHelper());
        NeoForge.EVENT_BUS.start(); // Startup the Neo bus as an experiment to see what kinds of things this breaks. We may do this in Neo at some point.
        PlaceboConfig.load();
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        PayloadHelper.registerPayload(new ButtonClickPayload.Provider());
        PayloadHelper.registerPayload(new PatreonDisablePayload.Provider());
        PayloadHelper.registerPayload(new ReloadListenerPayloads.Start.Provider());
        PayloadHelper.registerPayload(new ReloadListenerPayloads.Content.Provider<>());
        PayloadHelper.registerPayload(new ReloadListenerPayloads.End.Provider());
        e.enqueueWork(() -> {
            PlaceboUtil.registerCustomColor(GradientColor.RAINBOW);
        });
        GearSetRegistry.INSTANCE.registerToBus();
        WandererTradesRegistry.INSTANCE.registerToBus();
        MixRegistry.INSTANCE.registerToBus();
    }

    @SubscribeEvent
    public void register(RegisterEvent e) {
        e.register(Registries.LOOT_POOL_ENTRY_TYPE, helper -> {
            helper.register(loc("stack_entry"), StackLootEntry.CODEC);
        });
    }

    public void registerCommands(RegisterCommandsEvent e) {
        PlaceboCommand.register(e.getDispatcher(), e.getBuildContext());
    }

    public void serverReload(AddServerReloadListenersEvent e) {
        e.addListener(loc("placebo_reload_event"), (ResourceManagerReloadListener) res -> NeoForge.EVENT_BUS.post(new ResourceReloadEvent(res, LogicalSide.SERVER)));
    }

    public void serverStart(ServerAboutToStartEvent e) {
        MixRegistry.applyMixes();
    }

    public static Identifier loc(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

}
