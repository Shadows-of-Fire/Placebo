package dev.shadowsoffire.placebo.util.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.datagen.DataGenBuilder;
import dev.shadowsoffire.placebo.datagen.DataGenBuilder.DataProviderFactory;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import dev.shadowsoffire.placebo.reload.DynamicRegistry.DataGenPopulator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Data provider for objects registered to a {@link DynamicRegistry}.
 */
public abstract class DynamicRegistryProvider<R extends CodecProvider<R>> implements DataProvider {

    protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
    protected final PackOutput.PathProvider pathProvider;
    protected final DynamicRegistry<R> registry;
    protected final List<CompletableFuture<?>> futures = new ArrayList<>();

    private CachedOutput cachedOutput;
    private DataGenPopulator<R> populator;
    boolean skipGeneration = false;

    /**
     * Creates a new provider. Subclasses should create a public constructor that inlines the registry parameter.
     *
     * @param output     The pack output. The final output folder will be for a data pack using the registry path.
     * @param registries The registry lookup for this datagen instance.
     * @param registry   The registry for which objects are being generated for
     */
    public DynamicRegistryProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, DynamicRegistry<R> registry) {
        this.lookupProvider = registries;
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, registry.getPath());
        this.registry = registry;
    }

    /**
     * @deprecated Use {@link #DynamicRegistryProvider(PackOutput, CompletableFuture, DynamicRegistry)}
     */
    @Deprecated(forRemoval = true)
    public DynamicRegistryProvider(GatherDataEvent event, DynamicRegistry<R> registry) {
        this.lookupProvider = event.getLookupProvider();
        this.pathProvider = event.getGenerator().getPackOutput().createPathProvider(PackOutput.Target.DATA_PACK, registry.getPath());
        this.registry = registry;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput pOutput) {
        this.cachedOutput = pOutput;
        DataGenPopulator.runScoped(registry, populator -> {
            this.populator = populator;
            this.generate();
            this.populator = null;
        });
        return CompletableFuture.allOf(this.futures.toArray(CompletableFuture[]::new));
    }

    /**
     * Adds an individual object to this provider.
     *
     * @param id     The id of the object
     * @param object The object
     */
    protected final void add(ResourceLocation id, R object) {
        this.populator.register(id, object);
        if (!this.skipGeneration) {
            this.futures.add(this.lookupProvider.thenCompose(regs -> {
                DynamicOps<JsonElement> ops = regs.createSerializationContext(JsonOps.INSTANCE);
                return DataProvider.saveStable(this.cachedOutput, this.registry.elementCodec().encodeStart(ops, object).getOrThrow(), this.pathProvider.json(id));
            }));
        }
    }

    /**
     * Adds an individual object to this provider with a specified list of conditions.
     *
     * @param id         The id of the object
     * @param object     The object
     * @param conditions Conditions required for the object to load.
     */
    protected final void addConditionally(ResourceLocation id, R object, ICondition... conditions) {
        this.populator.register(id, object);
        Codec<Optional<WithConditions<R>>> conditionalCodec = net.neoforged.neoforge.common.conditions.ConditionalOps.<R>createConditionalCodecWithConditions(this.registry.elementCodec());
        if (!this.skipGeneration) {
            this.futures.add(this.lookupProvider.thenCompose(regs -> {
                DynamicOps<JsonElement> ops = regs.createSerializationContext(JsonOps.INSTANCE);
                Optional<WithConditions<R>> withConds = Optional.of(new WithConditions<>(Arrays.asList(conditions), object));
                return DataProvider.saveStable(this.cachedOutput, conditionalCodec.encodeStart(ops, withConds).getOrThrow(), this.pathProvider.json(id));
            }));
        }
    }

    /**
     * Generates all items provided by this provider.
     * <p>
     * Use {@link #add(ResourceLocation, CodecProvider)} to supply items.
     */
    public abstract void generate();

    /**
     * Binds a {@link DynamicRegistryProvider} in a way that ensures it will run "silently" and not generate any files.
     * <p>
     * This can be used by mods who need to generate dynamic registry entries from a dependency, but do not want to generate any files themselves.
     * Provided you have access to the datagen code for said dependency, anyway.
     * 
     * @param <R>     The registry type of the provider
     * @param <T>     The provider type
     * @param factory A method reference to the provider's constructor.
     * @return A re-bound factory that will skip generation. This can be passed to {@link DataGenBuilder#provider(DataProviderFactory)}.
     */
    public static <R extends CodecProvider<R>, T extends DynamicRegistryProvider<R>> DataProviderFactory<T> runSilently(DataProviderFactory<T> factory) {
        return (output, registries, fileHelper) -> {
            T provider = factory.create(output, registries, fileHelper);
            provider.skipGeneration = true;
            return provider;
        };
    }

}
