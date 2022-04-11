package shadows.placebo.json;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.network.PacketBuffer;

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
		public JsonObject serialize(V src) {
			if (this.js == null) throw new UnsupportedOperationException("Attempted to serialize a " + this.name + " to json, but this serializer does not support that operation.");
			return this.js.serialize(src);
		}

		@Override
		public void serialize(V src, PacketBuffer buf) {
			if (this.ns == null) throw new UnsupportedOperationException("Attempted to serialize a " + this.name + " to the network, but this serializer does not support that operation.");
			this.ns.serialize(src, buf);
		}

		@Override
		public V deserialize(JsonObject json) throws JsonParseException {
			if (this.jds == null) throw new UnsupportedOperationException("Attempted to deserialize a " + this.name + " from json, but this serializer does not support that operation.");
			return this.jds.deserialize(json);
		}

		@Override
		public V deserialize(PacketBuffer buf) {
			if (this.nds == null) throw new UnsupportedOperationException("Attempted to deserialize a " + this.name + " from the network, but this serializer does not support that operation.");
			return this.nds.deserialize(buf);
		}

	}

	public static interface JsonSerializer<V> {
		public JsonObject serialize(V src);
	}

	public static interface JsonDeserializer<V> {
		public V deserialize(JsonObject json);
	}

	public static interface NetSerializer<V> {
		public void serialize(V src, PacketBuffer buf);
	}

	public static interface NetDeserializer<V> {
		public V deserialize(PacketBuffer buf);
	}

}
