package shadows.placebo.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class JsonUtil {

	public static <T extends IForgeRegistryEntry<T>> T getRegistryObject(JsonObject parent, String name, IForgeRegistry<T> registry) {
		String key = GsonHelper.getAsString(parent, name);
		T regObj = registry.getValue(new ResourceLocation(key));
		if (regObj == null) throw new JsonSyntaxException("Failed to parse " + registry.getRegistryName() + " object with key " + key);
		return regObj;
	}

}
