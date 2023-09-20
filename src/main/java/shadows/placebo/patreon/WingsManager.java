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

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.EntityRenderersEvent.AddLayers;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import shadows.placebo.Placebo;
import shadows.placebo.packets.PatreonDisableMessage;
import shadows.placebo.patreon.PatreonUtils.WingType;
import shadows.placebo.patreon.wings.Wing;
import shadows.placebo.patreon.wings.WingLayer;

public class WingsManager {

	static Map<UUID, WingType> WINGS = new HashMap<>();
	public static final KeyMapping TOGGLE = new KeyMapping("placebo.toggleWings", GLFW.GLFW_KEY_KP_8, "key.categories.placebo");
	public static final Set<UUID> DISABLED = new HashSet<>();
	public static final ModelLayerLocation WING_LOC = new ModelLayerLocation(new ResourceLocation(Placebo.MODID, "wings"), "main");

	public static void init(FMLClientSetupEvent e) {
		e.enqueueWork(() -> {
			ForgeHooksClient.registerLayerDefinition(WING_LOC, Wing::createLayer);
		});
		FMLJavaModLoadingContext.get().getModEventBus().addListener(WingsManager::addLayers);
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
				// not possible
			}
			Placebo.LOGGER.info("Loaded {} patreon wings.", WINGS.size());
			if (WINGS.size() > 0) MinecraftForge.EVENT_BUS.register(WingsManager.class);
		}, "Placebo Patreon Wing Loader").start();
	}

	@SubscribeEvent
	public static void keys(InputEvent.Key e) {
		if (e.getAction() == InputConstants.PRESS && TOGGLE.matches(e.getKey(), e.getScanCode())) {
			Placebo.CHANNEL.sendToServer(new PatreonDisableMessage(1));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addLayers(AddLayers e) {
		Wing.INSTANCE = new Wing(e.getEntityModels().bakeLayer(WING_LOC));
		for (String s : e.getSkins()) {
			LivingEntityRenderer skin = e.getSkin(s);
			skin.addLayer(new WingLayer(skin));
		}
	}

	public static WingType getType(UUID id) {
		return WINGS.get(id);
	}

}
