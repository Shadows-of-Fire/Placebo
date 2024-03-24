package dev.shadowsoffire.placebo.json;

import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.Placebo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.attachment.AttachmentHolder;

public class ItemAdapter {

    public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            new OptionalItemMapCodec().forGetter(ItemStack::getItem),
            ExtraCodecs.strictOptionalField(Codec.intRange(1, 64), "count", 1).forGetter(ItemStack::getCount),
            ExtraCodecs.strictOptionalField(NBTAdapter.EITHER_CODEC, "nbt").forGetter(stack -> Optional.ofNullable(stack.getTag())),
            ExtraCodecs.strictOptionalField(NBTAdapter.EITHER_CODEC, AttachmentHolder.ATTACHMENTS_NBT_KEY).forGetter(s -> Optional.ofNullable(s.serializeAttachments())))
        .apply(inst, (item, count, nbt, capNbt) -> {
            var stack = new ItemStack(item, count, capNbt.orElse(null));
            stack.setTag(nbt.orElse(null));
            return stack;
        }));

    private static class OptionalItemMapCodec extends MapCodec<Item> {

        private final MapCodec<Item> encoder = BuiltInRegistries.ITEM.byNameCodec().fieldOf("item");
        private final MapCodec<ResourceLocation> idDecoder = ResourceLocation.CODEC.fieldOf("item");
        private final MapCodec<Boolean> optDecoder = ExtraCodecs.strictOptionalField(Codec.BOOL, "optional", false);

        @Override
        public <T> DataResult<Item> decode(DynamicOps<T> ops, MapLike<T> input) {
            ResourceLocation id = this.idDecoder.decode(ops, input).getOrThrow(false, Placebo.LOGGER::error);
            boolean optional = this.optDecoder.decode(ops, input).getOrThrow(false, Placebo.LOGGER::error);

            Item item = BuiltInRegistries.ITEM.get(id);
            if (!optional && item == Items.AIR && !id.equals(BuiltInRegistries.ITEM.getKey(Items.AIR))) return DataResult.error(() -> "Failed to read non-optional item " + id);
            return DataResult.success(item);
        }

        @Override
        public <T> RecordBuilder<T> encode(Item input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return this.encoder.encode(input, ops, prefix);
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.of(ops.createString("item"), ops.createString("optional"));
        }

    }

}
