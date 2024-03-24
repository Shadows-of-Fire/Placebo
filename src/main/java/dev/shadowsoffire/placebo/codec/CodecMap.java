package dev.shadowsoffire.placebo.codec;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import net.minecraft.resources.ResourceLocation;

/**
 * A Codec map is simultaneously a registry of named codecs and a codec for the specified value type.
 * <p>
 * The intended usage is to have a codec map stored as a field where new sub-codecs may be registered,
 * without needing a separate codec field or separate registration handling code.
 *
 * @param <V> The type of the codec.
 */
public class CodecMap<V extends CodecProvider<? super V>> implements Codec<V> {

    protected final String name;
    private final BiMap<ResourceLocation, Codec<? extends V>> codecs = HashBiMap.create();
    private final Codec<V> codec;

    @Nullable
    protected Codec<? extends V> defaultCodec;

    /**
     * Creates a new CodecMap with the given name.
     *
     * @param name The name of the object being de/serialized, for logging.
     */
    public CodecMap(String name) {
        this.name = name;
        this.codec = new MapBackedCodec<>(this.name, this.codecs, this::getDefaultCodec);
    }

    @Nullable
    public Codec<? extends V> getDefaultCodec() {
        return this.defaultCodec;
    }

    public void setDefaultCodec(Codec<? extends V> codec) {
        synchronized (this.codecs) {
            if (this.defaultCodec != null) throw new UnsupportedOperationException("Attempted to set the default codec after it has already been set.");
            if (this.getKey(codec) == null) throw new UnsupportedOperationException("Attempted to set the default codec without registering it first.");
            this.defaultCodec = codec;
        }
    }

    /**
     * Returns true if there are no codecs registered.
     */
    public boolean isEmpty() {
        return this.codecs.isEmpty();
    }

    /**
     * Returns true if a codecs with the passed type key exists.
     */
    public boolean containsKey(ResourceLocation key) {
        return this.codecs.containsKey(key);
    }

    /**
     * Gets a codec by type key.
     *
     * @param typeId The key of the codec.
     * @return The codec registered with the passed key, or null, if none exists.
     */
    @Nullable
    public Codec<? extends V> getValue(ResourceLocation key) {
        return this.codecs.get(key);
    }

    /**
     * Gets the key of a codec.
     *
     * @param codec A codec.
     * @return The key of the codec, or null, if it is not registered.
     */
    @Nullable
    public ResourceLocation getKey(Codec<?> codec) {
        return this.codecs.inverse().get(codec);
    }

    /**
     * Registers a codec with the specified key.
     *
     * @param key   The key of the codec.
     * @param codec The codec being registered.
     * @throws UnsupportedOperationException if the key or codec is already registered.
     */
    public void register(ResourceLocation key, Codec<? extends V> codec) {
        synchronized (this.codecs) {
            if (this.codecs.containsKey(key)) {
                throw new UnsupportedOperationException("Attempted to register a " + this.name + " codec with key " + key + " but one already exists!");
            }
            if (this.codecs.containsValue(codec)) {
                throw new UnsupportedOperationException("Attempted to register a " + this.name + " codec with key " + key + " but it is already registered under the key " + this.getKey(codec));
            }
            this.codecs.put(key, codec);
        }
    }

    @Override
    public final <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        return this.codec.encode(input, ops, prefix);
    }

    @Override
    public final <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        return this.codec.decode(ops, input);
    }

}
