package shadows.placebo.util;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;

public class ClientUtil {

	public static boolean isHoldingShift() {
		return (InputMappings.isKeyDown(Minecraft.getInstance().func_228018_at_().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputMappings.isKeyDown(Minecraft.getInstance().func_228018_at_().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT));
	}

}
