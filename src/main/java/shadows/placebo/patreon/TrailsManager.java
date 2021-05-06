package shadows.placebo.patreon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import shadows.placebo.Placebo;

@EventBusSubscriber(bus = Bus.MOD, modid = Placebo.MODID)
public class TrailsManager {

	private static Map<UUID, TrailType> TRAILS = new HashMap<>();

	@SubscribeEvent
	public static void init(FMLClientSetupEvent e) {
		new Thread(() -> {
			Placebo.LOGGER.info("Loading patreon data...");
			try {
				URL url = new URL("https://raw.githubusercontent.com/Shadows-of-Fire/Placebo/1.16/PatreonTrails.txt");
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
					String s;
					while ((s = reader.readLine()) != null) {
						String[] split = s.split(" ", 2);
						if (split.length != 2) {
							Placebo.LOGGER.error("Invalid patreon data entry {} will be ignored.", s);
							continue;
						}
						TRAILS.put(UUID.fromString(split[0]), TrailType.valueOf(split[1]));
					}
					reader.close();
				} catch (IOException ex) {
					Placebo.LOGGER.error("Exception loading patreon data!");
					ex.printStackTrace();
				}
			} catch (Exception k) {
				//not possible
			}
			Placebo.LOGGER.info("Loaded {} patreon trails.", TRAILS.size());
			if (TRAILS.size() > 0) MinecraftForge.EVENT_BUS.addListener(TrailsManager::playerTick);
		}, "Placebo Patreon Loader").start();
	}

	private static enum TrailType {
		SOUL_FIRE,
		FIRE
	}

	public static void playerTick(PlayerTickEvent e) {
		TrailType t = null;
		if (e.phase == Phase.END && e.player.world.isRemote && e.player.ticksExisted * 3 % 2 == 0 && (t = TRAILS.get(e.player.getUniqueID())) != null) {
			World world = e.player.world;
			PlayerEntity player = e.player;
			Random rand = world.rand;
			IParticleData type = t == TrailType.SOUL_FIRE ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
			world.addParticle(type, player.getPosX() + rand.nextDouble() * 0.4 - 0.2, player.getPosY(), player.getPosZ() + rand.nextDouble() * 0.4 - 0.2, 0, 0, 0);
		}
	}

}
