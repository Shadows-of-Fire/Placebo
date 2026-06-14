package dev.shadowsoffire.placebo.json;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
 * @param operation The operation of the generated modifier.
 * @param value     The value range for the generated modifier.
 */
public record RandomAttributeModifier(Holder<Attribute> attribute, Operation operation, StepFunction value, Identifier modifierId) {

    public static Codec<RandomAttributeModifier> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(RandomAttributeModifier::attribute),
            Operation.CODEC.fieldOf("operation").forGetter(RandomAttributeModifier::operation),
            StepFunction.CODEC.fieldOf("value").forGetter(RandomAttributeModifier::value),
            Identifier.CODEC.fieldOf("modifier_id").forGetter(RandomAttributeModifier::modifierId))
        .apply(inst, RandomAttributeModifier::new));

    public static Codec<RandomAttributeModifier> CONSTANT_CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(RandomAttributeModifier::attribute),
            PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(RandomAttributeModifier::operation),
            StepFunction.CONSTANT_CODEC.fieldOf("value").forGetter(RandomAttributeModifier::value),
            Identifier.CODEC.fieldOf("modifier_id").forGetter(RandomAttributeModifier::modifierId))
        .apply(inst, RandomAttributeModifier::new));

    /**
     * Variant of {@link #create(RandomSource)} that uses a custom identifier.
     */
    public AttributeModifier create(Identifier id, RandomSource rand) {
        return new AttributeModifier(id, this.value.get(rand.nextFloat()), this.operation);
    }

    /**
     * Creates an {@link AttributeModifier} a randomly-selected value from the {@link #value} function.
     */
    public AttributeModifier create(RandomSource rand) {
        return create(this.modifierId, rand);
    }

    /**
     * Variant of {@link #createDeterministic()} that uses a custom identifier.
     */
    public AttributeModifier createDeterministic(Identifier id) {
        return new AttributeModifier(id, this.value.min(), this.operation);
    }

    /**
     * Creates a deterministic {@link AttributeModifier} using the minimum value of the {@link #value} function.
     */
    public AttributeModifier createDeterministic() {
        return createDeterministic(this.modifierId);
    }

    public void apply(Identifier id, RandomSource rand, LivingEntity entity) {
        if (entity == null) {
            throw new RuntimeException("Attempted to apply a random attribute modifier to a null entity!");
        }
        AttributeModifier modif = this.create(id, rand);
        AttributeInstance inst = entity.getAttribute(this.attribute);
        if (inst == null) {
            Placebo.LOGGER
                .trace(String.format("Attempted to apply a random attribute modifier to an entity (%s) that does not have that attribute (%s)!", EntityType.getKey(entity.getType()), this.attribute.unwrapKey().get()));
            return;
        }
        inst.addPermanentModifier(modif);
    }

}
