package shadows.placebo.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import shadows.placebo.statemap.ModelMapRegistry;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

	@Redirect(method = "processLoading", at = @At(value = "NEW", target = "net/minecraft/client/resources/model/ModelResourceLocation", ordinal = 0))
	private static ModelResourceLocation mapItem(ResourceLocation id, String variant) {
		return ModelMapRegistry.getMRL(new ModelResourceLocation(id, variant), id);
	}

}
