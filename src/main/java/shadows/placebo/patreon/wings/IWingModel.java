package shadows.placebo.patreon.wings;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.ResourceLocation;

public interface IWingModel {
	public void render(MatrixStack stack, IRenderTypeBuffer buf, int packedLightIn, AbstractClientPlayerEntity player, float partialTicks, ResourceLocation texture, PlayerModel<AbstractClientPlayerEntity> model);
}
