package shadows.placebo.json;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemAdapter implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {

	public static final ItemAdapter INSTANCE = new ItemAdapter();

	public static final Gson ITEM_READER = new GsonBuilder().registerTypeAdapter(ItemStack.class, INSTANCE).registerTypeAdapter(CompoundTag.class, NBTAdapter.INSTANCE).registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

	//Formatter::off
	public static final Codec<ItemStack> CODEC = RecordCodecBuilder.create(inst -> inst
		.group(
			ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(ItemStack::getItem),
			Codec.intRange(0, 64).optionalFieldOf("count", 1).forGetter(ItemStack::getCount),
			CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(stack -> Optional.of(stack.getTag())),
			CompoundTag.CODEC.optionalFieldOf("cap_nbt").forGetter(stack -> Optional.of(stack.getTag())))
			.apply(inst, (item, count, nbt, capNbt) -> {var stack = new ItemStack(item, count, capNbt.orElse(null)); stack.setTag(nbt.orElse(null)); return stack;})
		);
	//Formatter::on

	@Override
	public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		ResourceLocation id = ctx.deserialize(obj.get("item"), ResourceLocation.class);
		Item item = ForgeRegistries.ITEMS.getValue(id);
		boolean optional = obj.has("optional") ? obj.get("optional").getAsBoolean() : false;
		if (!optional && item == Items.AIR && !id.equals(ForgeRegistries.ITEMS.getKey(Items.AIR))) throw new JsonParseException("Failed to read non-optional item " + id);
		int count = obj.has("count") ? obj.get("count").getAsInt() : 1;
		CompoundTag tag = null;
		if (obj.has("nbt")) {
			var nbt = obj.get("nbt");
			if (nbt.isJsonObject()) {
				tag = CompoundTag.CODEC.decode(JsonOps.INSTANCE, nbt).get().orThrow().getFirst();
			} else {
				tag = ctx.deserialize(obj.get("nbt"), CompoundTag.class);
			}
		}
		CompoundTag capTag = obj.has("cap_nbt") ? ctx.deserialize(obj.get("cap_nbt"), CompoundTag.class) : null;
		ItemStack stack = new ItemStack(item, count, capTag);
		stack.setTag(tag);
		return stack;
	}

	@Override
	public JsonElement serialize(ItemStack stack, Type typeOfSrc, JsonSerializationContext ctx) {
		CompoundTag written = stack.save(new CompoundTag());
		JsonObject obj = new JsonObject();
		obj.add("item", ctx.serialize(ForgeRegistries.ITEMS.getKey(stack.getItem())));
		obj.add("count", ctx.serialize(stack.getCount()));
		if (stack.hasTag()) obj.add("nbt", ctx.serialize(stack.getTag()));
		if (written.contains("ForgeCaps")) obj.add("cap_nbt", ctx.serialize(written.getCompound("ForgeCaps")));
		return obj;
	}

	public static ItemStack readStack(JsonElement obj) {
		return ITEM_READER.fromJson(obj, ItemStack.class);
	}

	public static List<ItemStack> readStacks(JsonElement obj) {
		return ITEM_READER.fromJson(obj, new TypeToken<List<ItemStack>>() {
		}.getType());
	}

}