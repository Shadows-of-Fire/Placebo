package dev.shadowsoffire.placebo.patreon.wings;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

public interface IWingModel {
    public void render(PoseStack stack, MultiBufferSource buf, int packedLightIn, AbstractClientPlayer player, float partialTicks, ResourceLocation texture, PlayerModel<AbstractClientPlayer> model);
}
