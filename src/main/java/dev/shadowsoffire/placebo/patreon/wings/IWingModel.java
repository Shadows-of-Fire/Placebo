package dev.shadowsoffire.placebo.patreon.wings;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.placebo.patreon.PatreonUtils.WingType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

public interface IWingModel {

    /**
     * Renders a set of wings on the given entity.
     *
     * @param stack  The pose stack, positioned at the top-center of the entity's torso. For a player, this is the model origin.
     * @param entity The entity wearing the wings.
     * @param type   The wing type being rendered, which supplies the texture, y-offset, and flap speed.
     */
    public void render(PoseStack stack, MultiBufferSource buf, int packedLightIn, int packedOverlayIn, LivingEntity entity, float partialTicks, WingType type);
}
