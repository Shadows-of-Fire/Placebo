package dev.shadowsoffire.placebo.patreon.wings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.shadowsoffire.placebo.patreon.PatreonUtils.WingType;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;

// Made with Blockbench 3.8.4
// Exported for Minecraft version 1.15 - 1.16
// Paste this class into your mod and generate all required imports

public class Wing extends EntityModel<AvatarRenderState> implements IWingModel {

    public static Wing INSTANCE;

    private final ModelPart bb_main;
    private final ModelPart cube_r1;
    private final ModelPart cube_r2;

    public Wing(ModelPart baked) {
        super(baked);
        this.bb_main = baked.getChild("bb_main");
        this.cube_r1 = this.bb_main.getChild("cube_r1");
        this.cube_r2 = this.bb_main.getChild("cube_r2");
    }

    @Override
    public void setupAnim(AvatarRenderState state) {
        super.setupAnim(state);
        // Flap animation derived from the render state's interpolated age in ticks.
        float rotationTime = state.ageInTicks;
        // 1.0 is the default flap speed; WingType-specific scaling is applied at submit time via a pose rotation.
        setRotationAngle(this.cube_r1, 0, 0.3491F * 1.5F + 0.3491F / 2 * (float) Math.sin(Math.PI * rotationTime / 20), 0);
        setRotationAngle(this.cube_r2, 0, -(0.3491F * 1.5F + 0.3491F / 2 * (float) Math.sin(Math.PI * rotationTime / 20)), 0);
    }

    @Override
    public void submit(PoseStack stack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, Identifier texture) {
        if (state.isInvisible) {
            return;
        }
        stack.pushPose();
        stack.translate(0, 0, 0.065);
        // Push the wings further back when the player is wearing chest equipment so they don't clip.
        if (state.chestEquipment.has(DataComponents.EQUIPPABLE)) {
            stack.translate(0, 0, 0.075);
        }
        stack.mulPose(Axis.YN.rotationDegrees(90));
        // Refresh the flap pose for this frame (the render framework may or may not have called setupAnim already).
        this.setupAnim(state);
        collector.submitModel(
            this,
            state,
            stack,
            RenderTypes.entityTranslucent(texture),
            lightCoords,
            OverlayTexture.NO_OVERLAY,
            0,
            null);
        stack.popPose();
    }

    public static void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0), PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.getChild("bb_main").addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.0F, CubeDeformation.NONE.extend(0.001F)),
            PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0, .3491F, 0));
        partdefinition.getChild("bb_main").addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.0F, CubeDeformation.NONE.extend(0.001F)),
            PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0, -.3491F, 0));
        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    /**
     * Retained compatibility entry point for the old interface shape. {@link WingType#model}
     * returns this object; existing type enum constructors still reference {@code Wing.INSTANCE}
     * without needing to know about the underlying render-state rework.
     */
    public void render(PoseStack stack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, Identifier texture, WingType type) {
        // yOffset and flap-speed adjustment are applied by the caller before invoking submit(...).
        this.submit(stack, collector, lightCoords, state, texture);
    }
}
