package shadows.placebo.events;

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
	 * 
	 * Injected via coremods/get_ench_level_event.js
	 */
	public static int getEnchantmentLevel(int level, IForgeItemStack stack, Enchantment ench) {
		var event = new GetEnchantmentLevelEvent((ItemStack) stack, ench, level);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getLevel();
	}
}
