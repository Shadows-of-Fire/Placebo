package dev.shadowsoffire.placebo.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.shadowsoffire.placebo.recipe.RecipeHelper;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;

@Mixin(ReloadableServerResources.class)
public class ServerResourcesMixin {

    @Inject(method = "listeners()Ljava/util/List;", at = @At("RETURN"), cancellable = true)
    public void placebo_listeners(CallbackInfoReturnable<List<PreparableReloadListener>> ci) {
        List<PreparableReloadListener> listeners = new ArrayList<>(ci.getReturnValue());
        listeners.add(RecipeHelper.getReloader(((ReloadableServerResources) (Object) this).getRecipeManager()));
        ci.setReturnValue(listeners);
    }

}
