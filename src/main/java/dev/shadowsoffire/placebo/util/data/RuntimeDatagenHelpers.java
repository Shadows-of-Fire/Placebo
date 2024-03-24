package dev.shadowsoffire.placebo.util.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Code that allows for datagen of files at runtime.
 * <p>
 * Allows for normal datagen and custom datagen on-the-fly via commands.
 * <p>
 * Uses the run/datagen folder as the output directory.
 */
public class RuntimeDatagenHelpers {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Converts an object to json via codec.
     *
     * @throws IllegalStateException if the serialization fails
     */
    public static <T> JsonElement toJson(T object, Codec<T> codec) {
        return Util.getOrThrow(codec.encodeStart(JsonOps.INSTANCE, object), IllegalStateException::new);
    }

    /**
     * Converts a {@link CodecProvider} object to json via its provided codec.
     */
    @SuppressWarnings("unchecked")
    public static <T extends CodecProvider<T>> JsonElement toJson(T object) {
        return toJson(object, (Codec<T>) object.getCodec());
    }

    /**
     * Writes any codec-based object to the datagen directory.
     */
    public static <T> void write(T object, Codec<T> codec, String type, ResourceLocation key) {
        write(toJson(object, codec), type, key);
    }

    /**
     * Writes a {@link CodecProvider} object to the datagen directory.
     */
    @SuppressWarnings("unchecked")
    public static <T extends CodecProvider<T>> void write(T object, String type, ResourceLocation key) {
        write(toJson(object), type, key);
    }

    /**
     * Writes json to the specified location.
     * <p>
     * The path will be constructed as if it were the expected data path of the object: namespace/type/path.json
     *
     * @throws IllegalStateException if the disk write fails
     */
    public static void write(JsonElement json, String type, ResourceLocation key) {
        File file = new File(FMLPaths.GAMEDIR.get().toFile(), "datagen/" + key.getNamespace() + "/" + type + "/" + key.getPath() + ".json");
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            JsonWriter jWriter = new JsonWriter(writer);
            jWriter.setIndent("    ");
            GSON.toJson(json, jWriter);
        }
        catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
