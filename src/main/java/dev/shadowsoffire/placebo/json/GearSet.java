package dev.shadowsoffire.placebo.json;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.ILuckyWeighted;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/**
 * A Gear Set is a weighted object that holds a list of potential items for every equipment slot.<br>
 * When applying a Gear Set to an entity, it randomly selects an item for each slot and applies it.
 * <p>
 * The list of potentials for a slot may be empty.
 */
public record GearSet(int weight, float quality, List<WeightedItemStack> mainhands, List<WeightedItemStack> offhands, List<WeightedItemStack> boots, List<WeightedItemStack> leggings, List<WeightedItemStack> chestplates,
    List<WeightedItemStack> helmets, List<String> tags) implements CodecProvider<GearSet>, ILuckyWeighted {

    public static final Codec<GearSet> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.intRange(0, Integer.MAX_VALUE).fieldOf("weight").forGetter(ILuckyWeighted::getWeight),
        PlaceboCodecs.nullableField(Codec.floatRange(0, Float.MAX_VALUE), "quality", 0F).forGetter(ILuckyWeighted::getQuality),
        PlaceboCodecs.nullableField(WeightedItemStack.LIST_CODEC, "mainhands", Collections.emptyList()).forGetter(g -> g.mainhands),
        PlaceboCodecs.nullableField(WeightedItemStack.LIST_CODEC, "offhands", Collections.emptyList()).forGetter(g -> g.offhands),
        PlaceboCodecs.nullableField(WeightedItemStack.LIST_CODEC, "boots", Collections.emptyList()).forGetter(g -> g.boots),
        PlaceboCodecs.nullableField(WeightedItemStack.LIST_CODEC, "leggings", Collections.emptyList()).forGetter(g -> g.leggings),
        PlaceboCodecs.nullableField(WeightedItemStack.LIST_CODEC, "chestplates", Collections.emptyList()).forGetter(g -> g.chestplates),
        PlaceboCodecs.nullableField(WeightedItemStack.LIST_CODEC, "helmets", Collections.emptyList()).forGetter(g -> g.helmets),
        Codec.STRING.listOf().fieldOf("tags").forGetter(g -> g.tags))
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
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            WeightedRandom.getRandomItem(entity.getRandom(), this.getPotentials(slot)).ifPresent(s -> s.apply(entity, slot));
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
        };
    }

    @Override
    public Codec<? extends GearSet> getCodec() {
        return CODEC;
    }

    public static class SetPredicate implements Predicate<GearSet> {

        public static final Codec<SetPredicate> CODEC = ExtraCodecs.stringResolverCodec(s -> s.key, SetPredicate::new);

        protected final String key;
        protected final Predicate<GearSet> internal;

        public SetPredicate(String key) {
            this.key = key;
            if (key.startsWith("#")) {
                String tag = key.substring(1);
                this.internal = t -> t.tags.contains(tag);
            }
            else {
                ResourceLocation id = new ResourceLocation(key);
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
