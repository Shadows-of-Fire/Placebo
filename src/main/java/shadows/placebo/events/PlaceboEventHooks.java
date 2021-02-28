package shadows.placebo.events;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;

public class PlaceboEventHooks {

	/**
	 * ASM Hook: Called from {@link ItemStack#onItemUse(ItemUseContext)}
	 * See coremods/item_use_hook.js
	 */
	public static ActionResultType onItemUse(ItemStack stack, ItemUseContext ctx) {
		ItemUseEvent event = new ItemUseEvent(ctx);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) return event.getCancellationResult();
		return null;
	}

	/**
	 * ASM Hook: Called from {@link LivingEntity#attackEntityFrom}
	 * See coremods/shield_block_hook.js
	 */
	public static float onShieldBlock(LivingEntity blocker, DamageSource source, float blocked) {
		ShieldBlockEvent e = new ShieldBlockEvent(blocker, source, blocked);
		MinecraftForge.EVENT_BUS.post(e);
		return e.getBlocked();
	}

}
