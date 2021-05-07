package shadows.placebo.patreon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import shadows.placebo.Placebo;

@EventBusSubscriber(bus = Bus.MOD, modid = Placebo.MODID, value = Dist.CLIENT)
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
		SOUL_FIRE(() -> ParticleTypes.SOUL_FIRE_FLAME),
		FIRE(() -> ParticleTypes.FLAME),
		CAMPFIRE_SMOKE(() -> ParticleTypes.CAMPFIRE_COSY_SMOKE),
		CLOUD(() -> ParticleTypes.CLOUD),
		GROWTH(() -> ParticleTypes.HAPPY_VILLAGER),
		DMG_HEART(() -> ParticleTypes.DAMAGE_INDICATOR),
		HEART(() -> ParticleTypes.HEART),
		DRAGON_BREATH(() -> ParticleTypes.DRAGON_BREATH),
		END_ROD(() -> ParticleTypes.END_ROD),
		FIREWORK(() -> ParticleTypes.FIREWORK),
		SLIME(() -> ParticleTypes.ITEM_SLIME),
		SNOW(() -> ParticleTypes.ITEM_SNOWBALL),
		SOUL(() -> ParticleTypes.SOUL),
		WITCH(() -> ParticleTypes.WITCH);

		Supplier<IParticleData> type;

		TrailType(Supplier<IParticleData> type) {
			this.type = type;
		}
	}

	public static void playerTick(PlayerTickEvent e) {
		TrailType t = null;
		if (e.phase == Phase.END && e.player.world.isRemote && e.player.ticksExisted * 3 % 2 == 0 && (t = TRAILS.get(e.player.getUniqueID())) != null) {
			World world = e.player.world;
			PlayerEntity player = e.player;
			Random rand = world.rand;
			IParticleData type = t.type.get();
			world.addParticle(type, player.getPosX() + rand.nextDouble() * 0.4 - 0.2, player.getPosY() + 0.1, player.getPosZ() + rand.nextDouble() * 0.4 - 0.2, 0, 0, 0);
		}
	}

}
