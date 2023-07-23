package shadows.placebo.util;

import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Level Function that allows for only returning "nice" stepped numbers.
 */
public final class StepFunction implements Float2FloatFunction {

    // Formatter::off
    public static final Codec<StepFunction> STRICT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.FLOAT.fieldOf("min").forGetter(StepFunction::min),
        Codec.INT.fieldOf("steps").forGetter(StepFunction::steps),
        Codec.FLOAT.fieldOf("step").forGetter(StepFunction::step))
        .apply(inst, StepFunction::new));
    // Formatter::on

    /**
     * Accepts a float value, creating a constant step function, or a proper step function.
     */
    public static final Codec<StepFunction> CODEC = Codec.either(Codec.FLOAT, STRICT_CODEC).xmap(e -> e.map(StepFunction::constant, Function.identity()), Either::right);

    protected final float min;
    protected final int steps;
    protected final float step;

    /**
     * Create a new StepFunction
     *
     * @param min   The min value
     * @param steps The max number of steps
     * @param step  The value per step
     */
    public StepFunction(float min, int steps, float step) {
        this.min = min;
        this.steps = steps;
        this.step = step;
        Preconditions.checkArgument(steps > 0);
    }

    @Override
    public float get(float level) {
        return this.min + (int) (this.steps * (level + 0.5F / this.steps)) * this.step;
    }

    public int getInt(float level) {
        return (int) this.get(level);
    }

    public float min() {
        return this.min;
    }

    public int steps() {
        return this.steps;
    }

    public float step() {
        return this.step;
    }

    public float max() {
        return this.min + this.steps * this.step;
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

    public static StepFunction constant(float val) {
        return new StepFunction(val, 1, 0);
    }

}
