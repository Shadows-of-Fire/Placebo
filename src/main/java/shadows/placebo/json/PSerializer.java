package shadows.placebo.json;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import shadows.placebo.Placebo;
import shadows.placebo.json.JsonUtil.JsonDeserializer;
import shadows.placebo.json.JsonUtil.JsonSerializer;
import shadows.placebo.json.JsonUtil.NetDeserializer;
import shadows.placebo.json.JsonUtil.NetSerializer;

/**
 * A PSerializer is a combination of json and network de/serializer.<br>
 * Each component is optional (as necessary) except for the Json Deserializer.<br>
 * The consumer is responsible for verifying that all serializers conform to application-dependent requirements through {@link #validate(boolean)}
 *
 * @param <V> The type of object this serializer interfaces with.
 */
public class PSerializer<V> implements JsonDeserializer<V>, JsonSerializer<V>, NetDeserializer<V>, NetSerializer<V> {

    private final String name;

    private final JsonDeserializer<V> jds;

    @Nullable
    private final JsonSerializer<V> js;

    @Nullable
    private final NetDeserializer<V> nds;

    @Nullable
    private final NetSerializer<V> ns;

    /**
     * Creates a new PSerializer.
     *
     * @param name A string name of the object being de/serialized for error logging.
     * @param jds  The json deserializer.
     * @param js   The json serializer.
     * @param nds  The network deserializer.
     * @param ns   The network serializer.'
     * @see PSerializer.Builder
     */
    public PSerializer(String name, JsonDeserializer<V> jds, @Nullable JsonSerializer<V> js, @Nullable NetDeserializer<V> nds, @Nullable NetSerializer<V> ns) {
        this.name = Objects.requireNonNull(name);
        this.jds = Objects.requireNonNull(jds, "The serializer for " + this.name + " is missing a json deserializer.");
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
        return this.jds.read(json);
    }

    @Override
    public V read(FriendlyByteBuf buf) {
        if (this.nds == null) throw new UnsupportedOperationException("Attempted to deserialize a " + this.name + " from the network, but this serializer does not support that operation.");
        return this.nds.read(buf);
    }

    @SuppressWarnings("unchecked")
    public final JsonObject writeUnchecked(Object src) {
        return this.write((V) src);
    }

    @SuppressWarnings("unchecked")
    public final void writeUnchecked(Object src, FriendlyByteBuf buf) {
        this.write((V) src, buf);
    }

    /**
     * Validates optional properties of this PSerializer.
     *
     * @param serializable If true, the Json Serializer is required.
     * @param synced       If true, both a Network Deserializer and Network Serializer are required.
     */
    public void validate(boolean serializable, boolean synced) {
        if (serializable) {
            Objects.requireNonNull(this.js, "The serializer for " + this.name + " is missing a json serializer.");
        }
        if (synced) {
            Objects.requireNonNull(this.nds, "The serializer for " + this.name + " is missing a network deserializer.");
            Objects.requireNonNull(this.ns, "The serializer for " + this.name + " is missing a network serializer.");
        }
    }

    /**
     * Automatically creates a PSerializer from a codec.<br>
     * Network de/serialization is handled by converting the object to/from NBT and sending that over the network.
     *
     * @param <V>   The type of object being serialized.
     * @param name  The name of the type of object being serialized, for error logging.
     * @param codec The codec.
     * @return A Codec-Backed PSerializer.
     */
    public static <V> PSerializer<V> fromCodec(String name, Codec<V> codec) {
        Builder<V> builder = new Builder<>(name);
        Consumer<String> onErr = msg -> logCodecError(name, msg);
        builder.toJson(obj -> codec.encodeStart(JsonOps.INSTANCE, obj).getOrThrow(false, onErr).getAsJsonObject());
        builder.fromJson(json -> codec.decode(JsonOps.INSTANCE, json).getOrThrow(false, onErr).getFirst());
        builder.toNetwork((obj, buf) -> buf.writeNbt((CompoundTag) codec.encodeStart(NbtOps.INSTANCE, obj).getOrThrow(false, onErr)));
        builder.fromNetwork(buf -> codec.decode(NbtOps.INSTANCE, buf.readNbt()).getOrThrow(false, onErr).getFirst());
        return builder.build();
    }

