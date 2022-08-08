package shadows.placebo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import shadows.placebo.events.PlaceboEventFactory;

@Mixin(ItemStack.class)
public class ItemStackMixin {

	@Inject(at = @At("HEAD"), method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", cancellable = true)
	public void placebo_itemUseHook(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
		InteractionResult itemUseEventRes = PlaceboEventFactory.onItemUse((ItemStack) (Object) this, ctx);
		if (itemUseEventRes != null) cir.setReturnValue(itemUseEventRes);
	}

}
