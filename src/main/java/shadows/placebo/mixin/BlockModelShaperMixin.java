package shadows.placebo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import shadows.placebo.statemap.ModelMapRegistry;

@Mixin(BlockModelShaper.class)
public class BlockModelShaperMixin {

	@Inject(method = "stateToModelLocation", at = @At(value = "RETURN"), cancellable = true)
	public static void stateToModelLocation(ResourceLocation pLocation, BlockState pState, CallbackInfoReturnable<ModelResourceLocation> ci) {
		ci.setReturnValue(ModelMapRegistry.getMRL(pState, ci.getReturnValue()));
	}

}
