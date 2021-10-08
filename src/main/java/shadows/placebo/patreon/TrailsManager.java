package shadows.placebo.patreon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import shadows.placebo.Placebo;
import shadows.placebo.net.MessagePatreonDisable;
import shadows.placebo.patreon.PatreonUtils.PatreonParticleType;

@EventBusSubscriber(bus = Bus.MOD, modid = Placebo.MODID, value = Dist.CLIENT)
public class TrailsManager {

	static Map<UUID, PatreonParticleType> TRAILS = new HashMap<>();

	public static final KeyBinding TOGGLE = new KeyBinding("placebo.toggleTrails", GLFW.GLFW_KEY_KP_9, "key.categories.placebo");

	public static final Set<UUID> DISABLED = new HashSet<>();

	@SubscribeEvent
	public static void init(FMLClientSetupEvent e) {
		ClientRegistry.registerKeyBinding(TOGGLE);
		new Thread(() -> {
			Placebo.LOGGER.info("Loading patreon trails data...");
			try {
				URL url = new URL("https://raw.githubusercontent.com/Shadows-of-Fire/Placebo/1.16/PatreonTrails.txt");
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
					String s;
					while ((s = reader.readLine()) != null) {
						String[] split = s.split(" ", 2);
						if (split.length != 2) {
							Placebo.LOGGER.error("Invalid patreon trail entry {} will be ignored.", s);
							continue;
						}
						TRAILS.put(UUID.fromString(split[0]), PatreonParticleType.valueOf(split[1]));
					}
					reader.close();
				} catch (IOException ex) {
					Placebo.LOGGER.error("Exception loading patreon trails data!");
					ex.printStackTrace();
				}
			} catch (Exception k) {
				//not possible
			}
			Placebo.LOGGER.info("Loaded {} patreon trails.", TRAILS.size());
			if (TRAILS.size() > 0) MinecraftForge.EVENT_BUS.addListener(TrailsManager::clientTick);
		}, "Placebo Patreon Trail Loader").start();
	}

	public static void clientTick(ClientTickEvent e) {
		if (TOGGLE.consumeClick()) Placebo.CHANNEL.sendToServer(new MessagePatreonDisable(0));
		PatreonParticleType t = null;
		if (e.phase == Phase.END && Minecraft.getInstance().level != null) {
			for (PlayerEntity player : Minecraft.getInstance().level.players()) {
				if (!player.isInvisible() && player.tickCount * 3 % 2 == 0 && !DISABLED.contains(player.getUUID()) && (t = TRAILS.get(player.getUUID())) != null) {
					ClientWorld world = (ClientWorld) player.level;
					Random rand = world.random;
					IParticleData type = t.type.get();
					world.addParticle(type, player.getX() + rand.nextDouble() * 0.4 - 0.2, player.getY() + 0.1, player.getZ() + rand.nextDouble() * 0.4 - 0.2, 0, 0, 0);
				}
			}
		}

	}
}
