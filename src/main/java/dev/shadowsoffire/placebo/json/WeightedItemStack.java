package dev.shadowsoffire.placebo.json;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

/**
 * A Weighted ItemStack is a combination of an item stack with a weight and drop chance.
 * <p>
 * Primarily for use in {@link GearSet}.
 */
public class WeightedItemStack extends WeightedEntry.IntrusiveBase {

    public static final Codec<WeightedItemStack> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        ItemAdapter.CODEC.fieldOf("stack").forGetter(w -> w.stack),
        Weight.CODEC.fieldOf("weight").forGetter(w -> w.getWeight()),
        Codec.FLOAT.optionalFieldOf("drop_chance", -1F).forGetter(w -> w.dropChance))
        .apply(inst, WeightedItemStack::new));

    public static final Codec<List<WeightedItemStack>> LIST_CODEC = CODEC.listOf();

    final ItemStack stack;
    final float dropChance;

    public WeightedItemStack(ItemStack stack, Weight weight, float dropChance) {
        super(weight);
        this.stack = stack;
        this.dropChance = dropChance;
    }

    /**
     * Gets the stored stack. Must not be modified.
     */
    public ItemStack getStack() {
        return this.stack;
    }

    @Override
    public String toString() {
        return "Stack: " + this.stack.toString() + " @ Weight: " + this.getWeight().asInt();
    }

    public void apply(LivingEntity entity, EquipmentSlot slot) {
        entity.setItemSlot(slot, this.stack.copy());
        if (this.dropChance >= 0 && entity instanceof Mob mob) {
            mob.setDropChance(slot, this.dropChance);
        }
    }
}
