package dev.shadowsoffire.placebo.codec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

/**
 * Util class for codecs.
 */
public class PlaceboCodecs {

    /**
     * Creates a map-backed codec with a default codec to use as a fallback.
     *
     * @param <V>          The type being de/serialized.
     * @param name         The name of the type being de/serialized for error logging.
     * @param reg          The codec map.
     * @param defaultCodec The default codec to use if the deserialized object has no type field.
     * @return A codec backed by the provided map, that will fallback if necessary.
     */
    public static <T extends CodecProvider<T>> Codec<T> mapBackedDefaulted(String name, BiMap<ResourceLocation, Codec<? extends T>> reg, Codec<? extends T> defaultCodec) {
        return new MapBackedCodec<>(name, reg, () -> defaultCodec);
    }

    /**
     * Creates a map-backed codec. Deserialized objects must have a 'type' field declaring the target codec name.
     *
     * @param <V>  The type being de/serialized.
     * @param name The name of the type being de/serialized for error logging.
     * @param reg  The codec map.
     * @return A codec backed by the provided map.
     */
    public static <T extends CodecProvider<? super T>> Codec<T> mapBacked(String name, BiMap<ResourceLocation, Codec<? extends T>> reg) {
        return new MapBackedCodec<>(name, reg);
    }

    /**
     * Converts a codec into a set codec.
     */
    public static <T> Codec<Set<T>> setOf(Codec<T> elementCodec) {
        return setFromList(elementCodec.listOf());
    }

    /**
     * Converts a list codec into a set codec.
     */
    public static <T> Codec<Set<T>> setFromList(Codec<List<T>> listCodec) {
        return listCodec.<Set<T>>xmap(HashSet::new, ArrayList::new);
    }

    /**
     * Creates an enum codec using the lowercase name of the enum values as the keys.
     */
    public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> clazz) {
        return ExtraCodecs.stringResolverCodec(e -> e.name().toLowerCase(Locale.ROOT), name -> Enum.valueOf(clazz, name.toUpperCase(Locale.ROOT)));
    }

    /**
     * Creates a string resolver codec for a type implementing {@link StringRepresentable}.
     */
    public static <T extends StringRepresentable> Codec<T> stringResolver(Function<String, T> decoder) {
        return ExtraCodecs.stringResolverCodec(StringRepresentable::getSerializedName, decoder);
    }

    /**
     * Creates a nullable field codec for use in {@link RecordCodecBuilder}.
     * <p>
     * Used to avoid swallowing exceptions during parse errors.
     *
     * @deprecated Use {@link ExtraCodecs#strictOptionalField(Codec, String)}
     */
    @Deprecated
    public static <A> MapCodec<Optional<A>> nullableField(Codec<A> elementCodec, String name) {
        return ExtraCodecs.strictOptionalField(elementCodec, name);
    }

    /**
     * Creates a nullable field codec with the given default value for use in {@link RecordCodecBuilder}.
     * <p>
     * Used to avoid swallowing exceptions during parse errors.
     *
     * @deprecated Use {@link ExtraCodecs#strictOptionalField(Codec, String, Object)}
     */
    @Deprecated
    public static <A> MapCodec<A> nullableField(Codec<A> elementCodec, String name, A defaultValue) {
        return ExtraCodecs.strictOptionalField(elementCodec, name, defaultValue);
    }

}
