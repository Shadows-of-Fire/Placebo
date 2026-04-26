package dev.shadowsoffire.placebo.dynreg.tag;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

/**
 * The dynamic registry analogue to vanilla's {@link HolderSet}.
 * 
 * Use {@link #codec(DynamicRegistry)} as the field codec in a record that wants to accept either a {@code "#tag_id"}
 * string reference or an inline list of entry ids in JSON. Use {@link #streamCodec(DynamicRegistry)} for sync.
 *
 * @param <R> The element type of the registry whose entries this set references.
 */
public sealed interface DynamicHolderSet<R> permits DynamicHolderSet.Named, DynamicHolderSet.Direct {

    Stream<DynamicHolder<R>> stream();

    int size();

    boolean contains(DynamicHolder<R> holder);

    boolean contains(R value);

    /**
     * @return The tag key backing this set, if any. Empty for {@link Direct} sets.
     */
    Optional<DynamicTagKey<R>> unwrapKey();

    /**
     * @return Either the backing tag key (for {@link Named}) or the inline holder list (for {@link Direct}).
     *         Used by codec encoding to round-trip the set back to its serialized form.
     */
    Either<DynamicTagKey<R>, List<DynamicHolder<R>>> unwrap();

    /**
     * @return True if this set has resolved contents. {@link Direct} is always bound; {@link Named} is bound after the
     *         tag manager populates it.
     */
    boolean isBound();

    /**
     * @return A random holder from this set, or empty if the set is empty.
     */
    Optional<DynamicHolder<R>> getRandomElement(RandomSource random);

    /**
     * Creates a {@link Direct} set from a list of holders.
     */
    static <R> Direct<R> direct(List<DynamicHolder<R>> holders) {
        return new Direct<>(List.copyOf(holders));
    }

    /**
     * @return An empty {@link Direct} set.
     */
    @SuppressWarnings("unchecked")
    static <R> Direct<R> empty() {
        return (Direct<R>) Direct.EMPTY;
    }

    /**
     * Returns a {@link Codec} that reads/writes {@link DynamicHolderSet}s for the given registry.
     * <p>
     * Accepts either:
     * <ul>
     * <li>A {@code "#tag_id"} string → resolves to a {@link Named} interned by the registry. The set may be unbound at
     * decode time and bound later when tags load.</li>
     * <li>A list of entry id strings → produces a {@link Direct}.</li>
     * </ul>
     */
    static <R> Codec<DynamicHolderSet<R>> codec(DynamicRegistry<R> registry) {
        Codec<DynamicTagKey<R>> tagKeyCodec = Codec.STRING.comapFlatMap(
            s -> s.startsWith("#")
                ? Identifier.read(s.substring(1)).map(id -> new DynamicTagKey<R>(registry.getId(), id))
                : DataResult.error(() -> "Not a tag reference (must start with '#'): " + s),
            key -> "#" + key.id());
        Codec<List<DynamicHolder<R>>> listCodec = registry.holderCodec().listOf();
        return Codec.either(tagKeyCodec, listCodec)
            .xmap(
                either -> either.map(registry::getOrCreateTag, DynamicHolderSet::direct),
                set -> set.unwrap());
    }

    /**
     * Returns a {@link StreamCodec} for syncing {@link DynamicHolderSet}s.
     * <p>
     * Wire format: a discriminator byte (0 = {@link Direct}, 1 = {@link Named}), followed by either a list of holder
     * ids or a single tag id.
     */
    static <R> StreamCodec<RegistryFriendlyByteBuf, DynamicHolderSet<R>> streamCodec(DynamicRegistry<R> registry) {
        StreamCodec<ByteBuf, DynamicHolder<R>> holderStream = registry.holderStreamCodec();
        StreamCodec<ByteBuf, DynamicTagKey<R>> tagKeyStream = DynamicTagKey.streamCodec(registry.getId());
        return new StreamCodec<>(){

            @Override
            public DynamicHolderSet<R> decode(RegistryFriendlyByteBuf buf) {
                byte kind = buf.readByte();
                if (kind == 0) {
                    int n = ByteBufCodecs.readCount(buf, Integer.MAX_VALUE);
                    var holders = new java.util.ArrayList<DynamicHolder<R>>(n);
                    for (int i = 0; i < n; i++) {
                        holders.add(holderStream.decode(buf));
                    }
                    return new Direct<>(List.copyOf(holders));
                }
                else {
                    DynamicTagKey<R> key = tagKeyStream.decode(buf);
                    return registry.getOrCreateTag(key);
                }
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, DynamicHolderSet<R> value) {
                if (value instanceof Direct<R> direct) {
                    buf.writeByte(0);
                    ByteBufCodecs.writeCount(buf, direct.holders.size(), Integer.MAX_VALUE);
                    for (DynamicHolder<R> holder : direct.holders) {
                        holderStream.encode(buf, holder);
                    }
                }
                else if (value instanceof Named<R> named) {
                    buf.writeByte(1);
                    tagKeyStream.encode(buf, named.key);
                }
            }
        };
    }

