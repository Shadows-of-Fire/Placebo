package shadows.placebo.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;

import net.minecraft.network.FriendlyByteBuf;
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

	@Deprecated
	public static <T> Object makeSerializer(IForgeRegistry<T> reg) {
		return new SDS<>(reg);
	}

	/**
	 * Short for Serializer/Deserializer
	 */
	@Deprecated
	private static class SDS<T> implements com.google.gson.JsonDeserializer<T>, com.google.gson.JsonSerializer<T> {

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

	@Deprecated
	public static interface JsonSerializer<V> {
		public JsonObject write(V src);
	}

	@Deprecated
	public static interface JsonDeserializer<V> {
		public V read(JsonObject json);
	}

	@Deprecated
	public static interface NetSerializer<V> {
		public void write(V src, FriendlyByteBuf buf);
	}

	@Deprecated
	public static interface NetDeserializer<V> {
		public V read(FriendlyByteBuf buf);
	}

	private static record SDS2<T>(com.google.gson.JsonDeserializer<T> jds, com.google.gson.JsonSerializer<T> js) implements com.google.gson.JsonDeserializer<T>, com.google.gson.JsonSerializer<T> {

		@Override
		public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
			return js.serialize(src, typeOfSrc, context);
		}

		@Override
		public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return jds.deserialize(json, typeOfT, context);
		}

	}

	@Deprecated
	public static <T> Object makeSerializer(com.google.gson.JsonDeserializer<T> jds, com.google.gson.JsonSerializer<T> js) {
		return new SDS2<>(jds, js);
	}

}
