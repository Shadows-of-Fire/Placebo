package shadows.placebo.patreon.wings;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import shadows.placebo.patreon.PatreonUtils.WingType;
import shadows.placebo.patreon.WingsManager;

public class WingLayer extends LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {

	public WingLayer(IEntityRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> playerModelIn) {
		super(playerModelIn);
	}

	@Override
	public void render(MatrixStack stack, IRenderTypeBuffer buf, int packedLightIn, AbstractClientPlayerEntity player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
		if (WingsManager.DISABLED.contains(player.getUUID())) return;
		WingType type = WingsManager.getType(player.getUUID());
		if (type != null) {
			stack.pushPose();
			stack.translate(0, type.yOffset, 0);
			type.model.get().render(stack, buf, packedLightIn, player, partialTicks, type.textureGetter.apply(player), this.getParentModel());
			stack.popPose();
		}
	}

}
