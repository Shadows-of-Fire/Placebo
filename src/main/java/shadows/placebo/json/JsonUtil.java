package shadows.placebo.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.IForgeRegistry;

public class JsonUtil {

	public static <T> T getRegistryObject(JsonObject parent, String name, IForgeRegistry<T> registry) {
		String key = GsonHelper.getAsString(parent, name);
		T regObj = registry.getValue(new ResourceLocation(key));
		if (regObj == null) throw new JsonSyntaxException("Failed to parse " + registry.getRegistryName() + " object with key " + key);
		return regObj;
	}

	public static <T> Object makeSerializer(IForgeRegistry<T> reg) {
		return new SDS<>(reg);
	}

	/**
	 * Short for Serializer/Deserializer
	 */
	private static class SDS<T> implements JsonDeserializer<T>, JsonSerializer<T> {

		private final IForgeRegistry<T> reg;

		SDS(IForgeRegistry<T> reg) {
			this.reg = reg;
		}

		@Override
		public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(reg.getKey(src).toString());
		}

		@Override
		public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			T regObj = reg.getValue(new ResourceLocation(json.getAsString()));
			if (regObj == null) throw new JsonSyntaxException("Failed to parse " + reg.getRegistryName() + " object with key " + json.getAsString());
			return regObj;
		}

	}

}
