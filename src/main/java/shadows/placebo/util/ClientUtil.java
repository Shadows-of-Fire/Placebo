package shadows.placebo.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class ClientUtil {

	public static void colorBlit(PoseStack pMatrixStack, float pX, float pY, float pUOffset, float pVOffset, float pUWidth, float pVHeight, int color) {
		innerBlit(pMatrixStack, pX, pX + pUWidth, pY, pY + pVHeight, 0, pUWidth, pVHeight, pUOffset, pVOffset, 256, 256, color);
	}

	public static void colorBlit(PoseStack pMatrixStack, int pX, int pY, int pBlitOffset, int pWidth, int pHeight, TextureAtlasSprite pSprite, int color) {
		innerBlit(pMatrixStack.last().pose(), pX, pX + pWidth, pY, pY + pHeight, pBlitOffset, pSprite.getU0(), pSprite.getU1(), pSprite.getV0(), pSprite.getV1(), color);
	}

	public static void innerBlit(PoseStack pMatrixStack, float pX1, float pX2, float pY1, float pY2, int pBlitOffset, float pUWidth, float pVHeight, float pUOffset, float pVOffset, int pTextureWidth, int pTextureHeight, int color) {
		innerBlit(pMatrixStack.last().pose(), pX1, pX2, pY1, pY2, pBlitOffset, (pUOffset + 0.0F) / pTextureWidth, (pUOffset + pUWidth) / pTextureWidth, (pVOffset + 0.0F) / pTextureHeight, (pVOffset + pVHeight) / pTextureHeight, color);
	}

	@SuppressWarnings("deprecation")
	public static void innerBlit(Matrix4f pMatrix, float pX1, float pX2, float pY1, float pY2, int pBlitOffset, float pMinU, float pMaxU, float pMinV, float pMaxV, int color) {
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		int a = color >> 24 & 0xFF, r = color >> 16 & 0xFF, g = color >> 8 & 0xFF, b = color & 0xFF;
		if (a == 0) a = 255;
		bufferbuilder.vertex(pMatrix, pX1, pY2, pBlitOffset).color(r, g, b, a).uv(pMinU, pMaxV).endVertex();
		bufferbuilder.vertex(pMatrix, pX2, pY2, pBlitOffset).color(r, g, b, a).uv(pMaxU, pMaxV).endVertex();
		bufferbuilder.vertex(pMatrix, pX2, pY1, pBlitOffset).color(r, g, b, a).uv(pMaxU, pMinV).endVertex();
		bufferbuilder.vertex(pMatrix, pX1, pY1, pBlitOffset).color(r, g, b, a).uv(pMinU, pMinV).endVertex();
		BufferUploader.draw(bufferbuilder.end());
	}

}
