package dev.shadowsoffire.placebo.dynreg;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A Registry Serializer encapsulates the de/serialization strategy for a {@link DynamicRegistry}.
 * <p>
 * It owns the {@link Codec} used to read entries from disk and the optional {@link StreamCodec} used to sync
 * entries over the network. {@link DynamicRegistry} delegates all codec concerns to its serializer, which means
 * that base registries do not need to know whether their elements support subtypes or are synchronized.
 *
 * @param <R> The element type of the target registry.
 *
 * @see #simple(Codec)
 * @see #synced(Codec)
 * @see #synced(Codec, StreamCodec)
 * @see #subtyped(String)
 * @see #subtypedSynced(String)
 */
public abstract class RegistrySerializer<R> {

    /**
     * @return The codec used to read entries of this registry from json.
     */
    public abstract Codec<R> codec();

    /**
     * @return The stream codec used to sync entries over the network, or null if the registry is not synced.
     */
    @Nullable
    public abstract StreamCodec<RegistryFriendlyByteBuf, R> streamCodec();

    /**
     * @return True if this serializer supports network sync.
     */
    public final boolean isSynced() {
        return this.streamCodec() != null;
    }

    /**
     * Creates a non-synced serializer backed by the supplied {@link Codec}.
     *
     * @param <R>   The element type.
     * @param codec The element codec.
     */
    public static <R> RegistrySerializer<R> simple(Codec<R> codec) {
        return new Simple<>(codec, null);
    }

    /**
     * Creates a synced serializer backed by the supplied {@link Codec}, automatically wrapping it as a {@link StreamCodec}.
     *
     * @param <R>   The element type.
     * @param codec The element codec, used both for json and for sync.
     */
    public static <R> RegistrySerializer<R> synced(Codec<R> codec) {
        return new Simple<>(codec, ByteBufCodecs.fromCodecWithRegistries(codec));
    }

    /**
     * Creates a synced serializer backed by an explicit {@link Codec} and {@link StreamCodec}.
     *
     * @param <R>         The element type.
     * @param codec       The element codec, used for json.
     * @param streamCodec The stream codec, used for sync.
     */
    public static <R> RegistrySerializer<R> synced(Codec<R> codec, StreamCodec<RegistryFriendlyByteBuf, R> streamCodec) {
        return new Simple<>(codec, streamCodec);
    }

    /**
     * Creates a non-synced subtype-dispatching serializer.
     * <p>
     * Subtypes are registered via {@link SubtypedSerializer#register} and {@link SubtypedSerializer#registerDefault}.
     *
     * @param <R>  The element type. Must implement {@link CodecProvider} so encoding can recover the original codec.
     * @param name The name of the registry, used in error messages.
     */
    public static <R extends CodecProvider<? super R>> SubtypedSerializer<R> subtyped(String name) {
        return new SubtypedSerializer<>(name, false);
    }

    /**
     * Creates a synced subtype-dispatching serializer.
     * <p>
     * Subtypes are registered via {@link SubtypedSerializer#register} and {@link SubtypedSerializer#registerDefault}.
     *
     * @param <R>  The element type. Must implement {@link CodecProvider} so encoding can recover the original codec.
     * @param name The name of the registry, used in error messages.
     */
    public static <R extends CodecProvider<? super R>> SubtypedSerializer<R> subtypedSynced(String name) {
        return new SubtypedSerializer<>(name, true);
    }

    private static final class Simple<R> extends RegistrySerializer<R> {

        private final Codec<R> codec;

        @Nullable
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        private Simple(Codec<R> codec, @Nullable StreamCodec<RegistryFriendlyByteBuf, R> streamCodec) {
            this.codec = codec;
            this.streamCodec = streamCodec;
        }

        @Override
        public Codec<R> codec() {
            return this.codec;
        }

        @Override
        @Nullable
        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return this.streamCodec;
        }
    }

}
