package dev.shadowsoffire.placebo.util;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Interpolator that allows for only returning "nice" stepped numbers.
 */
public record StepFunction(float min, int steps, float step, float max) implements Float2FloatFunction {

    /**
     * Accepts a fully defined step function with min, steps, and step values.
     * 
     * @deprecated Prefer {@link #BOUNDS_CODEC}.
     */
    @Deprecated
    public static final Codec<StepFunction> STRICT_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.FLOAT.fieldOf("min").forGetter(StepFunction::min),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("steps").forGetter(StepFunction::steps),
            Codec.FLOAT.fieldOf("step").forGetter(StepFunction::step))
        .apply(inst, StepFunction::new));

    /**
     * Accepts a bounds-style StepFunction, which defines a min / max and the step value.
     */
    public static final Codec<StepFunction> BOUNDS_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.FLOAT.fieldOf("min").forGetter(StepFunction::min),
            Codec.FLOAT.fieldOf("max").forGetter(StepFunction::max),
            Codec.FLOAT.optionalFieldOf("step", 0.01F).forGetter(StepFunction::step))
        .apply(inst, StepFunction::fromBounds));

    public static final Codec<StepFunction> TRANSITION_CODEC = Codec.either(STRICT_CODEC, BOUNDS_CODEC).xmap(Either::unwrap, Either::right);

    /**
     * Accepts a single float value that will produce a {@linkplain StepFunction#constant constant step function}.
     */
    public static final Codec<StepFunction> CONSTANT_CODEC = Codec.FLOAT.xmap(StepFunction::constant, StepFunction::min);

    /**
     * Either codec between {@link STRICT_CODEC} and {@link CONSTANT_CODEC}, producing a step function from either a single float or the full definition.
     */
    public static final Codec<StepFunction> CODEC = Codec.either(CONSTANT_CODEC, TRANSITION_CODEC).xmap(Either::unwrap, StepFunction::toEither);

    @Deprecated
    public StepFunction(float min, int steps, float step) {
        this(min, steps, step, min + steps * step);
    }

    /**
     * Create a new StepFunction
     *
     * @param min   The min value
     * @param steps The max number of steps
     * @param step  The value per step
     */
    public StepFunction(float min, int steps, float step, float max) {
        this.min = min;
        this.steps = steps;
        this.step = step;
        this.max = max;
        Preconditions.checkArgument(steps > 0, "Steps must be a positive integer");
        Preconditions.checkArgument(Math.abs((min + steps * step) - max) <= 0.00001F, "Max value is out-of-sync with other fields.");
    }

    @Override
    public float get(float level) {
        return this.min + (int) (this.steps * (level + 0.5F / this.steps)) * this.step;
    }

    public int getInt(float level) {
        return (int) this.get(level);
    }

    /**
     * Returns the step number that the current level value corresponds to.<br>
     * Does not return a value higher than {@link steps()}, which is the max number of steps.
     */
    public int getStep(float level) {
        return (int) (this.steps * (level + 0.5F / this.steps));
    }

    public float getForStep(int step) {
        return this.min + this.step * step;
    }

    public float getIntForStep(int step) {
        return (int) this.getForStep(step);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(this.min);
        buf.writeInt(this.steps);
        buf.writeFloat(this.step);
    }

    public static StepFunction read(FriendlyByteBuf buf) {
        return new StepFunction(buf.readFloat(), buf.readInt(), buf.readFloat());
    }

    public static StepFunction fromBounds(float min, float max) {
        return fromBounds(min, max, 0.01F);
    }

    public static StepFunction fromBounds(float min, float max, float step) {
        if (min == max) {
            return constant(min);
        }

        int steps = Math.round((max - min) / step);
        if (Math.abs((min + step * steps) - max) > 0.0001F) {
            throw new UnsupportedOperationException("Failed to interpolate step function bounds with min=" + min + "; max=" + max + "; step=" + step
                + ". The step value must be a multiple of the difference between min and max.");
        }
        StepFunction function = new StepFunction(min, steps, step, max);
        return function;
    }

    public static StepFunction constant(float val) {
        return new StepFunction(val, 1, 0);
    }

    /**
     * Used by {@link #CODEC} to delegate the step function to {@link #CONSTANT_CODEC} or {@link #STRICT_CODEC} appropriately.
     * <p>
     * If it is detected the step function is constant (by {@link #step} being zero), it will be serialized as a single float.
     */
    private static Either<StepFunction, StepFunction> toEither(StepFunction function) {
        return function.step == 0 ? Either.left(function) : Either.right(function);
    }

}
