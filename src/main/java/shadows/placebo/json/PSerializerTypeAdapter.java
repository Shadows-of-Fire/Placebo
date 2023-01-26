package shadows.placebo.json;

import java.lang.reflect.Type;

import com.google.common.collect.BiMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyed;

public class PSerializerTypeAdapter<V extends TypeKeyed<V>> implements JsonSerializer<V>, JsonDeserializer<V> {

	protected final BiMap<ResourceLocation, PSerializer<V>> serializers;

	public PSerializerTypeAdapter(BiMap<ResourceLocation, PSerializer<V>> serializers) {
		this.serializers = serializers;
	}

	@Override
	public V deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(obj, "type"));
		PSerializer<V> serializer = serializers.get(type);
		if (serializer == null) throw new JsonParseException("No serializer available for type: " + type);
		return serializer.read(obj);
	}

	@Override
	public JsonElement serialize(V src, Type typeOfSrc, JsonSerializationContext context) {
		PSerializer<V> serializer = src.getSerializer();
		ResourceLocation type = serializers.inverse().get(serializer);
		if (type == null) throw new JsonParseException("Attempted to serialize unregistered object: " + src.getId());
		return serializer.write(src);
	}

}
