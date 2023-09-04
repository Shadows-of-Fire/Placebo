package dev.shadowsoffire.placebo.codec;

import com.mojang.serialization.Codec;

/**
 * A Codec Provider is an object which supplies the codec that was used to create it.
 * <p>
 * Primarily for use by elements in {@link MapBackedCodec} and {@link CodecMap}.
 *
 * @param <T> This
 */
public interface CodecProvider<T> {

    /**
     * @return The codec used to de/serialize this object to/from disk.
     * @implNote The return value of this method must be invariant.
     */
    Codec<? extends T> getCodec();

}
