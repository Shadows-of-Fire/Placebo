package shadows.placebo.patreon.wings;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import shadows.placebo.patreon.PatreonUtils.WingType;
import shadows.placebo.patreon.WingsManager;

// Made with Blockbench 3.8.4
// Exported for Minecraft version 1.15 - 1.16
// Paste this class into your mod and generate all required imports

public class Wing extends EntityModel<AbstractClientPlayer> implements IWingModel {

    public static Wing INSTANCE;

    private final ModelPart bb_main;
    private final ModelPart cube_r1;
    private final ModelPart cube_r2;

    public Wing(ModelPart baked) {
        this.bb_main = baked.getChild("bb_main");
        this.cube_r1 = this.bb_main.getChild("cube_r1");
        this.cube_r2 = this.bb_main.getChild("cube_r2");
    }

    @Override
    public void setupAnim(AbstractClientPlayer entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}

    @Override
    public void renderToBuffer(PoseStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource buf, int packedLightIn, AbstractClientPlayer player, float partialTicks, ResourceLocation texture, PlayerModel<AbstractClientPlayer> model) {
        if (player.isInvisible()) return;
        WingType type = WingsManager.getType(player.getUUID());
        stack.translate(0, 0, 0.065);
        if (player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ArmorItem) stack.translate(0, 0, 0.075);
        stack.mulPose(Axis.YN.rotationDegrees(90));
        float rotationTime = player.tickCount % 40 + partialTicks;
        this.setRotationAngle(this.cube_r1, 0, 0.3491F * 1.5F + 0.3491F / 2 * (float) Math.sin(type.flapSpeed * Math.PI * rotationTime / 20), 0);
        this.setRotationAngle(this.cube_r2, 0, -(0.3491F * 1.5F + 0.3491F / 2 * (float) Math.sin(type.flapSpeed * Math.PI * rotationTime / 20)), 0);
        this.renderToBuffer(stack, buf.getBuffer(RenderType.entityTranslucent(texture)), packedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0), PartPose.offset(0.0F, 24.0F, 0.0F));
        partdefinition.getChild("bb_main").addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.0F, CubeDeformation.NONE.extend(0.001F)),
            PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0, .3491F, 0));
        partdefinition.getChild("bb_main").addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(1.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.0F, CubeDeformation.NONE.extend(0.001F)),
            PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0, -.3491F, 0));
        return LayerDefinition.create(meshdefinition, 32, 32);
    }
}
