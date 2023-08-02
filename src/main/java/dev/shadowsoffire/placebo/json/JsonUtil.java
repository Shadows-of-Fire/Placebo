package dev.shadowsoffire.placebo.json;

import java.lang.reflect.Type;

import org.apache.logging.log4j.Logger;

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
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.registries.IForgeRegistry;

public class JsonUtil {

    /**
     * Checks if an item is empty, and if it is, returns false and logs the key.
     */
    public static boolean checkAndLogEmpty(JsonElement e, ResourceLocation id, String type, Logger logger) {
        String s = e.toString();
        if (s.isEmpty() || "{}".equals(s)) {
            logger.error("Ignoring {} item with id {} as it is empty.  Please switch to a condition-false json instead of an empty one.", type, id);
            return false;
        }
        return true;
    }

    /**
     * Checks the conditions on a Json, and returns true if they are met.
     * Checks both 'conditions' and 'forge:conditions'
     *
     * @param e       The Json being checked.
     * @param id      The ID of that json.
     * @param type    The type of the json, for logging.
     * @param logger  The logger to log to.
     * @param context The context object used for resolving conditions.
     * @return True if the item's conditions are met, false otherwise.
     */
    public static boolean checkConditions(JsonElement e, ResourceLocation id, String type, Logger logger, IContext context) {
        if (!e.isJsonObject() || CraftingHelper.processConditions(e.getAsJsonObject(), "conditions", context) && CraftingHelper.processConditions(e.getAsJsonObject(), "forge:conditions", context)) {
            return true;
        }
        logger.trace("Skipping loading {} item with id {} as it's conditions were not met", type, id);
        return false;
    }

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
    private static class SDS<T> implements com.google.gson.JsonDeserializer<T>, com.google.gson.JsonSerializer<T> {

        private final IForgeRegistry<T> reg;

        SDS(IForgeRegistry<T> reg) {
            this.reg = reg;
        }

        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(this.reg.getKey(src).toString());
        }

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            T regObj = this.reg.getValue(new ResourceLocation(json.getAsString()));
            if (regObj == null) throw new JsonSyntaxException("Failed to parse " + this.reg.getRegistryName() + " object with key " + json.getAsString());
            return regObj;
        }

    }

    public static interface JsonSerializer<V> {
        public JsonObject write(V src);
    }

    public static interface JsonDeserializer<V> {
        public V read(JsonObject json);
    }

    public static interface NetSerializer<V> {
        public void write(V src, FriendlyByteBuf buf);
    }

    public static interface NetDeserializer<V> {
        public V read(FriendlyByteBuf buf);
    }

    private static record SDS2<T>(com.google.gson.JsonDeserializer<T> jds, com.google.gson.JsonSerializer<T> js) implements com.google.gson.JsonDeserializer<T>, com.google.gson.JsonSerializer<T> {

        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            return this.js.serialize(src, typeOfSrc, context);
        }

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return this.jds.deserialize(json, typeOfT, context);
        }

    }

    public static <T> Object makeSerializer(com.google.gson.JsonDeserializer<T> jds, com.google.gson.JsonSerializer<T> js) {
        return new SDS2<>(jds, js);
    }

}
