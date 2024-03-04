package dev.shadowsoffire.placebo.json;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

/**
 * Creates a Random Attribute Modifier. A UUID will be randomly generated.
 *
 * @param attribute The attribute the generated modifier will be applicable to.
 * @param op        The operation of the generated modifier.
 * @param value     The value range for the generated modifier.
 */
public record RandomAttributeModifier(Attribute attribute, Operation op, StepFunction value) {

    public static Codec<RandomAttributeModifier> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("attribute").forGetter(a -> a.attribute),
            PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(a -> a.op),
            StepFunction.CODEC.fieldOf("value").forGetter(a -> a.value))
        .apply(inst, RandomAttributeModifier::new));

    public static Codec<RandomAttributeModifier> CONSTANT_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            BuiltInRegistries.ATTRIBUTE.byNameCodec().fieldOf("attribute").forGetter(a -> a.attribute),
            PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(a -> a.op),
            StepFunction.CONSTANT_CODEC.fieldOf("value").forGetter(a -> a.value))
        .apply(inst, RandomAttributeModifier::new));

    public void apply(RandomSource rand, LivingEntity entity) {
        if (entity == null) throw new RuntimeException("Attempted to apply a random attribute modifier to a null entity!");
        AttributeModifier modif = this.create(rand);
        AttributeInstance inst = entity.getAttribute(this.attribute);
        if (inst == null) {
            Placebo.LOGGER
                .trace(String.format("Attempted to apply a random attribute modifier to an entity (%s) that does not have that attribute (%s)!", EntityType.getKey(entity.getType()), BuiltInRegistries.ATTRIBUTE.getKey(this.attribute)));
            return;
        }
        inst.addPermanentModifier(modif);
    }

    public AttributeModifier create(RandomSource rand) {
        return new AttributeModifier(UUID.randomUUID(), "placebo_random_modifier_" + this.attribute.getDescriptionId(), this.value.get(rand.nextFloat()), this.op);
    }

    public AttributeModifier create(String name, RandomSource rand) {
        return new AttributeModifier(name, this.value.get(rand.nextFloat()), this.op);
    }

    public AttributeModifier createDeterministic() {
        return new AttributeModifier(UUID.randomUUID(), "placebo_random_modifier_" + this.attribute.getDescriptionId(), this.value.min(), this.op);
    }

    public AttributeModifier createDeterministic(String name) {
        return new AttributeModifier(UUID.randomUUID(), name, this.value.min(), this.op);
    }

    public Attribute getAttribute() {
        return this.attribute;
    }

    public Operation getOp() {
        return this.op;
    }

    public StepFunction getValue() {
        return this.value;
    }
}
