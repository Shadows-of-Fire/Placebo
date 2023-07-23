package shadows.placebo.screen;

import net.minecraft.util.Mth;

public class ScreenUtil {

    /**
     * Retrieves an interpolated height value, based on the fraction between current and max.
     */
    public static int getHeight(float height, int current, int max) {
        return Mth.ceil(height * current / max);
    }

}
