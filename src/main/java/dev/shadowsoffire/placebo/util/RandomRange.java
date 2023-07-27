package dev.shadowsoffire.placebo.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public record RandomRange(double min, double max) {

    public int getInt(RandomSource rand) {
        return Mth.nextInt(rand, (int) this.min, (int) this.max);
    }

    public float getFloat(RandomSource rand) {
        return Mth.nextFloat(rand, (float) this.min, (float) this.max);
    }

    public double getDouble(RandomSource rand) {
        return Mth.nextDouble(rand, this.min, this.max);
    }
}
