package dev.shadowsoffire.placebo.patreon.wings;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;

public interface IWingModel {
    /**
     * Submits the wing model for rendering during the feature submission phase.
     *
     * @param stack       The pose stack already positioned at the player's body.
     * @param collector   The feature submission collector from the render layer.
     * @param lightCoords Packed light coordinates for the player.
     * @param state       The avatar render state (with chest equipment, invisibility flag, age in ticks, etc.).
     * @param texture     The resolved wing texture.
     */
    void submit(PoseStack stack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, Identifier texture);
}
