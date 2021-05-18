package shadows.placebo.loot;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootPoolEntryType;
import net.minecraft.loot.StandaloneLootEntry;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import shadows.placebo.Placebo;

public class LambdaLootEntry extends StandaloneLootEntry {
	public static final Serializer SERIALIZER = new Serializer();
	public static final LootPoolEntryType LAMBDALOOTENTRYTYPE = Registry.register(Registry.LOOT_POOL_ENTRY_TYPE, new ResourceLocation(Placebo.MODID, "lambda_entry"), new LootPoolEntryType(SERIALIZER));

	private final BiConsumer<Consumer<ItemStack>, LootContext> loot;

	public LambdaLootEntry(BiConsumer<Consumer<ItemStack>, LootContext> loot, int weight, int quality) {
		super(weight, quality, new ILootCondition[0], new ILootFunction[0]);
		this.loot = loot;
	}

	@Override
	protected void func_216154_a(Consumer<ItemStack> list, LootContext ctx) {
		this.loot.accept(list, ctx);
	}

	@Override
	public LootPoolEntryType func_230420_a_() {
		return LAMBDALOOTENTRYTYPE;
	}

	public static class Serializer extends StandaloneLootEntry.Serializer<LambdaLootEntry> {

		@Override
		protected LambdaLootEntry deserialize(JsonObject jsonObject, JsonDeserializationContext context, int weight, int quality, ILootCondition[] lootConditions, ILootFunction[] lootFunctions) {
			throw new UnsupportedOperationException();
		}

	}
}
