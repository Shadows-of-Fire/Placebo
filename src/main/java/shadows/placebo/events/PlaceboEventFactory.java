package shadows.placebo.events;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.common.MinecraftForge;

public class PlaceboEventFactory {

	public static InteractionResult onItemUse(ItemStack stack, UseOnContext ctx) {
		ItemUseEvent event = new ItemUseEvent(ctx);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.isCanceled()) return event.getCancellationResult();
		return null;
	}

}
