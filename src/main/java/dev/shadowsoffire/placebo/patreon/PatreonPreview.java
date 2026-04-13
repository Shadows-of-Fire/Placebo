package dev.shadowsoffire.placebo.patreon;

import java.util.Locale;

import org.apache.commons.lang3.text.WordUtils;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.patreon.PatreonUtils.PatreonParticleType;
import dev.shadowsoffire.placebo.patreon.PatreonUtils.WingType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@SuppressWarnings("deprecation")
@EventBusSubscriber(value = Dist.CLIENT, modid = Placebo.MODID)
public class PatreonPreview {

    public static final boolean PARTICLES = false;
    public static final boolean WINGS = false;

    private static int counter = 0;

    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post e) {
        Player player = e.getEntity();
        if (player.level().isClientSide()) {
            if (player.tickCount >= 200) {
                if (player.tickCount % 150 == 0) {
                    Minecraft mc = Minecraft.getInstance();
                    if (PARTICLES) {
                        PatreonParticleType[] arr = PatreonParticleType.values();
                        PatreonParticleType p = arr[counter++ % arr.length];
                        Component type = Component.literal(WordUtils.capitalize(p.name().toLowerCase(Locale.ROOT).replace('_', ' ')));
                        mc.gui.setTimes(0, 40, 20);
                        mc.gui.setSubtitle(type);
                        mc.gui.setTitle(Component.literal(""));
                        TrailsManager.TRAILS.put(player.getUUID(), p);
                    }
                    else if (WINGS) {
                        WingType[] arr = WingType.values();
                        WingType p = arr[counter++ % arr.length];
                        Component type = Component.literal(WordUtils.capitalize(p.name().toLowerCase(Locale.ROOT).replace('_', ' ')));
                        mc.gui.setTimes(0, 40, 20);
                        mc.gui.setSubtitle(type);
                        mc.gui.setTitle(Component.literal(""));
                        WingsManager.WINGS.put(player.getUUID(), p);
                    }
                }
            }
        }
    }

}
