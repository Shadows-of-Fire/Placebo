package shadows.placebo.json;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiFunction;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.network.FriendlyByteBuf;

public class SerializerBuilder<V> {

	private final String name;
	private JsonDeserializer<V> jds;
	private JsonSerializer<V> js;
	private NetDeserializer<V> nds;
	private NetSerializer<V> ns;

	/**
	 * Creates a new serializer builder
	 * @param name The name of the thing being serialized.
	 */
	public SerializerBuilder(String name) {
		this.name = name;
	}

	public SerializerBuilder<V> withJsonDeserializer(JsonDeserializer<V> jds) {
		this.jds = jds;
		return this;
	}

	public SerializerBuilder<V> withJsonSerializer(JsonSerializer<V> js) {
		this.js = js;
		return this;
	}

	public SerializerBuilder<V> withNetworkDeserializer(NetDeserializer<V> nds) {
		this.nds = nds;
		return this;
	}

	public SerializerBuilder<V> withNetworkSerializer(NetSerializer<V> ns) {
		this.ns = ns;
		return this;
	}

	public SerializerBuilder<V> json(JsonDeserializer<V> jds, JsonSerializer<V> js) {
		return this.withJsonDeserializer(jds).withJsonSerializer(js);
	}

	public SerializerBuilder<V> net(NetDeserializer<V> jds, NetSerializer<V> js) {
		return this.withNetworkDeserializer(jds).withNetworkSerializer(js);
	}

	@SuppressWarnings("unchecked")
	private BiFunction<Object, Object, V> coerce(Method m) {
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

	public SerializerBuilder<V> autoRegister(Class<? extends V> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method m : methods) {
			if (Modifier.isStatic(m.getModifiers())) {
				if (m.getName().equals("read")) {
					Class<?>[] p = m.getParameterTypes();
					if (p.length == 1) {
						if (p[0] == FriendlyByteBuf.class) {
							this.withNetworkDeserializer(buf -> coerce(m).apply(null, buf));
						} else if (p[0] == JsonObject.class) {
							this.withJsonDeserializer(obj -> coerce(m).apply(null, obj));
						}
					}
				}
			} else {
				if (m.getName().equals("write")) {
					Class<?>[] p = m.getParameterTypes();
					if (p.length == 1) {
						if (p[0] == FriendlyByteBuf.class) {
							this.withNetworkSerializer((inst, buf) -> coerce(m).apply(inst, buf));
						}
					} else if (p.length == 0 && m.getReturnType() == JsonObject.class) {
						this.withJsonDeserializer((inst) -> coerce(m).apply(null, inst));
					}
				}
			}
		}

		return this;
	}

	public Serializer build(boolean synced) {
		Preconditions.checkNotNull(this.jds, "Attempted to build a Serializer for " + this.name + " but no json deserializer was provided.");
		if (synced) {
			Preconditions.checkNotNull(this.nds, "Attempted to build a Synced Serializer for " + this.name + " but no network deserializer was provided.");
			Preconditions.checkNotNull(this.ns, "Attempted to build a Synced Serializer for " + this.name + " but no network serializer was provided.");
		}
		return new Serializer(this.name, this.jds, this.js, this.nds, this.ns);
	}

	public class Serializer implements JsonDeserializer<V>, JsonSerializer<V>, NetDeserializer<V>, NetSerializer<V> {

		private final String name;
		private final JsonDeserializer<V> jds;
		private final JsonSerializer<V> js;
		private final NetDeserializer<V> nds;
		private final NetSerializer<V> ns;

		public Serializer(String name, JsonDeserializer<V> jds, JsonSerializer<V> js, NetDeserializer<V> nds, NetSerializer<V> ns) {
			this.name = name;
			this.jds = jds;
			this.js = js;
			this.nds = nds;
			this.ns = ns;
		}

		@Override
		public JsonObject write(V src) {
			if (this.js == null) throw new UnsupportedOperationException("Attempted to serialize a " + this.name + " to json, but this serializer does not support that operation.");
			return this.js.write(src);
		}

		@Override
		public void write(V src, FriendlyByteBuf buf) {
			if (this.ns == null) throw new UnsupportedOperationException("Attempted to serialize a " + this.name + " to the network, but this serializer does not support that operation.");
			this.ns.write(src, buf);
		}

		@Override
		public V read(JsonObject json) throws JsonParseException {
			if (this.jds == null) throw new UnsupportedOperationException("Attempted to deserialize a " + this.name + " from json, but this serializer does not support that operation.");
			return this.jds.read(json);
		}

		@Override
		public V read(FriendlyByteBuf buf) {
			if (this.nds == null) throw new UnsupportedOperationException("Attempted to deserialize a " + this.name + " from the network, but this serializer does not support that operation.");
			return this.nds.read(buf);
		}

	}

	public static interface JsonSerializer<V> {
		public JsonObject write(V src);
	}

	public static interface JsonDeserializer<V> {
		public V read(JsonObject json);
	}

	public static interface NetSerializer<V> {
		public void write(V src, FriendlyByteBuf buf);
	}

	public static interface NetDeserializer<V> {
		public V read(FriendlyByteBuf buf);
	}

}
