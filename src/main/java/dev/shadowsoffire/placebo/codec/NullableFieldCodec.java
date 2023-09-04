package dev.shadowsoffire.placebo.codec;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.OptionalFieldCodec;

/**
 * Similar to OptionalFieldCodec, but *only* permits absent values.
 * OptionalFieldCodec silently catches any parsing error-results and
 * returns an empty optional in such cases.
 * <p>
 * NullableFieldCodec returns an empty optional when it encounters absent
 * values, but returns error results if encounters any other decoding issues.
 * 
 * @author Commoble - Used under the terms of the MIT license.
 */
public class NullableFieldCodec<A> extends OptionalFieldCodec<A> {

    private final String name;
    private final Codec<A> elementCodec;

    /**
     * @param name         String name of the field
     * @param elementCodec Codec to de/serialize the field's value
     */
    protected NullableFieldCodec(String name, Codec<A> elementCodec) {
        super(name, elementCodec);
        this.name = name;
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> DataResult<Optional<A>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final T value = input.get(this.name);
        if (value == null) {
            return DataResult.success(Optional.empty());
        }
        return this.elementCodec.parse(ops, value)
            .map(Optional::of);
    }
}
