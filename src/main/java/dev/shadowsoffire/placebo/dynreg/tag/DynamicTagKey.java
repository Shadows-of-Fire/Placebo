package dev.shadowsoffire.placebo.dynreg.tag;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

/**
 * The {@link DynamicRegistry} equivalent of a {@link TagKey}.
 */
public record DynamicTagKey<R>(Identifier registryId, Identifier id) {

    /**
     * Creates a tag key for the given registry.
     *
     * @param <R>      The element type of the registry.
     * @param registry The registry this tag belongs to.
     * @param id       The id of the tag.
     */
    public static <R> DynamicTagKey<R> create(DynamicRegistry<R> registry, Identifier id) {
        return new DynamicTagKey<>(registry.getId(), id);
    }

    /**
     * Returns a {@link Codec} that reads/writes tag keys for the registry at {@code registryId}.
     * <p>
     * The serialized form is just the tag {@link Identifier}; the registry id is implicit from context.
     */
    public static <R> Codec<DynamicTagKey<R>> codec(Identifier registryId) {
        return Identifier.CODEC.xmap(id -> new DynamicTagKey<R>(registryId, id), DynamicTagKey::id);
    }

    /**
     * Returns a {@link StreamCodec} that reads/writes tag keys for the registry at {@code registryId}.
     */
    public static <R> StreamCodec<ByteBuf, DynamicTagKey<R>> streamCodec(Identifier registryId) {
        return Identifier.STREAM_CODEC.map(id -> new DynamicTagKey<R>(registryId, id), DynamicTagKey::id);
    }

    @Override
    public String toString() {
        return "#" + this.id + " (" + this.registryId + ")";
    }
}
