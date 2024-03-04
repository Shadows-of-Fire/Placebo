package dev.shadowsoffire.placebo.json;

import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
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
            new OptionalItemCodec().fieldOf("item").forGetter(ItemStack::getItem),
            ExtraCodecs.strictOptionalField(Codec.intRange(1, 64), "count", 1).forGetter(ItemStack::getCount),
            ExtraCodecs.strictOptionalField(NBTAdapter.EITHER_CODEC, "nbt").forGetter(stack -> Optional.ofNullable(stack.getTag())),
            ExtraCodecs.strictOptionalField(NBTAdapter.EITHER_CODEC, AttachmentHolder.ATTACHMENTS_NBT_KEY).forGetter(s -> Optional.ofNullable(s.serializeAttachments())))
        .apply(inst, (item, count, nbt, capNbt) -> {
            var stack = new ItemStack(item, count, capNbt.orElse(null));
            stack.setTag(nbt.orElse(null));
            return stack;
        }));

    public static class OptionalItemCodec implements Codec<Item> {

        @Override
        public <T> DataResult<T> encode(Item input, DynamicOps<T> ops, T prefix) {
            return BuiltInRegistries.ITEM.byNameCodec().encode(input, ops, prefix);
        }

        @Override
        public <T> DataResult<Pair<Item, T>> decode(DynamicOps<T> ops, T input) {
            ResourceLocation id = ResourceLocation.CODEC.decode(ops, ops.get(input, "item").getOrThrow(false, Placebo.LOGGER::error)).getOrThrow(false, Placebo.LOGGER::error).getFirst();
            Item item = BuiltInRegistries.ITEM.get(id);
            boolean optional = ops.get(input, "optional").result().map(j -> Codec.BOOL.decode(ops, j).map(Pair::getFirst).result()).map(o -> o.orElse(false)).orElse(false);
            if (!optional && item == Items.AIR && !id.equals(BuiltInRegistries.ITEM.getKey(Items.AIR))) return DataResult.error(() -> "Failed to read non-optional item " + id);
            return DataResult.success(Pair.of(item, input));
        }

    }

}
