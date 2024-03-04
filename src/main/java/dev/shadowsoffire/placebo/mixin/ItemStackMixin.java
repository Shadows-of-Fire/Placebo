package dev.shadowsoffire.placebo.mixin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.spongepowered.asm.mixin.Mixin;

import dev.shadowsoffire.placebo.util.CachedObject;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@Mixin(ItemStack.class)
public class ItemStackMixin implements CachedObjectSource {

    private volatile Map<ResourceLocation, CachedObject<?>> cachedObjects = null;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(ResourceLocation id, Function<ItemStack, T> deserializer, ToIntFunction<ItemStack> hasher) {
        var cachedObj = this.getOrCreate().computeIfAbsent(id, key -> new CachedObject<>(key, deserializer, hasher));
        return (T) cachedObj.get((ItemStack) (Object) this);
    }

    private Map<ResourceLocation, CachedObject<?>> getOrCreate() {
        if (this.cachedObjects == null) {
            synchronized (this) {
                if (this.cachedObjects == null) this.cachedObjects = new ConcurrentHashMap<>();
            }
        }
        return this.cachedObjects;
    }

}
