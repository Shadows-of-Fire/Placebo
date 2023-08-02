package dev.shadowsoffire.placebo.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.google.common.collect.BiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.json.ItemAdapter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

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
        return new MapBackedCodec<>(name, reg, defaultCodec);
    }

    /**
     * Creates a map-backed codec. Deserialized objects must have a 'type' field declaring the target codec name.
     *
     * @param <V>  The type being de/serialized.
     * @param name The name of the type being de/serialized for error logging.
     * @param reg  The codec map.
     * @return A codec backed by the provided map.
     */
    public static <T extends CodecProvider<T>> Codec<T> mapBacked(String name, BiMap<ResourceLocation, Codec<? extends T>> reg) {
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
     * One-Way Ingredient codec. When initialliy deserializing, supports a mix of tag keys and item registry names.<br>
     * When serialzing, it will only serialize the actual item list, which will not include the original tag key.
     */
    public static class IngredientCodec implements Codec<Ingredient> {

        public static IngredientCodec INSTANCE = new IngredientCodec();

        private static Codec<List<ItemStack>> ITEM_LIST_CODEC = ItemAdapter.CODEC.listOf();

        @Override
        public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
            return ITEM_LIST_CODEC.encode(Arrays.asList(input.getItems()), ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
            JsonElement json = input instanceof JsonElement j ? j : ops.convertTo(JsonOps.INSTANCE, input);
            try {
                return DataResult.success(Pair.of(CraftingHelper.getIngredient(json, true), input));
            }
            catch (JsonSyntaxException ex) {
                return DataResult.error(ex::getMessage);
            }
        }

    }

    /**
     * Map backed codec with optional default functionality.
     * <p>
     * Serialized objects are expected to declare their serializer in the top-level 'type' key.
     *
     * @see PlaceboCodecs#mapBacked(String, BiMap)
     * @see PlaceboCodecs#mapBackedDefaulted(String, BiMap, Codec)
     */
    @ApiStatus.Internal
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static class MapBackedCodec<V extends CodecProvider<V>> implements Codec<V> {

        protected final String name;
        protected final BiMap<ResourceLocation, Codec<? extends V>> registry;
        @Nullable
        protected final Codec<? extends V> defaultCodec;

        public MapBackedCodec(String name, BiMap<ResourceLocation, Codec<? extends V>> registry, @Nullable Codec<? extends V> defaultCodec) {
            this.name = name;
            this.registry = registry;
            this.defaultCodec = defaultCodec;
        }

        public MapBackedCodec(String name, BiMap<ResourceLocation, Codec<? extends V>> registry) {
            this(name, registry, null);
        }

        @Override
        public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
            Optional<T> type = ops.get(input, "type").resultOrPartial(str -> {});
            Optional<ResourceLocation> key = type.map(t -> ResourceLocation.CODEC.decode(ops, t).resultOrPartial(Placebo.LOGGER::error).get().getFirst());

            Codec codec = key.<Codec>map(this.registry::get).orElse(this.defaultCodec);

            if (codec == null) {
                return DataResult.error(() -> "Failure when parsing a " + this.name + ". Unrecognized type: " + key.map(ResourceLocation::toString).orElse("null"));
            }
            return codec.decode(ops, input);
        }

        @Override
        public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
            Codec<V> codec = (Codec<V>) input.getCodec();
            ResourceLocation key = this.registry.inverse().get(codec);
            if (key == null) {
                return DataResult.error(() -> "Attempted to serialize an element of type " + this.name + " with an unregistered codec! Object: " + input);
            }
            T encodedKey = ResourceLocation.CODEC.encodeStart(ops, key).getOrThrow(false, Placebo.LOGGER::error);
            T encodedObj = codec.encode(input, ops, prefix).getOrThrow(false, Placebo.LOGGER::error);
            return ops.mergeToMap(encodedObj, ops.createString("type"), encodedKey);
        }
    }

    public interface CodecProvider<T> {

        /**
         * Returns a codec capable of de/serializing this object.
         */
        public Codec<? extends T> getCodec();

    }
}