    private static void logCodecError(String name, String msg) {
        Placebo.LOGGER.error("Codec failure for type {}, message: {}", name, msg);
    }

    /**
     * Creates a PSerializer for a built-in object that is not truly loaded from json.
     *
     * @param <V>     The type of the object.
     * @param name    The name of the type of the object.
     * @param factory A supplier to the built-in object.
     * @return A Serializer that will always return the built-in object.
     */
    public static <V> PSerializer<V> builtin(String name, Supplier<V> factory) {
        Builder<V> builder = new Builder<>(name);
        builder.fromJson(json -> factory.get()).toJson(o -> new JsonObject());
        builder.fromNetwork(net -> factory.get()).toNetwork((obj, buf) -> {});
        return builder.build();
    }

    /**
     * Creates a simple PSerializer which only supports json deserialization.
     *
     * @param <V>  The type of the object.
     * @param name The name of the type of the object.
     * @param jds  The Json Deserializer for the object.
     * @return A Serializer with only the ability to deserialize json.
     */
    public static <V> PSerializer<V> basic(String name, JsonDeserializer<V> jds) {
        return builder(name, jds).build();
    }

    /**
     * Creates a new builder.
     *
     * @see Builder#Builder(String)
     */
    public static <V> PSerializer.Builder<V> builder(String name) {
        return new PSerializer.Builder<>(name);
    }

    /**
     * Creates a new builder.
     *
     * @see Builder#Builder(String)
     */
    public static <V> PSerializer.Builder<V> builder(String name, JsonDeserializer<V> jds) {
        return new PSerializer.Builder<V>(name).fromJson(jds);
    }

    /**
     * A PSerializer Builder is a helper class for constructing a PSerializer.
     *
     * @param <V> The type being de/serialized.
     */
    public static class Builder<V> {

        private final String name;
        private JsonDeserializer<V> jds;
        private JsonSerializer<V> js;
        private NetDeserializer<V> nds;
        private NetSerializer<V> ns;

        /**
         * Creates a new serializer builder
         *
         * @param name The name of the thing being serialized.
         */
        public Builder(String name) {
            this.name = name;
        }

        /**
         * Sets the Json Deserializer of this builder.
         *
         * @param jds The Json Deserializer.
         * @return this
         */
        public Builder<V> fromJson(JsonDeserializer<V> jds) {
            this.jds = jds;
            return this;
        }

        /**
         * Sets the Json Serializer of this builder.
         *
         * @param js The Json Serializer.
         * @return this
         */
        public Builder<V> toJson(JsonSerializer<V> js) {
            this.js = js;
            return this;
        }

        /**
         * Sets the Network Deserializer of this builder.
         *
         * @param nds The Network Deserializer.
         * @return this
         */
        public Builder<V> fromNetwork(NetDeserializer<V> nds) {
            this.nds = nds;
            return this;
        }

        /**
         * Sets the Network Serializer of this builder.
         *
         * @param ns The Network Serializer.
         * @return this
         */
        public Builder<V> toNetwork(NetSerializer<V> ns) {
            this.ns = ns;
            return this;
        }

        /**
         * Sets the Network Serializer and Deserializer of this builder.
         *
         * @param nds The Network Deserializer.
         * @param ns  The Network Serializer.
         * @return this
         */
        public Builder<V> networked(NetDeserializer<V> nds, NetSerializer<V> ns) {
            return this.fromNetwork(nds).toNetwork(ns);
        }

        /**
         * Builds the underlying PSerializer.
         */
        public PSerializer<V> build() {
            return new PSerializer<>(this.name, this.jds, this.js, this.nds, this.ns);
        }

    }

    /**
     * An interface supporting Serializers with generic types.
     *
     * @param <V> This
     */
    public static interface PSerializable<V extends PSerializable<V>> {

        /**
         * Returns the serializer that is responsible for de/serializing this object.<br>
         * If this object is subtyped (i.e. is backed by a {@link SerializerMap}, the returned serializer should be registered.
         */
        PSerializer<? extends V> getSerializer();
    }

}
