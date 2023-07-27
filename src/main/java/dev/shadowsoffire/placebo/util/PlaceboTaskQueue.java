package dev.shadowsoffire.placebo.util;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.BooleanSupplier;

import org.apache.commons.lang3.tuple.Pair;

import dev.shadowsoffire.placebo.Placebo;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Placebo.MODID, bus = Bus.FORGE)
public class PlaceboTaskQueue {

    private static final Queue<Pair<String, BooleanSupplier>> TASKS = new ArrayDeque<>();

    @SubscribeEvent
    public static void tick(ServerTickEvent e) {
        if (e.phase == Phase.END) {
            Iterator<Pair<String, BooleanSupplier>> it = TASKS.iterator();
            Pair<String, BooleanSupplier> current = null;
            while (it.hasNext()) {
                current = it.next();
                try {
                    if (current.getRight().getAsBoolean()) it.remove();
                }
                catch (Exception ex) {
                    Placebo.LOGGER.error("An exception occurred while running a ticking task with ID {}.  It will be terminated.", current.getLeft());
                    it.remove();
                    ex.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent e) {
        TASKS.clear();
    }

    @SubscribeEvent
    public static void started(ServerStartedEvent e) {
        TASKS.clear();
    }

    public static void submitTask(String id, BooleanSupplier task) {
        TASKS.add(Pair.of(id, task));
    }

}
