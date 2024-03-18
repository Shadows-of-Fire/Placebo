package dev.shadowsoffire.placebo.systems.brewing;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.common.crafting.NBTIngredient;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/**
 * Thin wrapper around partial nbt ingredient with helpers for deserializing potion data.
 */
public class PotionIngredient extends NBTIngredient {

    public static final Codec<NBTIngredient> CODEC = RecordCodecBuilder.create(
        builder -> builder
            .group(
                NeoForgeExtraCodecs.singularOrPluralCodec(BuiltInRegistries.ITEM.byNameCodec(), "item").forGetter(NBTIngredient::getContainedItems),
                BuiltInRegistries.POTION.byNameCodec().xmap(PotionIngredient::toTag, PotionIngredient::fromTag).fieldOf("potion").forGetter(NBTIngredient::getTag),
                Codec.BOOL.optionalFieldOf("strict", false).forGetter(NBTIngredient::isStrict))
            .apply(builder, PotionIngredient::new));

    public static final Codec<NBTIngredient> CODEC_NONEMPTY = RecordCodecBuilder.create(
        builder -> builder
            .group(
                NeoForgeExtraCodecs.singularOrPluralCodecNotEmpty(BuiltInRegistries.ITEM.byNameCodec(), "item").forGetter(NBTIngredient::getContainedItems),
                BuiltInRegistries.POTION.byNameCodec().xmap(PotionIngredient::toTag, PotionIngredient::fromTag).fieldOf("potion").forGetter(NBTIngredient::getTag),
                Codec.BOOL.optionalFieldOf("strict", false).forGetter(NBTIngredient::isStrict))
            .apply(builder, PotionIngredient::new));

    protected PotionIngredient(Set<Item> items, CompoundTag potion, boolean strict) {
        super(items, potion, null, strict);
    }

    private static CompoundTag toTag(Potion potion) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Potion", BuiltInRegistries.POTION.getKey(potion).toString());
        return tag;
    }

    private static Potion fromTag(CompoundTag tag) {
        ResourceLocation id = new ResourceLocation(tag.getString("Potion"));
        return BuiltInRegistries.POTION.get(id);
    }

}
