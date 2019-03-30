package shadows.placebo.patreon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import shadows.placebo.Placebo;

@EventBusSubscriber(modid = Placebo.MODID, value = Side.CLIENT)
public class PatreonManager {

	public static final Map<UUID, EnumParticleTypes> PATREONS = new HashMap<>();

	@SubscribeEvent
	public static void init() {
		new Thread(() -> {
			Placebo.LOG.info("Loading patreon data...");
			try {
				URL url = new URL("https://github.com/Shadows-of-Fire/Placebo/blob/master/PatreonInfo.txt");
				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				while (reader.ready()) {
					String s = reader.readLine();
					String[] split = s.split(" ");
					if (split.length != 2) {
						Placebo.LOG.error("Invalid patreon data entry {} will be ignored.", s);
						continue;
					}
					UUID id = UUID.fromString(split[0]);
					EnumParticleTypes type = EnumParticleTypes.getByName(split[1]);
					PATREONS.put(id, type);
				}
				reader.close();
			} catch (IOException e) {
				Placebo.LOG.error("Exception loading patreon data!");
				e.printStackTrace();
			}
		}).start();
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent e) {
		if (e.player.world.isRemote) {
			EnumParticleTypes type = PATREONS.get(e.player.getGameProfile().getId());
			if (type == null) return;
			Random rand = Minecraft.getMinecraft().world.rand;
			if (rand.nextInt(35) == 0) {
				double x = e.player.prevPosX;
				double y = e.player.prevPosY;
				double z = e.player.prevPosZ;
				Minecraft.getMinecraft().world.spawnParticle(type, true, x, y + 0.7, z, MathHelper.nextDouble(rand, -0.05D, 0.05D), MathHelper.nextDouble(rand, 0.03D, 0.15D), MathHelper.nextDouble(rand, -0.05D, 0.05D));
			}
		}
	}

}
