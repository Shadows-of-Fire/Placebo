package dev.shadowsoffire.placebo.systems.gear;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.dynreg.WeightedDynamicRegistry.ILuckyWeighted;
import dev.shadowsoffire.placebo.json.WeightedItemStack;
import net.minecraft.resources.Identifier;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * A Gear Set is a weighted object that holds a list of potential items for every equipment slot.<br>
 * When applying a Gear Set to an entity, it randomly selects an item for each slot and applies it.
 * <p>
 * The list of potentials for a slot may be empty.
 * TODO: Think about splitting between Weapon Sets (hand items) and Armor Sets (helm/chest/legs/feet items) to allow for ease of combinations.
 */
public record GearSet(int weight, float quality, List<WeightedItemStack> mainhands, List<WeightedItemStack> offhands, List<WeightedItemStack> boots, List<WeightedItemStack> leggings, List<WeightedItemStack> chestplates,
    List<WeightedItemStack> helmets, Set<String> tags) implements CodecProvider<GearSet>, ILuckyWeighted {

    public static EquipmentSlot[] VALID_SLOTS = { EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD };

    public static final Codec<GearSet> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
        Codec.floatRange(0, Float.MAX_VALUE).optionalFieldOf("quality", 0F).forGetter(ILuckyWeighted::getQuality),
        WeightedItemStack.LIST_CODEC.optionalFieldOf("mainhands", Collections.emptyList()).forGetter(GearSet::mainhands),
        WeightedItemStack.LIST_CODEC.optionalFieldOf("offhands", Collections.emptyList()).forGetter(GearSet::offhands),
        WeightedItemStack.LIST_CODEC.optionalFieldOf("boots", Collections.emptyList()).forGetter(GearSet::boots),
        WeightedItemStack.LIST_CODEC.optionalFieldOf("leggings", Collections.emptyList()).forGetter(GearSet::leggings),
        WeightedItemStack.LIST_CODEC.optionalFieldOf("chestplates", Collections.emptyList()).forGetter(GearSet::chestplates),
        WeightedItemStack.LIST_CODEC.optionalFieldOf("helmets", Collections.emptyList()).forGetter(GearSet::helmets),
        PlaceboCodecs.setOf(Codec.STRING).fieldOf("tags").forGetter(GearSet::tags))
        .apply(inst, GearSet::new));

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public float getQuality() {
        return this.quality;
    }

    /**
     * Makes the entity wear this armor set. Returns the entity for convenience.
     */
    public LivingEntity apply(LivingEntity entity) {
        for (EquipmentSlot slot : VALID_SLOTS) {
            WeightedRandom.getRandomItem(entity.getRandom(), this.getPotentials(slot), WeightedItemStack::weight).ifPresent(s -> s.apply(entity, slot));
        }
        return entity;
    }

    public List<WeightedItemStack> getPotentials(EquipmentSlot slot) {
        return switch (slot) {
            case MAINHAND -> this.mainhands;
            case OFFHAND -> this.offhands;
            case FEET -> this.boots;
            case LEGS -> this.leggings;
            case CHEST -> this.chestplates;
            case HEAD -> this.helmets;
            case BODY, SADDLE -> throw new UnsupportedOperationException("Invalid slot type: " + slot);
        };
    }

    @Override
    public Codec<? extends GearSet> getCodec() {
        return CODEC;
    }

    public static class SetPredicate implements Predicate<GearSet> {

        public static final Codec<SetPredicate> CODEC = Codec.stringResolver(s -> s.key, SetPredicate::new);

        protected final String key;
        protected final Predicate<GearSet> internal;

        public SetPredicate(String key) {
            this.key = key;
            if (key.startsWith("#")) {
                String tag = key.substring(1);
                this.internal = t -> t.tags.contains(tag);
            }
            else {
                Identifier id = Identifier.parse(key);
                this.internal = t -> GearSetRegistry.INSTANCE.getKey(t).equals(id);
            }
        }

        @Override
        public boolean test(GearSet t) {
            return this.internal.test(t);
        }

        @Override
        public String toString() {
            return "SetPredicate[" + this.key + "]";
        }

    }

}
