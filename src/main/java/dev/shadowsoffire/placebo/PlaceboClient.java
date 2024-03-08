package dev.shadowsoffire.placebo;

import dev.shadowsoffire.placebo.events.ResourceReloadEvent;
import dev.shadowsoffire.placebo.patreon.TrailsManager;
import dev.shadowsoffire.placebo.patreon.WingsManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent.ClientTickEvent;
import net.neoforged.neoforge.event.TickEvent.Phase;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD, modid = Placebo.MODID)
public class PlaceboClient {

    public static long ticks = 0;

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e) {
        TrailsManager.init();
        WingsManager.init(e);
        NeoForge.EVENT_BUS.addListener(PlaceboClient::tick);
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(TrailsManager.TOGGLE);
        e.register(WingsManager.TOGGLE);
    }

    @SubscribeEvent
    public static void clientResource(RegisterClientReloadListenersEvent e) {
        e.registerReloadListener((ResourceManagerReloadListener) res -> NeoForge.EVENT_BUS.post(new ResourceReloadEvent(res, LogicalSide.CLIENT)));
    }

    public static void tick(ClientTickEvent e) {
        if (e.phase == Phase.END) {
            ticks++;
        }
    }

    public static float getColorTicks() {
        return (ticks + Minecraft.getInstance().getDeltaFrameTime()) / 0.5F;
    }
}
