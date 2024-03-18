package dev.shadowsoffire.placebo.systems.mixes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;

public class JsonMix<T> extends PotionBrewing.Mix<T> implements CodecProvider<JsonMix<?>> {

    public static Codec<JsonMix<?>> CODEC = PlaceboCodecs.enumCodec(Type.class).dispatch("mix_type", JsonMix::getMixType, Type::codec);

    protected final Type mixType;

    public JsonMix(T pFrom, Ingredient pIngredient, T pTo, Type mixType) {
        super(pFrom, pIngredient, pTo);
        this.mixType = mixType;
    }

    @Override
    public Codec<? extends JsonMix<?>> getCodec() {
        return CODEC;
    }

    public Type getMixType() {
        return this.mixType;
    }

    public static enum Type {
        CONTAINER(BuiltInRegistries.ITEM.byNameCodec()),
        POTION(BuiltInRegistries.POTION.byNameCodec());

        private final Codec<JsonMix<?>> codec;

        @SuppressWarnings({ "unchecked", "rawtypes" })
        private Type(Codec<?> elementCodec) {
            this.codec = (Codec) forType(this, elementCodec);
        }

        public Codec<JsonMix<?>> codec() {
            return this.codec;
        }

        private static <T> Codec<JsonMix<T>> forType(Type type, Codec<T> elementCodec) {
            return RecordCodecBuilder.create(inst -> inst
                .group(
                    elementCodec.fieldOf("from").forGetter(m -> m.from),
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(m -> m.ingredient),
                    elementCodec.fieldOf("to").forGetter(m -> m.to))
                .apply(inst, (from, ingredient, to) -> new JsonMix<>(from, ingredient, to, type))

            );

        }
    }

}
