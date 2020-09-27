package shadows.placebo.loot;

import java.util.function.Consumer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import shadows.placebo.Placebo;
import shadows.placebo.recipe.RecipeHelper;

public class StackLootEntry extends StandaloneLootEntry {
	public static final Serializer SERIALIZER = new Serializer();
	public static final LootPoolEntryType STACKLOOTENTRYTYPE = Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(Placebo.MODID, "stack_entry"), new LootPoolEntryType(SERIALIZER));

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

	@Override
	public LootPoolEntryType func_230420_a_() {
		return STACKLOOTENTRYTYPE;
	}

	public static class Serializer extends StandaloneLootEntry.Serializer<StackLootEntry> {

		@Override
		protected StackLootEntry deserialize(JsonObject jsonObject, JsonDeserializationContext context, int weight, int quality, ILootCondition[] lootConditions, ILootFunction[] lootFunctions) {
			int min = JSONUtils.getInt(jsonObject, "min", 1);
			int max = JSONUtils.getInt(jsonObject, "max", 1);
			int count = JSONUtils.getInt(jsonObject, "count", 1);
			Item item = JSONUtils.getItem(jsonObject, "item");
			ItemStack stack = new ItemStack(item, count);
			return new StackLootEntry(stack, min, max, weight, quality);
		}

	}
}
