package dev.shadowsoffire.placebo.util.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.placebo.datagen.DataGenBuilder;
import dev.shadowsoffire.placebo.datagen.DataGenBuilder.DataProviderFactory;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import dev.shadowsoffire.placebo.dynreg.tag.DynamicTagKey;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;

/**
 * Datagen provider for tags of a {@link DynamicRegistry}.
 * <p>
 * Subclass and override {@link #addTags()} to declare tags. Output JSON is written to
 * {@code data/<ns>/tags/<registry path>/<tag path>.json} using vanilla's {@link TagFile} codec — so the runtime
 * loader and tooling alike read the same shape vanilla tags use.
 * <p>
 * Tag providers must be registered after their corresponding {@link DynamicRegistryProvider} in the
 * {@link DataGenBuilder}; the tag provider validates references against the registry's in-memory state populated by
 * the content provider.
 *
 * @param <R> The element type of the target registry.
 */
public abstract class DynamicTagProvider<R> implements DataProvider {

    protected final CompletableFuture<HolderLookup.Provider> lookupProvider;
    protected final PackOutput.PathProvider pathProvider;
    protected final DynamicRegistry<R> registry;

    private final Map<DynamicTagKey<R>, TagBuilder> builders = new HashMap<>();

    boolean skipGeneration = false;

    /**
     * Creates a new tag provider.
     *
     * @param output     The pack output. The final output folder will be {@code data/<ns>/tags/<registry path>/}.
     * @param registries The registry lookup future from the datagen runner. Not used for validation, but required for
     *                   parity with vanilla's data provider shape.
     * @param registry   The registry whose tags are being generated.
     */
    public DynamicTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, DynamicRegistry<R> registry) {
        this.lookupProvider = registries;
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "tags/" + registry.getId().getNamespace() + "/" + registry.getId().getPath());
        this.registry = registry;
    }

    /**
     * Override and call {@link #tag(DynamicTagKey)} to declare tag contents.
     */
    protected abstract void addTags();

    /**
     * Returns the {@link TagAppender} for the given tag key, creating one if it does not yet exist.
     */
    protected final TagAppender tag(DynamicTagKey<R> key) {
        return this.builders.computeIfAbsent(key, k -> new TagBuilder(k)).appender;
    }

    @Override
    public final CompletableFuture<?> run(CachedOutput cachedOutput) {
        this.builders.clear();
        this.addTags();
        if (this.skipGeneration) {
            return CompletableFuture.completedFuture(null);
        }
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (TagBuilder builder : this.builders.values()) {
            this.validate(builder);
            if (builder.isEmpty()) continue;
            TagFile file = new TagFile(List.copyOf(builder.values), builder.replace, List.copyOf(builder.remove));
            JsonElement json = TagFile.CODEC.encodeStart(JsonOps.INSTANCE, file).getOrThrow();
            futures.add(DataProvider.saveStable(cachedOutput, json, this.pathProvider.json(builder.key.id())));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void validate(TagBuilder builder) {
        for (TagEntry entry : builder.values) {
            if (!entry.isTag() && entry.isRequired() && this.registry.getValue(entry.getId()) == null) {
                throw new IllegalStateException(
                    "Tag '%s' for registry '%s' references missing required entry '%s'. Did you register the tag provider before its content provider?"
                        .formatted(builder.key.id(), this.registry.getId(), entry.getId()));
            }
        }
    }

    /**
     * Fluent appender for a single tag's contents.
     */
    public final class TagAppender {

        private final TagBuilder builder;

        private TagAppender(TagBuilder builder) {
            this.builder = builder;
        }

        /**
         * Adds an entry by id. The entry must exist in the registry at datagen time, or {@link #run} will throw.
         */
        public TagAppender add(Identifier id) {
            this.builder.values.add(TagEntry.element(id));
            return this;
        }

        /**
         * Adds an entry by holder. Convenience that delegates to {@link #add(Identifier)} with {@code holder.getId()}.
         */
        public TagAppender add(DynamicHolder<R> holder) {
            return this.add(holder.getId());
        }

        /**
         * Adds an optional entry by id. Missing references are tolerated at load time.
         */
        public TagAppender addOptional(Identifier id) {
            this.builder.values.add(TagEntry.optionalElement(id));
            return this;
        }

        /**
         * Adds another tag's contents (a {@code "#tag_id"} reference).
         */
        public TagAppender addTag(DynamicTagKey<R> tag) {
            this.builder.values.add(TagEntry.tag(tag.id()));
            return this;
        }

        /**
         * Adds another tag's contents as an optional reference. Missing tags are tolerated.
         */
        public TagAppender addOptionalTag(DynamicTagKey<R> tag) {
            this.builder.values.add(TagEntry.optionalTag(tag.id()));
            return this;
        }

        /**
         * Marks an entry id for removal in this tag file.
         */
        public TagAppender remove(Identifier id) {
            this.builder.remove.add(TagEntry.element(id));
            return this;
        }

        /**
         * Marks another tag's contents for removal in this tag file.
         */
        public TagAppender remove(DynamicTagKey<R> tag) {
            this.builder.remove.add(TagEntry.tag(tag.id()));
            return this;
        }

        /**
         * Marks this tag file as replacing parent-pack contributions to the same tag.
         */
        public TagAppender replace() {
            this.builder.replace = true;
            return this;
        }
    }

    private final class TagBuilder {

        final DynamicTagKey<R> key;
        final List<TagEntry> values = new ArrayList<>();
        final List<TagEntry> remove = new ArrayList<>();
        boolean replace = false;
        final TagAppender appender;

        TagBuilder(DynamicTagKey<R> key) {
            this.key = key;
            this.appender = new TagAppender(this);
        }

        boolean isEmpty() {
            return this.values.isEmpty() && this.remove.isEmpty() && !this.replace;
        }
    }

    /**
     * Binds a {@link DynamicTagProvider} in a way that ensures it will run "silently" and not generate any files.
     * Symmetric with {@link DynamicRegistryProvider#runSilently(DataProviderFactory)}.
     */
    public static <R, T extends DynamicTagProvider<R>> DataProviderFactory<T> runSilently(DataProviderFactory<T> factory) {
        return (output, registries) -> {
            T provider = factory.create(output, registries);
            provider.skipGeneration = true;
            return provider;
        };
    }
}
