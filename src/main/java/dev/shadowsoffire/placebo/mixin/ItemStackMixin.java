package dev.shadowsoffire.placebo.mixin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.placebo.events.PlaceboEventFactory;
import dev.shadowsoffire.placebo.util.CachedObject;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

@Mixin(ItemStack.class)
public class ItemStackMixin implements CachedObjectSource {

    public Map<ResourceLocation, CachedObject<?>> cachedObjects = new ConcurrentHashMap<>();

    @Inject(at = @At("HEAD"), method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", cancellable = true)
    public void placebo_itemUseHook(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult itemUseEventRes = PlaceboEventFactory.onItemUse((ItemStack) (Object) this, ctx);
        if (itemUseEventRes != null) cir.setReturnValue(itemUseEventRes);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(ResourceLocation id, Function<ItemStack, T> deserializer, ToIntFunction<ItemStack> hasher) {
        var cachedObj = this.cachedObjects.computeIfAbsent(id, key -> new CachedObject<>(key, deserializer, hasher));
        return (T) cachedObj.get((ItemStack) (Object) this);
    }

}
