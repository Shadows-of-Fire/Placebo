package dev.shadowsoffire.placebo.json;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.systems.gear.GearSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * A Weighted ItemStack is a combination of an item stack with a weight and drop chance.
 * <p>
 * Primarily for use in {@link GearSet}.
 */
public record WeightedItemStack(Optional<ItemStackTemplate> stack, int weight, float dropChance) {

    public static final Codec<WeightedItemStack> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        OptionalTemplateCodec.INSTANCE.fieldOf("stack").forGetter(WeightedItemStack::stack),
        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weight").forGetter(WeightedItemStack::weight),
        Codec.FLOAT.optionalFieldOf("drop_chance", -1F).forGetter(WeightedItemStack::dropChance))
        .apply(inst, WeightedItemStack::new));

    public static final Codec<List<WeightedItemStack>> LIST_CODEC = CODEC.listOf();

    @Override
    public String toString() {
        return "Stack: " + this.stack.toString() + " @ Weight: " + this.weight;
    }

    public void apply(LivingEntity entity, EquipmentSlot slot) {
        entity.setItemSlot(slot, this.stack.map(ItemStackTemplate::create).orElse(ItemStack.EMPTY));
        if (this.dropChance >= 0 && entity instanceof Mob mob) {
            mob.setDropChance(slot, this.dropChance);
        }
    }
}
