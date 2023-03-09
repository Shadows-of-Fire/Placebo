package shadows.placebo.json;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import shadows.placebo.Placebo;
import shadows.placebo.json.JsonUtil.JsonDeserializer;
import shadows.placebo.json.JsonUtil.JsonSerializer;
import shadows.placebo.json.JsonUtil.NetDeserializer;
import shadows.placebo.json.JsonUtil.NetSerializer;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyed;

public class PSerializer<V> implements JsonDeserializer<V>, JsonSerializer<V>, NetDeserializer<V>, NetSerializer<V> {

	private final String name;
	private final JsonDeserializer<V> jds;
	private final JsonSerializer<V> js;
	private final NetDeserializer<V> nds;
	private final NetSerializer<V> ns;

	public PSerializer(String name, JsonDeserializer<V> jds, JsonSerializer<V> js, NetDeserializer<V> nds, NetSerializer<V> ns) {
		this.name = name;
		this.jds = jds;
		this.js = js;
		this.nds = nds;
		this.ns = ns;
	}

	@Override
	public JsonElement write(V src) {
		if (this.js == null) throw new UnsupportedOperationException("Attempted to serialize a " + this.name + " to json, but this serializer does not support that operation.");
		return this.js.write(src);
	}

	@Override
	public void write(V src, FriendlyByteBuf buf) {
		if (this.ns == null) throw new UnsupportedOperationException("Attempted to serialize a " + this.name + " to the network, but this serializer does not support that operation.");
		this.ns.write(src, buf);
	}

	@Override
	public V read(JsonElement json) throws JsonParseException {
		if (this.jds == null) throw new UnsupportedOperationException("Attempted to deserialize a " + this.name + " from json, but this serializer does not support that operation.");
		return this.jds.read(json);
	}

	@Override
	public V read(FriendlyByteBuf buf) {
		if (this.nds == null) throw new UnsupportedOperationException("Attempted to deserialize a " + this.name + " from the network, but this serializer does not support that operation.");
		return this.nds.read(buf);
	}

