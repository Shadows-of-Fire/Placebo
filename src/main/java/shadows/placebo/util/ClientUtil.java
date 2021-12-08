package shadows.placebo.util;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.platform.InputConstants;

public class ClientUtil {

	public static boolean isHoldingShift() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

}
