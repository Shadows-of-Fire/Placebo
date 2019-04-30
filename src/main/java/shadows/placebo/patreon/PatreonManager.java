package shadows.placebo.patreon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import shadows.placebo.Placebo;

/**
 * Patreon Data Loader.  Loads from a file on the Placebo github.
 * Data uses a plaintext format, and the only rules to the format are that the first entry must be a number, then a space.
 * @author Shadows
 *
 */
@EventBusSubscriber(modid = Placebo.MODID, value = Side.CLIENT)
public class PatreonManager {

	public static final Int2ObjectMap<Consumer<String>> DATA_PARSERS = new Int2ObjectOpenHashMap<>();
	public static final Map<UUID, EnumParticleTypes> PATREONS = new HashMap<>();

	static {
		DATA_PARSERS.put(0, PatreonManager::parseV0Data);
	}

	@SubscribeEvent
	public static void init(ModelRegistryEvent e) {
		new Thread(() -> {
			Placebo.LOG.info("Loading patreon data...");
			try {
				URL url = new URL("https://raw.githubusercontent.com/Shadows-of-Fire/Placebo/master/PatreonInfo.txt");
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				while (reader.ready()) {
					String s = reader.readLine();
					String[] split = s.split(" ", 2);
					if (split.length != 2) {
						Placebo.LOG.error("Invalid patreon data entry {} will be ignored.", s);
						continue;
					}
					int dataVer = Integer.parseInt(split[0]);
					Consumer<String> func = DATA_PARSERS.get(dataVer);
					if (func == null) {
						Placebo.LOG.error("Invalid patreon data entry {} with invalid data version {} will be ignored.", s, dataVer);
						continue;
					}
					func.accept(split[1]);
				}
				reader.close();
			} catch (IOException ex) {
				Placebo.LOG.error("Exception loading patreon data!");
				ex.printStackTrace();
			}
		}, "Placebo Patreon Loader").start();
	}

	static void parseV0Data(String s) {
		String[] split = s.split(" ");
		if (split.length != 2) {
			Placebo.LOG.error("Invalid patreon data entry (version 0) {} will be ignored.", s);
			return;
		}
		UUID id = UUID.fromString(split[0]);
		EnumParticleTypes type = EnumParticleTypes.getByName(split[1]);
		PATREONS.put(id, type);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent e) {
		if (Placebo.patreonEnabled && e.player.world.isRemote) {
			Random rand = Minecraft.getMinecraft().world.rand;
			if (rand.nextInt(35) == 0) {
				EnumParticleTypes type = PATREONS.get(e.player.getGameProfile().getId());
				if (type == null) return;
				double x = e.player.prevPosX;
				double y = e.player.prevPosY;
				double z = e.player.prevPosZ;
				double velX = e.player.getHorizontalFacing().getOpposite().getDirectionVec().getX() * MathHelper.nextDouble(rand, 0.01, 0.2);
				double velY = MathHelper.nextDouble(rand, 0.03D, 0.15D);
				double velZ = e.player.getHorizontalFacing().getOpposite().getDirectionVec().getZ() * MathHelper.nextDouble(rand, 0.01, 0.2);
				Minecraft.getMinecraft().world.spawnParticle(type, true, x, y + 0.7, z, velX, velY, velZ);
			}
		}
	}

}