	@SuppressWarnings("unchecked")
	private static <V> BiFunction<Object, Object, V> coerce(Method m) {
		MethodHandle k;
		try {
			k = MethodHandles.lookup().unreflect(m);
		} catch (IllegalAccessException e1) {
			throw new RuntimeException(e1);
		}
		return (p1, p2) -> {
			try {
				return (V) (p1 == null ? k.invoke(p2) : k.invoke(p1, p2));
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		};
	}

	/**
	 * Automatically generates a serializer based on methods found on the class.
	 * Searches for the following methods:
	 *
	 * public static V read(JsonElement) [Required]
	 * public JsonElement write() [Optional]
	 * 
	 * public static V read(FriendlyByteBuf) [Required if synced]
	 * public void write(FriendlyByteBuf) [Required if synced]
	 * 
	 * Note: the json methods work if the json param is any subclass of JsonElement, but it must match.
	 * 
	 * @param name Name of the object being serialized, for error logging
	 * @param synced If this serialized will be used for networked data
	 * @param clazz The class containing the read/write methods
	 * 
	 * @deprecated Prefer using {@link #fromCodec(String, Codec)} as it gives better error reporting even when buried in lambda soup.
	 */
	@Deprecated(forRemoval = true)
	public static <V> PSerializer.Builder<V> autoRegister(String name, Class<? extends V> clazz) {
		Builder<V> builder = new Builder<>(name);
		Method[] methods = clazz.getDeclaredMethods();
		Class<?> jsonType = null;
		for (Method m : methods) {
			if (Modifier.isStatic(m.getModifiers())) {
				if (m.getName().equals("read")) {
					Class<?>[] p = m.getParameterTypes();
					if (p.length == 1) {
						if (p[0] == FriendlyByteBuf.class) {
							builder.withNetworkDeserializer(buf -> PSerializer.<V>coerce(m).apply(null, buf));
						} else if (JsonElement.class.isAssignableFrom(p[0])) {
							builder.withJsonDeserializer(obj -> PSerializer.<V>coerce(m).apply(null, obj));
							if (jsonType == null) jsonType = p[0];
							else if (jsonType != p[0]) throw new RuntimeException("Invalid automatic PSerializer registration - JSON read/write type mismatch!");
						}
					}
				}
			} else {
				if (m.getName().equals("write")) {
					Class<?>[] p = m.getParameterTypes();
					if (p.length == 1) {
						if (p[0] == FriendlyByteBuf.class) {
							builder.withNetworkSerializer((inst, buf) -> coerce(m).apply(inst, buf));
						}
					} else if (p.length == 0 && JsonElement.class.isAssignableFrom(m.getReturnType())) {
						builder.withJsonSerializer((inst) -> PSerializer.<JsonElement>coerce(m).apply(null, inst));
						if (jsonType == null) jsonType = m.getReturnType();
						else if (jsonType != m.getReturnType()) throw new RuntimeException("Invalid automatic PSerializer registration - JSON read/write type mismatch!");
					}
				}
			}
		}

		return builder;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <V> PSerializer.Builder<V> fromCodec(String name, Codec<? extends V> codec) {
		Builder<V> builder = new Builder<>(name);
		Codec<V> rawCodec = (Codec) codec;
		Consumer<String> onErr = msg -> logCodecError(name, msg);
		builder.withJsonSerializer(obj -> rawCodec.encodeStart(JsonOps.INSTANCE, obj).getOrThrow(false, onErr));
		builder.withJsonDeserializer(json -> rawCodec.decode(JsonOps.INSTANCE, json).getOrThrow(false, onErr).getFirst());
		builder.withNetworkSerializer((obj, buf) -> buf.writeNbt((CompoundTag) rawCodec.encodeStart(NbtOps.INSTANCE, obj).getOrThrow(false, onErr)));
		builder.withNetworkDeserializer(buf -> rawCodec.decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow(false, onErr).getFirst());
		return builder;
	}

	private static void logCodecError(String name, String msg) {
		Placebo.LOGGER.error("Codec failure for type {}, message: {}", name, msg);
	}

	public static <V> PSerializer.Builder<V> builtin(String name, Supplier<V> factory) {
		Builder<V> builder = new Builder<>(name);
		builder.withJsonDeserializer(json -> factory.get()).withJsonSerializer(o -> new JsonObject());
		builder.withNetworkDeserializer(net -> factory.get()).withNetworkSerializer((obj, buf) -> {
		});
		return builder;
	}

	public static <V extends TypeKeyed<V>> PSerializerTypeAdapter<V> makeTypeAdapter(BiMap<ResourceLocation, PSerializer<V>> serializers) {
		return new PSerializerTypeAdapter<>(serializers);
	}

	public static class Builder<V> {

		private final String name;
		private JsonDeserializer<V> jds;
		private JsonSerializer<V> js;
		private NetDeserializer<V> nds;
		private NetSerializer<V> ns;

		/**
		 * Creates a new serializer builder
		 * @param name The name of the thing being serialized.
		 */
		public Builder(String name) {
			this.name = name;
		}

		public Builder<V> withJsonDeserializer(JsonDeserializer<V> jds) {
			this.jds = jds;
			return this;
		}

		public Builder<V> withJsonSerializer(JsonSerializer<V> js) {
			this.js = js;
			return this;
		}

		public Builder<V> withNetworkDeserializer(NetDeserializer<V> nds) {
			this.nds = nds;
			return this;
		}

		public Builder<V> withNetworkSerializer(NetSerializer<V> ns) {
			this.ns = ns;
			return this;
		}

		public Builder<V> json(JsonDeserializer<V> jds, JsonSerializer<V> js) {
			return this.withJsonDeserializer(jds).withJsonSerializer(js);
		}

		public Builder<V> net(NetDeserializer<V> jds, NetSerializer<V> js) {
			return this.withNetworkDeserializer(jds).withNetworkSerializer(js);
		}

		public PSerializer<V> build(boolean synced) {
			Preconditions.checkNotNull(this.jds, "Attempted to build a Serializer for " + this.name + " but no json deserializer was provided.");
			if (synced) {
				Preconditions.checkNotNull(this.nds, "Attempted to build a Synced Serializer for " + this.name + " but no network deserializer was provided.");
				Preconditions.checkNotNull(this.ns, "Attempted to build a Synced Serializer for " + this.name + " but no network serializer was provided.");
			}
			return new PSerializer<>(this.name, this.jds, this.js, this.nds, this.ns);
		}

	}

}
