package dev.shadowsoffire.placebo.events;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeItemStack;

public class PlaceboEventFactory {

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
        var map = new HashMap<Enchantment, Integer>();
        map.put(ench, level);
        var event = new GetEnchantmentLevelEvent((ItemStack) stack, map);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getEnchantments().get(ench);
    }

    /**
     * Called from {@link IForgeItemStack#getAllEnchantments()}
     * Injected via coremods/get_ench_level_event.js
     */
    public static Map<Enchantment, Integer> getEnchantmentLevel(Map<Enchantment, Integer> enchantments, IForgeItemStack stack) {
        enchantments = new HashMap<>(enchantments);
        var event = new GetEnchantmentLevelEvent((ItemStack) stack, enchantments);
        MinecraftForge.EVENT_BUS.post(event);
        return enchantments;
    }
}
