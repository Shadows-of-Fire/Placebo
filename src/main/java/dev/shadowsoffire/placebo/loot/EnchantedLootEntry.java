package dev.shadowsoffire.placebo.loot;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * StackLootEntry with the EnchantRandomly function pre-applied.
 */
@Deprecated
public class EnchantedLootEntry extends StackLootEntry {

    protected final LootItemFunction func = EnchantRandomlyFunction.randomApplicableEnchantment().build();

    public EnchantedLootEntry(ItemLike i, int weight) {
        super(new ItemStack(i), 1, 1, weight, 5, new LootItemCondition[0], new LootItemFunction[] { EnchantRandomlyFunction.randomApplicableEnchantment().build() });
    }

}
