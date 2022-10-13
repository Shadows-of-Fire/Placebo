package shadows.placebo;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import shadows.placebo.color.GradientColor;
import shadows.placebo.patreon.TrailsManager;
import shadows.placebo.patreon.WingsManager;
import shadows.placebo.util.PlaceboUtil;

@EventBusSubscriber(value = Dist.CLIENT, bus = Bus.MOD, modid = Placebo.MODID)
public class PlaceboClient {

	@SubscribeEvent
	public static void setup(FMLClientSetupEvent e) {
		TrailsManager.init();
		WingsManager.init(e);
		MinecraftForge.EVENT_BUS.addListener(PlaceboClient::tick);
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