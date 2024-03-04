package dev.shadowsoffire.placebo;

import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.patreon.TrailsManager;
import dev.shadowsoffire.placebo.patreon.WingsManager;
import dev.shadowsoffire.placebo.util.PlaceboUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod.EventBusSubscriber;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent.ClientTickEvent;
import net.neoforged.neoforge.event.TickEvent.Phase;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD, modid = Placebo.MODID)
public class PlaceboClient {

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

    /**
     * @see PlaceboUtil#registerCustomColor(String, TextColor)
     */
    @Deprecated(forRemoval = true)
    public static <T extends TextColor> void registerCustomColor(String id, T color) {
        PlaceboUtil.registerCustomColor(color);
    }

    public static long ticks = 0;

    public static void tick(ClientTickEvent e) {
        if (e.phase == Phase.END) {
            ticks++;
        }
    }

    public static float getColorTicks() {
        return (ticks + Minecraft.getInstance().getDeltaFrameTime()) / 0.5F;
    }

    @Deprecated(forRemoval = true)
    public static class RainbowColor extends GradientColor {

        public RainbowColor() {
            super(GradientColor.RAINBOW_GRADIENT, "rainbow");
        }
    }
}
