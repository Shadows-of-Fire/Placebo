package shadows.placebo.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;

public class NBTAdapter implements JsonDeserializer<CompoundTag>, JsonSerializer<CompoundTag> {

	public static final NBTAdapter INSTANCE = new NBTAdapter();

	@Override
	public JsonElement serialize(CompoundTag src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.toString());
	}

	@Override
	public CompoundTag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		try {
			return TagParser.parseTag(json.getAsString());
		} catch (CommandSyntaxException e) {
			throw new JsonParseException(e);
		}
	}

}
