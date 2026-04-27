package dev.shadowsoffire.placebo.mixin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;

import dev.shadowsoffire.placebo.datagen.FieldOrderingFactory;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;

@Mixin(value = DataProvider.class, remap = false)
public interface DataProviderMixin {

    /**
     * @author Shadows
     * @reason Supports FieldOrderingFactory.
     */
    @SuppressWarnings("deprecation")
    @Overwrite
    static CompletableFuture<?> saveStable(CachedOutput output, JsonElement json, Path path) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);

                try (JsonWriter jsonwriter = new JsonWriter(new OutputStreamWriter(hashingoutputstream, StandardCharsets.UTF_8))) {
                    jsonwriter.setSerializeNulls(false);
                    jsonwriter.setIndent(" ".repeat(java.lang.Math.max(0, DataProvider.INDENT_WIDTH.get()))); // Neo: Allow changing the indent width without needing to mixin this lambda.
                    GsonHelper.writeValue(jsonwriter, json, FieldOrderingFactory.Impl.getComparatorFor(json, path)); // Shadows: Allow per-object field ordering
                }

                output.writeIfNeeded(path, bytearrayoutputstream.toByteArray(), hashingoutputstream.hash());
            }
            catch (IOException ioexception) {
                DataProvider.LOGGER.error("Failed to save file to {}", path, ioexception);
            }
        }, Util.backgroundExecutor().forName("saveStable"));
    }

}
