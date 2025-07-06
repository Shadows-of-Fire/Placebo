package dev.shadowsoffire.placebo.util;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Interpolator that allows for only returning "nice" stepped numbers.
 */
public record StepFunction(float min, int steps, float step, float max) implements Float2FloatFunction {

    /**
     * Accepts a fully defined step function with min, steps, and step values.
     * 
     * @deprecated Prefer {@link #BOUNDS_CODEC}.
     */
    @Deprecated(forRemoval = true, since = "9.6.0")
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

    public static final StreamCodec<ByteBuf, StepFunction> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, StepFunction::min,
        ByteBufCodecs.INT, StepFunction::steps,
        ByteBufCodecs.FLOAT, StepFunction::step,
        StepFunction::new);

    /**
     * Legacy constructor that automatically calculates the max value based on the min, steps, and step values.
     * 
     * @deprecated Prefer {@link #fromBounds(float, float, float)} to create step functions. This constructor will be private in a future version.
     */
    @Deprecated(forRemoval = true, since = "9.6.0")
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

    /**
     * Returns the value for a given level. The expected input range is between [0, 1], where 0 corresponds to the minimum value and 1 corresponds to the maximum
     * value.
     * <p>
     * This function will allow scaling the value outside of the expected range, but other functions (such as {@link #getStep(float)}) will not.
     */
    @Override
    public float get(float level) {
        return this.min + (int) (this.steps * (level + 0.5F / this.steps)) * this.step;
    }

    public int getInt(float level) {
        return (int) this.get(level);
    }

    /**
     * Returns the step number that the current level value corresponds to.
     * <p>
     * Does not return a value higher than {@link steps()}, which is the max number of steps.
     */
    public int getStep(float level) {
        return (int) (this.steps * (level + 0.5F / this.steps));
    }

    /**
     * Returns the value for a given step.
     */
    public float getForStep(int step) {
        return this.min + this.step * step;
    }

    /**
     * Returns the integer value for a given step.
     */
    public float getIntForStep(int step) {
        return (int) this.getForStep(step);
    }

    /**
     * Returns true if the step function is constant, meaning it will always return the same value regardless of the input level.
     */
    public boolean isConstant() {
        return this.step == 0;
    }

    /**
     * @deprecated Use {@link #STREAM_CODEC}.
     */
    @Deprecated(forRemoval = true, since = "9.8.2")
    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(this.min);
        buf.writeInt(this.steps);
        buf.writeFloat(this.step);
    }

    /**
     * @deprecated Use {@link #STREAM_CODEC}.
     */
    @Deprecated(forRemoval = true, since = "9.8.2")
    public static StepFunction read(FriendlyByteBuf buf) {
        return new StepFunction(buf.readFloat(), buf.readInt(), buf.readFloat());
    }

    /**
     * Creates a step function from a minimum and maximum value, with a default step size of 0.01F.
     */
    public static StepFunction fromBounds(float min, float max) {
        return fromBounds(min, max, 0.01F);
    }

    /**
     * Creates a step function from a minimum and maximum value, with a specified step size.
     * <p>
     * The step value must be a multiple of the difference between min and max, otherwise an exception will be thrown.
     */
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

    /**
     * Creates a constant step function that always returns the same value.
     */
    public static StepFunction constant(float val) {
        return new StepFunction(val, 1, 0, val);
    }

    /**
     * Used by {@link #CODEC} to delegate the step function to {@link #CONSTANT_CODEC} or {@link #STRICT_CODEC} appropriately.
     * <p>
     * If the step function {@link #isConstant()}, it will be serialized as a single float.
     */
    private static Either<StepFunction, StepFunction> toEither(StepFunction function) {
        return function.isConstant() ? Either.left(function) : Either.right(function);
    }

}
