package shadows.placebo.loot;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;

public class LambdaLootEntry extends StandaloneLootEntry {

	private final BiConsumer<Consumer<ItemStack>, LootContext> loot;

	public LambdaLootEntry(BiConsumer<Consumer<ItemStack>, LootContext> loot, int weight, int quality) {
		super(weight, quality, new ILootCondition[0], new ILootFunction[0]);
		this.loot = loot;
	}

	@Override
	protected void func_216154_a(Consumer<ItemStack> list, LootContext ctx) {
		loot.accept(list, ctx);
	}

}
