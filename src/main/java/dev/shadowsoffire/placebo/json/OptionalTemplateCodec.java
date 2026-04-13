package dev.shadowsoffire.placebo.json;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

/**
 * Codec for {@link ItemStackTemplate} which supports declared optional templates.
 * <p>
 * Because {@link ItemStackTemplate} cannot represent an empty value, this codec produces
 * {@code Optional<ItemStackTemplate>}: a missing or {@code minecraft:air} item combined with {@code "optional": true}
 * decodes to {@link Optional#empty()}, and round-trips back to {@code {"id": "minecraft:air", "optional": true}}.
 */
public class OptionalTemplateCodec {

    public static final Codec<Optional<ItemStackTemplate>> INSTANCE = Codec.lazyInitialized(() -> RecordCodecBuilder.create(inst -> inst
        .group(
            new OptionalItemMapCodec().forGetter(opt -> opt.map(ItemStackTemplate::item)),
            ExtraCodecs.intRange(1, Item.ABSOLUTE_MAX_STACK_SIZE).fieldOf("count").orElse(1).forGetter(opt -> opt.map(ItemStackTemplate::count).orElse(1)),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(opt -> opt.map(ItemStackTemplate::components).orElse(DataComponentPatch.EMPTY)))
        .apply(inst, (item, count, components) -> item.map(h -> new ItemStackTemplate(h, count, components)))));

    private static class OptionalItemMapCodec extends MapCodec<Optional<Holder<Item>>> {

        private final MapCodec<Holder<Item>> encoder = BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id");
        private final MapCodec<Identifier> idDecoder = Identifier.CODEC.fieldOf("id");
        private final MapCodec<Boolean> optDecoder = Codec.BOOL.optionalFieldOf("optional", false);

        @Override
        public <T> DataResult<Optional<Holder<Item>>> decode(DynamicOps<T> ops, MapLike<T> input) {
            Identifier id = this.idDecoder.decode(ops, input).getOrThrow();
            boolean optional = this.optDecoder.decode(ops, input).getOrThrow();

            Optional<Holder.Reference<Item>> item = BuiltInRegistries.ITEM.get(id);
            boolean isAir = item.isPresent() && item.get().value() == Items.AIR;
            if (item.isEmpty() || isAir) {
                if (!optional) {
                    return DataResult.error(() -> "Failed to read non-optional item id " + id);
                }
                return DataResult.success(Optional.empty());
            }
            return DataResult.success(item.map(Function.<Holder<Item>>identity()));
        }

        @Override
        public <T> RecordBuilder<T> encode(Optional<Holder<Item>> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            if (input.isEmpty()) {
                RecordBuilder<T> builder = this.encoder.encode(BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR), ops, prefix);
                return builder.add("optional", ops.createBoolean(true));
            }
            return this.encoder.encode(input.get(), ops, prefix);
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("id"), ops.createString("optional"));
        }

    }

}
