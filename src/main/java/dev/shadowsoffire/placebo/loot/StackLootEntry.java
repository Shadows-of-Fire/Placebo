package dev.shadowsoffire.placebo.loot;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.json.OptionalStackCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * The StackLootEntry is a combination of the ItemEntry, alongside the SetCount and SetNBT loot functions that makes loot tables a bit easier to read.
 * The rolling in of SetCount is achieved by having native "min" and "max" fields, and the SetNBT is rolled into the actual item object, which is a full
 * ItemStack that can hold NBT data.
 */
public class StackLootEntry extends LootPoolSingletonContainer {

    public static final MapCodec<StackLootEntry> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
        .group(
            OptionalStackCodec.INSTANCE.fieldOf("stack").forGetter(e -> e.stack),
            Codec.intRange(0, 64).fieldOf("min").forGetter(e -> e.min),
            Codec.intRange(0, 64).fieldOf("max").forGetter(e -> e.max))
        .and(singletonFields(inst))
        .apply(inst, StackLootEntry::new));

    private final ItemStack stack;
    private final int min;
    private final int max;

    public StackLootEntry(ItemStack stack, int min, int max, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.stack = stack;
        this.min = min;
        this.max = max;
    }

    public StackLootEntry(ItemStack stack, int min, int max, int weight, int quality) {
        this(stack, min, max, weight, quality, List.of(), List.of());
    }

    public StackLootEntry(ItemLike item, int min, int max, int weight, int quality) {
        this(new ItemStack(item), min, max, weight, quality);
    }

    public StackLootEntry(ItemStack stack) {
        this(stack, 1, 1, 1, 0);
    }

    @Override
    protected void createItemStack(Consumer<ItemStack> list, LootContext ctx) {
        ItemStack s = this.stack.copy();
        s.setCount(Mth.nextInt(ctx.getRandom(), this.min, this.max));
        list.accept(s);
    }

    @Override
    public MapCodec<StackLootEntry> codec() {
        return CODEC;
    }

}
