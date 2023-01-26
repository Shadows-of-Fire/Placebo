package shadows.placebo.codec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.ListCodec;

import net.minecraft.resources.ResourceLocation;
import shadows.placebo.Placebo;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class PlaceboCodecs {

	public static <V> Codec<V> mapBacked(String name, Map<ResourceLocation, Codec<? extends V>> reg, Function<V, ResourceLocation> keyExtractor) {
		return new MapBackedCodec<>(name, reg, keyExtractor);
	}

	public static <V> Codec<Set<V>> setCodec(Codec<V> elementCodec) {
		return new ListCodec<>(elementCodec).<Set<V>>xmap(HashSet::new, ArrayList::new);
	}

	public static class MapBackedCodec<V> implements Codec<V> {

		protected final String name;
		protected final Map<ResourceLocation, Codec<? extends V>> registry;
		protected final Function<V, ResourceLocation> keyExtractor;

		public MapBackedCodec(String name, Map<ResourceLocation, Codec<? extends V>> registry, Function<V, ResourceLocation> keyExtractor) {
			this.name = name;
			this.registry = registry;
			this.keyExtractor = keyExtractor;
		}

		@Override
		public <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
			ResourceLocation key = ResourceLocation.CODEC.decode(ops, ops.get(input, "type").getOrThrow(false, Placebo.LOGGER::error)).getOrThrow(false, Placebo.LOGGER::error).getFirst();
			Codec codec = registry.get(key);
			if (codec == null) {
				return DataResult.error("Failure when parsing a " + name + ". Unrecognized type: " + key);
			}
			return codec.decode(ops, input);
		}

		@Override
		public <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
			ResourceLocation key = keyExtractor.apply(input);
			if (key == null) {
				return DataResult.error("Attempted to serialize an unnamed element of type " + name + ": " + input);
			}
			Codec codec = registry.get(key);
			if (codec == null) {
				return DataResult.error("Attempted to serialize an unregistered element of type " + name + " with key: " + key);
			}
			T encodedKey = ResourceLocation.CODEC.encodeStart(ops, key).getOrThrow(false, Placebo.LOGGER::error);
			T encodedObj = (T) codec.encode(input, ops, prefix).getOrThrow(false, Placebo.LOGGER::error);
			return ops.mergeToMap(encodedObj, ops.createString("type"), encodedKey);
		}
	}
}
