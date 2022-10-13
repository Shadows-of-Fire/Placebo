package shadows.placebo.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public record RandomRange(double min, double max) {

	public int getInt(RandomSource rand) {
		return Mth.nextInt(rand, (int) min, (int) max);
	}

	public float getFloat(RandomSource rand) {
		return Mth.nextFloat(rand, (float) min, (float) max);
	}

	public double getDouble(RandomSource rand) {
		return Mth.nextDouble(rand, min, max);
	}
}
