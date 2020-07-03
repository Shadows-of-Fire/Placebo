package shadows.placebo.loot;

import java.util.function.Consumer;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraftforge.registries.IForgeRegistryEntry;
import shadows.placebo.recipe.RecipeHelper;

public class StackLootEntry extends StandaloneLootEntry {

	private final ItemStack stack;
	private final int min;
	private final int max;

	public StackLootEntry(ItemStack stack, int min, int max, int weight, int quality) {
		super(weight, quality, new ILootCondition[0], new ILootFunction[0]);
		this.stack = stack;
		this.min = min;
		this.max = max;
	}

	public StackLootEntry(IForgeRegistryEntry<?> thing, int min, int max, int weight, int quality) {
		this(RecipeHelper.makeStack(thing), min, max, weight, quality);
	}

	public StackLootEntry(ItemStack stack) {
		this(stack, 1, 1, 1, 0);
	}

	@Override
	protected void func_216154_a(Consumer<ItemStack> list, LootContext ctx) {
		ItemStack s = stack.copy();
		s.setCount(MathHelper.nextInt(ctx.getRandom(), min, max));
		list.accept(s);
	}

}
