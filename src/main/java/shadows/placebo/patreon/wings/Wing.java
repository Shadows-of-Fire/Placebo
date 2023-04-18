package shadows.placebo.patreon.wings;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import shadows.placebo.patreon.PatreonUtils.WingType;
import shadows.placebo.patreon.WingsManager;

//Made with Blockbench 3.8.4
//Exported for Minecraft version 1.15 - 1.16
//Paste this class into your mod and generate all required imports

public class Wing extends EntityModel<AbstractClientPlayerEntity> implements IWingModel {

	public static final Wing INSTANCE = new Wing();

	private final ModelRenderer bb_main;
	private final ModelRenderer cube_r1;
	private final ModelRenderer cube_r2;

	public Wing() {
		this.texWidth = 32;
		this.texHeight = 32;

		this.bb_main = new ModelRenderer(this);
		this.bb_main.setPos(0.0F, 24.0F, 0.0F);

		this.cube_r1 = new ModelRenderer(this);
		this.cube_r1.setPos(0.0F, -8.0F, 0.0F);
		this.bb_main.addChild(this.cube_r1);
		this.setRotationAngle(this.cube_r1, 0.0F, 0.3491F, 0.0F);
		this.cube_r1.texOffs(0, 0).addBox(1.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.0F, 0.001F, false);

		this.cube_r2 = new ModelRenderer(this);
		this.cube_r2.setPos(0.0F, -8.0F, 0.0F);
		this.bb_main.addChild(this.cube_r2);
		this.setRotationAngle(this.cube_r2, 0.0F, -0.3491F, 0.0F);
		this.cube_r2.texOffs(0, 0).addBox(1.0F, -8.0F, 0.0F, 16.0F, 16.0F, 0.0F, 0.001F, false);
	}

	@Override
	public void setupAnim(AbstractClientPlayerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		this.bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void render(MatrixStack stack, IRenderTypeBuffer buf, int packedLightIn, AbstractClientPlayerEntity player, float partialTicks, ResourceLocation texture, PlayerModel<AbstractClientPlayerEntity> model) {
		if (player.isInvisible()) return;
		WingType type = WingsManager.getType(player.getUUID());
		stack.translate(0, 0, 0.065);
		if (player.getItemBySlot(EquipmentSlotType.CHEST).getItem() instanceof ArmorItem) stack.translate(0, 0, 0.075);
		stack.mulPose(Vector3f.YN.rotationDegrees(90));
		float rotationTime = player.tickCount % 40 + partialTicks;
		this.setRotationAngle(this.cube_r1, 0, 0.3491F * 1.5F + 0.3491F / 2 * (float) Math.sin(type.flapSpeed * Math.PI * rotationTime / 20), 0);
		this.setRotationAngle(this.cube_r2, 0, -(0.3491F * 1.5F + 0.3491F / 2 * (float) Math.sin(type.flapSpeed * Math.PI * rotationTime / 20)), 0);
		this.renderToBuffer(stack, buf.getBuffer(RenderType.entityTranslucent(texture)), packedLightIn, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.xRot = x;
		modelRenderer.yRot = y;
		modelRenderer.zRot = z;
	}
}