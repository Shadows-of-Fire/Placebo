package dev.shadowsoffire.placebo.json;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Primer for creating a {@link MobEffectInstance} with a random application chance and random amplifier.
 * <p>
 * Duration is determined by the caller when creating the real MobEffectInstance.
 */
public record ChancedEffectInstance(float chance, MobEffect effect, StepFunction amplifier, boolean ambient, boolean visible) {

    public static Codec<ChancedEffectInstance> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            ExtraCodecs.strictOptionalField(Codec.floatRange(0, 1), "chance", 1F).forGetter(ChancedEffectInstance::chance),
            BuiltInRegistries.MOB_EFFECT.byNameCodec().fieldOf("effect").forGetter(ChancedEffectInstance::effect),
            ExtraCodecs.strictOptionalField(StepFunction.CODEC, "amplifier", StepFunction.constant(0)).forGetter(ChancedEffectInstance::amplifier),
            ExtraCodecs.strictOptionalField(Codec.BOOL, "ambient", true).forGetter(ChancedEffectInstance::ambient),
            ExtraCodecs.strictOptionalField(Codec.BOOL, "visible", false).forGetter(ChancedEffectInstance::visible))
        .apply(inst, ChancedEffectInstance::new));

    /**
     * Special codec that makes the created effect instance deterministic.
     */
    public static Codec<ChancedEffectInstance> CONSTANT_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Codec.unit(1F).optionalFieldOf("chance", 1F).forGetter(a -> 1F),
            BuiltInRegistries.MOB_EFFECT.byNameCodec().fieldOf("effect").forGetter(ChancedEffectInstance::effect),
            ExtraCodecs.strictOptionalField(Codec.intRange(0, 255), "amplifier", 0).xmap(StepFunction::constant, sf -> (int) sf.min()).forGetter(ChancedEffectInstance::amplifier),
            ExtraCodecs.strictOptionalField(Codec.BOOL, "ambient", true).forGetter(ChancedEffectInstance::ambient),
            ExtraCodecs.strictOptionalField(Codec.BOOL, "visible", false).forGetter(ChancedEffectInstance::visible))
        .apply(inst, ChancedEffectInstance::new));

    public MobEffectInstance create(RandomSource rand, int duration) {
        return new MobEffectInstance(this.effect, duration, this.amplifier.getInt(rand.nextFloat()), this.ambient, this.visible);
    }

    public MobEffectInstance createDeterministic(int duration) {
        return new MobEffectInstance(this.effect, duration, this.amplifier.getInt(0), this.ambient, this.visible);
    }
}
