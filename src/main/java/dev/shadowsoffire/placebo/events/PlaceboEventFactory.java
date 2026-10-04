package dev.shadowsoffire.placebo.events;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.EventListenerHelper;

public class PlaceboEventFactory {

    private static final class EnchantmentListeners {
        private static final int BUS_ID = findBusId();

        private static int findBusId() {
            if (MinecraftForge.EVENT_BUS.getClass() != EventBus.class) return -1;
            try {
                var field = EventBus.class.getDeclaredField("busID");
                field.setAccessible(true);
                return field.getInt(MinecraftForge.EVENT_BUS);
            } catch (ReflectiveOperationException | RuntimeException e) {
                return -1;
            }
        }

        private static boolean isEmpty() {
            return BUS_ID >= 0 && EventListenerHelper.getListenerList(GetEnchantmentLevelEvent.class).getListeners(BUS_ID).length == 0;
        }
    }


    public static InteractionResult onItemUse(ItemStack stack, UseOnContext ctx) {
        ItemUseEvent event = new ItemUseEvent(ctx);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return event.getCancellationResult();
        return null;
    }

    /**
     * Called from {@link IForgeItemStack#getEnchantmentLevel(Enchantment)}
     * Injected via coremods/get_ench_level_event_specific.js
     */
    public static int getEnchantmentLevelSpecific(int level, IForgeItemStack stack, Enchantment ench) {
        ItemStack itemStack = (ItemStack) stack;
        if (EnchantmentListeners.isEmpty()) return level;
        var map = new HashMap<Enchantment, Integer>();
        map.put(ench, level);
        var event = new GetEnchantmentLevelEvent(itemStack, map);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getEnchantments().get(ench);
    }

    /**
     * Called from {@link IForgeItemStack#getAllEnchantments()}
     * Injected via coremods/get_ench_level_event.js
     */
    public static Map<Enchantment, Integer> getEnchantmentLevel(Map<Enchantment, Integer> enchantments, IForgeItemStack stack) {
        enchantments = new HashMap<>(enchantments);
        ItemStack itemStack = (ItemStack) stack;
        if (EnchantmentListeners.isEmpty()) return enchantments;
        var event = new GetEnchantmentLevelEvent(itemStack, enchantments);
        MinecraftForge.EVENT_BUS.post(event);
        return enchantments;
    }
}
