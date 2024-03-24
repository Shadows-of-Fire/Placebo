package dev.shadowsoffire.placebo.util.data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Data provider for objects registered to a {@link DynamicRegistry}.
 */
public abstract class DynamicRegistryProvider<T extends CodecProvider<T>> implements DataProvider {

    protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
    protected final PackOutput.PathProvider pathProvider;
    protected final DynamicRegistry<T> registry;

    private List<CompletableFuture<?>> futures;
    private CachedOutput cachedOutput;

    /**
     * Creates a new provider.
     *
     * @param event    The gather data event
     * @param registry The registry for which objects are being generated for
     */
    protected DynamicRegistryProvider(GatherDataEvent event, DynamicRegistry<T> registry) {
        this.lookupProvider = event.getLookupProvider();
        this.pathProvider = event.getGenerator().getPackOutput().createPathProvider(PackOutput.Target.DATA_PACK, registry.getPath());
        this.registry = registry;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput pOutput) {
        this.futures = new ArrayList<>();
        this.cachedOutput = pOutput;
        this.generate();
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    /**
     * Adds an individual object to this provider.
     * 
     * @param id     The id of the object
     * @param object The object
     */
    @SuppressWarnings("unchecked")
    protected final void add(ResourceLocation id, T object) {
        this.futures.add(DataProvider.saveStable(this.cachedOutput, (Codec<T>) object.getCodec(), object, this.pathProvider.json(id)));
    }

    /**
     * Generates all items provided by this provider.
     * <p>
     * Use {@link #add(ResourceLocation, CodecProvider)} to supply items.
     */
    public abstract void generate();

}
