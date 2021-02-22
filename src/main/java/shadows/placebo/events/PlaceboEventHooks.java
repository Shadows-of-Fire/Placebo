package shadows.placebo.events;

import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.CachedBlockInfo;
import net.minecraft.util.math.BlockPos;
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
		if (!ctx.getWorld().isRemote) return net.minecraftforge.common.ForgeHooks.onPlaceItemIntoWorld(ctx);
		return onItemUse(stack, ctx, c -> stack.getItem().onItemUse(ctx));
	}

	/**
	 * Vanilla (Patch) Copy :: {@link ItemStack#onItemUse(ItemUseContext, Function)}
	 */
	public static ActionResultType onItemUse(ItemStack stack, ItemUseContext ctx, Function<ItemUseContext, ActionResultType> callback) {
		PlayerEntity playerentity = ctx.getPlayer();
		BlockPos blockpos = ctx.getPos();
		CachedBlockInfo cachedblockinfo = new CachedBlockInfo(ctx.getWorld(), blockpos, false);
		if (playerentity != null && !playerentity.abilities.allowEdit && !stack.canPlaceOn(ctx.getWorld().getTags(), cachedblockinfo)) {
			return ActionResultType.PASS;
		} else {
			Item item = stack.getItem();
			ActionResultType actionresulttype = callback.apply(ctx);
			if (playerentity != null && actionresulttype == ActionResultType.SUCCESS) {
				playerentity.addStat(Stats.ITEM_USED.get(item));
			}

			return actionresulttype;
		}
	}

}
