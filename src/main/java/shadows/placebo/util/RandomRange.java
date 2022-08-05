package shadows.placebo.util;

import java.util.Random;

import net.minecraft.util.Mth;

public record RandomRange(double min, double max) {

	public int getInt(Random rand) {
		return Mth.nextInt(rand, (int) min, (int) max);
	}

	public float getFloat(Random rand) {
		return Mth.nextFloat(rand, (float) min, (float) max);
	}

	public double getDouble(Random rand) {
		return Mth.nextDouble(rand, min, max);
	}
}
