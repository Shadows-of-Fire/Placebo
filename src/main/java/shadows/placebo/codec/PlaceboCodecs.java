package shadows.placebo.codec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import shadows.placebo.Placebo;
import shadows.placebo.json.ItemAdapter;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PlaceboCodecs {

	public static <V extends CodecProvider<V>> Codec<V> mapBackedDefaulted(String name, BiMap<ResourceLocation, Codec<? extends V>> reg, Codec<? extends V> defaultCodec) {
		return new MapBackedCodec<>(name, reg, defaultCodec);
	}

	public static <V extends CodecProvider<V>> Codec<V> mapBacked(String name, BiMap<ResourceLocation, Codec<? extends V>> reg) {
		return new MapBackedCodec<>(name, reg);
	}

	@Deprecated // Renamed to setOf
	public static <V> Codec<Set<V>> setCodec(Codec<V> elementCodec) {
		return setOf(elementCodec);
	}

	public static <V> Codec<Set<V>> setOf(Codec<V> elementCodec) {
		return setFromList(elementCodec.listOf());
	}

	public static <V> Codec<Set<V>> setFromList(Codec<List<V>> listCodec) {
		return listCodec.<Set<V>>xmap(HashSet::new, ArrayList::new);
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
	 * @see NullableFieldCodec
	 */
	public static <A> MapCodec<Optional<A>> nullableField(Codec<A> elementCodec, String name) {
		return new NullableFieldCodec<>(name, elementCodec);
	}

	/**
	 * Creates a nullable field codec with the given default value for use in {@link RecordCodecBuilder}.
	 * <p>
	 * Used to avoid swallowing exceptions during parse errors.
	 *
	 * @see NullableFieldCodec
	 */
	public static <A> MapCodec<A> nullableField(Codec<A> elementCodec, String name, A defaultValue) {
		return nullableField(elementCodec, name).xmap(o -> o.orElse(defaultValue), Optional::ofNullable);
	}

	/**
	 * One-Way Ingredient codec. When initialliy deserializing, supports a mix of tag keys and item registry names.<br>
	 * When serialzing, it will only serialize the actual item list, which will not include the original tag key.
	 */
	@Deprecated(forRemoval = true)
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
				return DataResult.success(Pair.of(CraftingHelper.getIngredient(json), input));
			} catch (JsonSyntaxException ex) {
				return DataResult.error(ex.getMessage());
			}
		}

	}

	@Deprecated(forRemoval = true)
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
			Optional<T> type = ops.get(input, "type").resultOrPartial(str -> {
			});
			Optional<ResourceLocation> key = type.map(t -> ResourceLocation.CODEC.decode(ops, t).resultOrPartial(Placebo.LOGGER::error).get().getFirst());

			Codec codec = key.<Codec>map(this.registry::get).orElse(this.defaultCodec);

			if (codec == null) {
				return DataResult.error("Failure when parsing a " + name + ". Unrecognized type: " + key.map(ResourceLocation::toString).orElse("null"));
			}
			return codec.decode(ops, input);
		}

		@Override
		public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
			Codec<V> codec = (Codec<V>) input.getCodec();
			ResourceLocation key = this.registry.inverse().get(codec);
			if (key == null) {
				return DataResult.error("Attempted to serialize an element of type " + name + " with an unregistered codec! Object: " + input);
			}
			T encodedKey = ResourceLocation.CODEC.encodeStart(ops, key).getOrThrow(false, Placebo.LOGGER::error);
			T encodedObj = (T) codec.encode(input, ops, prefix).getOrThrow(false, Placebo.LOGGER::error);
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
