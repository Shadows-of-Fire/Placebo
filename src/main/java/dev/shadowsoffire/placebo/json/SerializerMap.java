package dev.shadowsoffire.placebo.json;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;

import dev.shadowsoffire.placebo.json.PSerializer.PSerializable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * A Serializer Map is a registry of PSerializers, which supports type-keyed de/serialization.
 *
 * @param <V> The base type of the object being de/serialized.
 */
public class SerializerMap<V extends PSerializable<? super V>> {

    /**
     * The name of the object being de/serialized, for logging.
     */
    protected final String name;

    /**
     * The default domain used when parsing type keys.
     */
    protected final String defaultDomain;

    /**
     * Registry of serializers.
     */
    private final BiMap<ResourceLocation, PSerializer<? extends V>> serializers = HashBiMap.create();

    /**
     * Creates a new SerializerMap with a fallback domain.<br>
     * This should only be used temporarily to migrate from string keys.
     *
     * @param name          See {@link #name}
     * @param defaultDomain See {@link #defaultDomain}
     */
    public SerializerMap(String name, String defaultDomain) {
        this.name = name;
        this.defaultDomain = defaultDomain;
    }

    /**
     * Creates a new SerializerMap.
     *
     * @param name See {@link #name}
     */
    public SerializerMap(String name) {
        this(name, "minecraft");
    }

    /**
     * Returns true if there are no serializers registered.
     */
    public boolean isEmpty() {
        return this.serializers.isEmpty();
    }

    /**
     * Returns true if a serializer with the passed type id exists.
     */
    public boolean contains(ResourceLocation typeId) {
        return this.serializers.containsKey(typeId);
    }

    /**
     * Gets a serializer by type id.
     *
     * @param typeId The ID of the serializer.
     * @return The serializer registered with the passed ID, or null, if none exists.
     */
    @Nullable
    public PSerializer<? extends V> get(ResourceLocation typeId) {
        return this.serializers.get(typeId);
    }

    /**
     * Gets the ID of a serializer.
     *
     * @param serializer A serializer.
     * @return The ID of the passed serializer, or null, if it is not registered.
     */
    @Nullable
    public ResourceLocation get(PSerializer<?> serializer) {
        return this.serializers.inverse().get(serializer);
    }

    /**
     * Registers a serializer with the specified ID.
     *
     * @param id         The ID of the serializer.
     * @param serializer The serializer being registered.
     * @throws RuntimeException if the key is already registered.
     */
    public void register(ResourceLocation id, PSerializer<? extends V> serializer) {
        if (this.serializers.containsKey(id)) {
            throw new RuntimeException("Attempted to register a " + this.name + " serializer with id " + id + " but one already exists!");
        }
        this.serializers.put(id, serializer);
    }

    /**
     * Reads an object from JSON, parsing the type and passing the json to the appropriate PSerializer.
     *
     * @param obj The json object being deserialized.
     * @return The deserialized object.
     * @throws RuntimeException If the object does contains no type or an invalid type.
     */
    public V read(JsonObject obj) {
        if (obj.has("type")) {
            ResourceLocation type = this.defaultedReloc(obj.get("type").getAsString());
            var serializer = this.serializers.get(type);
            if (serializer == null) throw new RuntimeException("Attempted to deserialize a " + this.name + " with type " + type + " but no serializer exists!");
            return serializer.read(obj);
        }
        else {
            throw new RuntimeException("Attempted to deserialize a " + this.name + " with unknown type!");
        }
    }

    /**
     * Writes an object to JSON, also writing the type key of the serializer to the resulting object.
     *
     * @param obj The object being serialized.
     * @return The serialized JSON.
     */
    @SuppressWarnings("unchecked")
    public JsonObject write(V obj) {
        JsonObject json = obj.getSerializer().writeUnchecked(obj).getAsJsonObject();
        ResourceLocation type = this.get(obj.getSerializer());
        json.addProperty("type", type.toString());
        return json;
    }

    /**
     * Reads an object from a byte buffer by reading the type name and delegating to the serializer.
     *
     * @param buf The byte buffer.
     * @return The deserialized object.
     */
    public V read(FriendlyByteBuf buf) {
        ResourceLocation type = buf.readResourceLocation();
        var serializer = this.serializers.get(type);
        return serializer.read(buf);
    }

    /**
     * Writes an object to a byte buffer, also writing the type name of the object.
     *
     * @param obj The object being serialized.
     * @param buf The buffer being written to.
     */
    public void write(V obj, FriendlyByteBuf buf) {
        ResourceLocation type = this.get(obj.getSerializer());
        buf.writeResourceLocation(type);
        obj.getSerializer().writeUnchecked(obj, buf);
    }

    private ResourceLocation defaultedReloc(String reloc) {
        if (reloc.indexOf(':') != -1) return new ResourceLocation(reloc);
        return new ResourceLocation(this.defaultDomain, reloc);
    }

}
