package dev.shadowsoffire.placebo.dynreg;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

/**
 * A {@link RegistrySerializer} that dispatches between multiple subtype codecs using a top-level "type" key.
 * <p>
 * Use {@link #register} to add a subtype codec or {@link #registerDefault} to add a fallback codec used when the
 * "type" key is absent. When synced, the {@link #streamCodec()} writes the matching type {@link Identifier} before the
 * payload so that the receiver can decode against the correct codec.
 *
 * @param <R> The element type. Must implement {@link CodecProvider} so subtype dispatch can recover the original codec.
 */
public final class SubtypedSerializer<R extends CodecProvider<? super R>> extends RegistrySerializer<R> {

    private final String name;
    private final CodecMap<R> codecs;
    private final BiMap<Identifier, StreamCodec<RegistryFriendlyByteBuf, ? extends R>> streamCodecs;

    @Nullable
    private final StreamCodec<RegistryFriendlyByteBuf, R> dispatchingStream;

    SubtypedSerializer(String name, boolean synced) {
        this.name = name;
        this.codecs = new CodecMap<>(name);
        this.streamCodecs = HashBiMap.create();
        this.dispatchingStream = synced ? StreamCodec.of(this::encodeStream, this::decodeStream) : null;
    }

    @Override
    public Codec<R> codec() {
        return this.codecs;
    }

    @Override
    @Nullable
    public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
        return this.dispatchingStream;
    }

    /**
     * Registers a subtype codec. The codec will be selected when the "type" key in a json file equals {@code key}.
     *
     * @param key         The type key.
     * @param codec       The codec for this subtype.
     * @param streamCodec The stream codec for this subtype, used during sync.
     * @return This serializer, for chaining.
     */
    public SubtypedSerializer<R> register(Identifier key, Codec<? extends R> codec, StreamCodec<RegistryFriendlyByteBuf, ? extends R> streamCodec) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(codec, "Attempted to register a null codec for key " + key);
        Preconditions.checkNotNull(streamCodec, "Attempted to register a null stream codec for key " + key);
        this.codecs.register(key, codec);
        this.streamCodecs.put(key, streamCodec);
        return this;
    }

    /**
     * Variant of {@link #register(Identifier, Codec, StreamCodec)} that automatically wraps the codec as a stream codec.
     * <p>
     * Prefer the explicit overload when this serializer is synced and the codec involves registry-aware data.
     */
    public SubtypedSerializer<R> register(Identifier key, Codec<? extends R> codec) {
        return this.register(key, codec, ByteBufCodecs.fromCodecWithRegistries(codec));
    }

    /**
     * Registers a default codec. The default codec is used when no "type" key is present in the json file.
     * Only one default codec may be registered.
     *
     * @param key         The type key.
     * @param codec       The default codec.
     * @param streamCodec The stream codec for the default subtype, used during sync.
     * @return This serializer, for chaining.
     */
    public SubtypedSerializer<R> registerDefault(Identifier key, Codec<? extends R> codec, StreamCodec<RegistryFriendlyByteBuf, ? extends R> streamCodec) {
        if (this.codecs.getDefaultCodec() != null) {
            throw new UnsupportedOperationException("Attempted to register a second default codec with key " + key);
        }
        this.register(key, codec, streamCodec);
        this.codecs.setDefaultCodec(codec);
        return this;
    }

    /**
     * Variant of {@link #registerDefault(Identifier, Codec, StreamCodec)} that automatically wraps the codec as a stream codec.
     * <p>
     * Prefer the explicit overload when this serializer is synced and the codec involves registry-aware data.
     */
    public SubtypedSerializer<R> registerDefault(Identifier key, Codec<? extends R> codec) {
        return this.registerDefault(key, codec, ByteBufCodecs.fromCodecWithRegistries(codec));
    }

    /**
     * @return True if at least one subtype codec is registered.
     */
    public boolean isPopulated() {
        return !this.codecs.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private void encodeStream(RegistryFriendlyByteBuf buf, R value) {
        Identifier type = this.codecs.getKey(value.getCodec());
        if (type == null) {
            throw new EncoderException("Attempted to sync a " + this.name + " with an unregistered codec! Object: " + value);
        }
        Identifier.STREAM_CODEC.encode(buf, type);
        ((StreamCodec<RegistryFriendlyByteBuf, R>) this.streamCodecs.get(type)).encode(buf, value);
    }

    @SuppressWarnings("unchecked")
    private R decodeStream(RegistryFriendlyByteBuf buf) {
        Identifier type = Identifier.STREAM_CODEC.decode(buf);
        StreamCodec<RegistryFriendlyByteBuf, ? extends R> codec = this.streamCodecs.get(type);
        if (codec == null) {
            throw new DecoderException("Received sync packet with unknown subtype: " + type);
        }
        return ((StreamCodec<RegistryFriendlyByteBuf, R>) codec).decode(buf);
    }

}
