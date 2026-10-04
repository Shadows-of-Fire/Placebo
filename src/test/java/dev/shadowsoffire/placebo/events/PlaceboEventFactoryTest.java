package dev.shadowsoffire.placebo.events;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class PlaceboEventFactoryTest {
    @BeforeClass
    public static void initializeUntransformedEventListenerList() {
        // Plain JUnit does not inject Forge's no-arg event constructor. Prime the
        // same listener list through Event's instance fallback instead.
        new GetEnchantmentLevelEvent(null, new HashMap<>()).getListenerList();
    }

    @Test
    public void returnsFreshMutableMapsWhenThereAreNoListeners() {
        Map<Enchantment, Integer> input = new HashMap<>();
        input.put(null, 2);
        Map<Enchantment, Integer> first = PlaceboEventFactory.getEnchantmentLevel(input, null);
        Map<Enchantment, Integer> second = PlaceboEventFactory.getEnchantmentLevel(input, null);
        assertEquals(input, first);
        assertNotSame(input, first);
        assertNotSame(first, second);
        first.put(null, 9);
        assertEquals(Integer.valueOf(2), input.get(null));
        assertEquals(Integer.valueOf(2), second.get(null));
    }

    @Test
    public void observesListenersAddedAndRemovedAfterTheFirstLookup() {
        MinecraftForge.EVENT_BUS.start();
        assertEquals(2, PlaceboEventFactory.getEnchantmentLevelSpecific(2, null, null));
        Consumer<GetEnchantmentLevelEvent> listener = event -> event.getEnchantments().put(null, 7);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, GetEnchantmentLevelEvent.class, listener);
        try {
            assertEquals(7, PlaceboEventFactory.getEnchantmentLevelSpecific(2, null, null));
            Map<Enchantment, Integer> input = new HashMap<>();
            input.put(null, 2);
            assertEquals(Integer.valueOf(7), PlaceboEventFactory.getEnchantmentLevel(input, null).get(null));
            assertEquals(Integer.valueOf(2), input.get(null));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        assertEquals(2, PlaceboEventFactory.getEnchantmentLevelSpecific(2, null, null));
    }

    @Test
    public void includesListenersRegisteredForTheEventSuperclass() {
        MinecraftForge.EVENT_BUS.start();
        Consumer<Event> listener = event -> {
            if (event instanceof GetEnchantmentLevelEvent enchantments)
                enchantments.getEnchantments().put(null, 11);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, Event.class, listener);
        try {
            assertEquals(11, PlaceboEventFactory.getEnchantmentLevelSpecific(2, null, null));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        assertEquals(2, PlaceboEventFactory.getEnchantmentLevelSpecific(2, null, null));
    }
}