    /**
     * A tag-backed holder set. Mutable and late-bound: instances are interned by {@link DynamicRegistry} and populated
     * when the tag manager loads tags. Before binding, the set is empty and {@link #isBound()} returns false.
     *
     * @param <R> The element type of the backing registry.
     */
    final class Named<R> implements DynamicHolderSet<R> {

        private final DynamicRegistry<R> registry;
        private final DynamicTagKey<R> key;
        private List<DynamicHolder<R>> holders = List.of();
        private boolean bound = false;

        @Nullable
        private Set<DynamicHolder<R>> contentsSet;

        public Named(DynamicRegistry<R> registry, DynamicTagKey<R> key) {
            this.registry = registry;
            this.key = key;
        }

        public DynamicTagKey<R> key() {
            return this.key;
        }

        public DynamicRegistry<R> registry() {
            return this.registry;
        }

        /**
         * Binds the set to the given holders. Called by {@link DynamicRegistry} during tag-manager apply.
         */
        public void bind(List<DynamicHolder<R>> holders) {
            this.holders = List.copyOf(holders);
            this.bound = true;
            this.contentsSet = null;
        }

        /**
         * Resets the set to empty/unbound. Called by {@link DynamicRegistry} when tags are cleared at reload start.
         */
        public void unbind() {
            this.holders = List.of();
            this.bound = false;
            this.contentsSet = null;
        }

        @Override
        public Stream<DynamicHolder<R>> stream() {
            return this.holders.stream();
        }

        @Override
        public int size() {
            return this.holders.size();
        }

        @Override
        public boolean contains(DynamicHolder<R> holder) {
            if (this.contentsSet == null) {
                this.contentsSet = new HashSet<>(this.holders);
            }
            return this.contentsSet.contains(holder);
        }

        @Override
        public boolean contains(R value) {
            return this.contains(this.registry.holder(value));
        }

        @Override
        public Optional<DynamicTagKey<R>> unwrapKey() {
            return Optional.of(this.key);
        }

        @Override
        public Either<DynamicTagKey<R>, List<DynamicHolder<R>>> unwrap() {
            return Either.left(this.key);
        }

        @Override
        public boolean isBound() {
            return this.bound;
        }

        @Override
        public Optional<DynamicHolder<R>> getRandomElement(RandomSource random) {
            return this.holders.isEmpty() ? Optional.empty() : Optional.of(this.holders.get(random.nextInt(this.holders.size())));
        }

        @Override
        public String toString() {
            return "NamedSet(" + this.key + ")[bound=" + this.bound + ", size=" + this.holders.size() + "]";
        }

    }

    /**
     * An inline holder set, backed by an immutable list of holders.
     *
     * @param <R> The element type of the backing registry.
     */
    record Direct<R>(List<DynamicHolder<R>> holders) implements DynamicHolderSet<R> {

        static final Direct<?> EMPTY = new Direct<>(List.of());

        public Direct{
            holders = List.copyOf(holders);
        }

        @Override
        public Stream<DynamicHolder<R>> stream() {
            return this.holders.stream();
        }

        @Override
        public int size() {
            return this.holders.size();
        }

        @Override
        public boolean contains(DynamicHolder<R> holder) {
            return this.holders.contains(holder);
        }

        @Override
        public boolean contains(R value) {
            if (this.holders.isEmpty()) return false;
            return this.contains(this.holders.get(0).getRegistry().holder(value));
        }

        @Override
        public Optional<DynamicTagKey<R>> unwrapKey() {
            return Optional.empty();
        }

        @Override
        public Either<DynamicTagKey<R>, List<DynamicHolder<R>>> unwrap() {
            return Either.right(this.holders);
        }

        @Override
        public boolean isBound() {
            return true;
        }

        @Override
        public Optional<DynamicHolder<R>> getRandomElement(RandomSource random) {
            return this.holders.isEmpty() ? Optional.empty() : Optional.of(this.holders.get(random.nextInt(this.holders.size())));
        }

        @Override
        public String toString() {
            return "DirectSet[" + this.holders + "]";
        }
    }
}
