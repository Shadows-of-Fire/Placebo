package dev.shadowsoffire.placebo.patreon.wings;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.placebo.patreon.WingsManager;
import dev.shadowsoffire.placebo.patreon.PatreonUtils.WingType;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class WingLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public WingLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> playerModelIn) {
        super(playerModelIn);
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource buf, int packedLightIn, AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
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
