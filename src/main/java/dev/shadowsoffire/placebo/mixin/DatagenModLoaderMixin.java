package dev.shadowsoffire.placebo.mixin;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.shadowsoffire.placebo.datagen.FieldOrderingFactory;
import net.minecraft.data.DataGenerator;
import net.minecraft.server.packs.PackResources;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;

/**
 * Captures the datagen output root so {@link FieldOrderingFactory.Impl#getPackRoot()} can strip it
 * from absolute write paths before they are decomposed by
 * {@link dev.shadowsoffire.placebo.datagen.FilteredOrderingFactory.ParsedPath#parse}.
 * <p>
 * If we don't do this, we can't really identify the components of the path being written. We'd run into problems if someone had their mod source in
 * /home/data/...
 */
@Mixin(value = DatagenModLoader.class, remap = false)
public class DatagenModLoaderMixin {

    @Inject(method = "begin", at = @At("HEAD"), require = 1)
    private static void placebo$capturePackRoot(Set<String> mods, Path path, Collection<Path> inputs, Collection<Path> existingPacks, boolean devToolGenerators, boolean reportsGenerator,
        boolean structureValidator, boolean flat, Runnable setup, GatherDataEvent.GatherDataEventGenerator eventGenerator, DataGenerator vanillaGenerator,
        Consumer<Consumer<PackResources>> vanillaClientAssets, CallbackInfo ci) {
        FieldOrderingFactory.Impl.setPackRoot(path);
    }

}
