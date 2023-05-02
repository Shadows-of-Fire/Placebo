package shadows.placebo.codec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.resources.ResourceLocation;
import shadows.placebo.Placebo;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PlaceboCodecs {

	public static <V extends CodecProvider<V>> Codec<V> mapBackedDefaulted(String name, BiMap<ResourceLocation, Codec<? extends V>> reg, Codec<? extends V> defaultCodec) {
		return new MapBackedCodec<>(name, reg, defaultCodec);
	}

	public static <V extends CodecProvider<V>> Codec<V> mapBacked(String name, BiMap<ResourceLocation, Codec<? extends V>> reg) {
		return new MapBackedCodec<>(name, reg);
	}

	public static <V> Codec<Set<V>> setCodec(Codec<V> elementCodec) {
		return elementCodec.listOf().<Set<V>>xmap(HashSet::new, ArrayList::new);
	}

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
