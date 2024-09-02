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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Codec for {@link ItemStack} which supports declared optional itemstacks and explicit specification of `minecraft:air`.
 */
public class OptionalStackCodec {

    public static final Codec<ItemStack> INSTANCE = Codec.lazyInitialized(() -> RecordCodecBuilder.create(inst -> inst
        .group(
            new OptionalItemMapCodec().forGetter(ItemStack::getItemHolder),
            ExtraCodecs.intRange(1, Item.ABSOLUTE_MAX_STACK_SIZE).fieldOf("count").orElse(1).forGetter(ItemStack::getCount),
            DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemStack::getComponentsPatch))
        .apply(inst, ItemStack::new)));

    private static class OptionalItemMapCodec extends MapCodec<Holder<Item>> {

        private final MapCodec<Holder<Item>> encoder = BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id");
        private final MapCodec<ResourceLocation> idDecoder = ResourceLocation.CODEC.fieldOf("id");
        private final MapCodec<Boolean> optDecoder = Codec.BOOL.optionalFieldOf("optional", false);

        @Override
        public <T> DataResult<Holder<Item>> decode(DynamicOps<T> ops, MapLike<T> input) {
            ResourceLocation id = this.idDecoder.decode(ops, input).getOrThrow();
            boolean optional = this.optDecoder.decode(ops, input).getOrThrow();

            Optional<Holder.Reference<Item>> item = BuiltInRegistries.ITEM.getHolder(id);
            if (!optional && item.isEmpty()) {
                return DataResult.error(() -> "Failed to read non-optional item id " + id);
            }
            return DataResult.success(item.map(Function.<Holder<Item>>identity()).orElse(BuiltInRegistries.ITEM.wrapAsHolder(Items.AIR)));
        }

        @Override
        public <T> RecordBuilder<T> encode(Holder<Item> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return this.encoder.encode(input, ops, prefix);
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("id"), ops.createString("optional"));
        }

    }

}
