package shadows.placebo.patreon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import shadows.placebo.Placebo;
import shadows.placebo.net.MessagePatreonDisable;
import shadows.placebo.patreon.PatreonUtils.WingType;
import shadows.placebo.patreon.wings.WingLayer;

@EventBusSubscriber(bus = Bus.MOD, modid = Placebo.MODID, value = Dist.CLIENT)
public class WingsManager {

	static Map<UUID, WingType> WINGS = new HashMap<>();

	public static final KeyBinding TOGGLE = new KeyBinding("placebo.toggleWings", GLFW.GLFW_KEY_KP_8, "key.categories.placebo");

	public static final Set<UUID> DISABLED = new HashSet<>();

	@SubscribeEvent
	public static void init(FMLClientSetupEvent e) {
		ClientRegistry.registerKeyBinding(TOGGLE);
		e.enqueueWork(() -> {
			Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().values().forEach(r -> {
				r.addLayer(new WingLayer(r));
			});
		});
		new Thread(() -> {
			Placebo.LOGGER.info("Loading patreon wing data...");
			try {
				URL url = new URL("https://raw.githubusercontent.com/Shadows-of-Fire/Placebo/1.16/PatreonWings.txt");
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
					String s;
					while ((s = reader.readLine()) != null) {
						String[] split = s.split(" ", 2);
						if (split.length != 2) {
							Placebo.LOGGER.error("Invalid patreon wing entry {} will be ignored.", s);
							continue;
						}
						WINGS.put(UUID.fromString(split[0]), WingType.valueOf(split[1]));
					}
					reader.close();
				} catch (IOException ex) {
					Placebo.LOGGER.error("Exception loading patreon wing data!");
					ex.printStackTrace();
				}
			} catch (Exception k) {
				//not possible
			}
			Placebo.LOGGER.info("Loaded {} patreon wings.", WINGS.size());
			if (WINGS.size() > 0) MinecraftForge.EVENT_BUS.addListener(WingsManager::clientTick);
		}, "Placebo Patreon Wing Loader").start();
	}

	public static void clientTick(ClientTickEvent e) {
		if (TOGGLE.consumeClick()) Placebo.CHANNEL.sendToServer(new MessagePatreonDisable(1));
	}

	public static WingType getType(UUID id) {
		return WINGS.get(id);
	}

}
