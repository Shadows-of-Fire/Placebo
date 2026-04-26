package dev.shadowsoffire.placebo.dynreg.tag;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.function.Consumer;

import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;

import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.util.DependencySorter;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

/**
 * Loads tag JSON files for a single {@link DynamicRegistry} and resolves them into a flat map of tag id → entry id list.
 * <p>
 * Reuses vanilla {@link TagFile}, {@link TagEntry}, and {@link DependencySorter}. Retains Neo's {@code "remove"}
 * semantics and {@code "required: false"} on individual entries. Cyclic tag references are silently
 * dropped (matching vanilla). Missing required entries log an error and the tag is skipped.
 */
public final class TagLoader<R> {

    private final DynamicRegistry<R> registry;
    private final String directory;
    private final Logger logger;

    public TagLoader(DynamicRegistry<R> registry, Logger logger) {
        this.registry = registry;
        this.directory = "tags/" + registry.getId().getNamespace() + "/" + registry.getId().getPath();
        this.logger = logger;
    }

    /**
     * Walks the datapack and resolves every tag file under {@code data/<ns>/tags/<registry path>/}.
     *
     * @param manager The active resource manager.
     * @param ops     Conditional ops for honoring {@code neoforge:conditions} on tag files.
     * @return Resolved map of tag id → list of entry ids, in declared order.
     */
    public Map<Identifier, List<Identifier>> loadTags(ResourceManager manager, ConditionalOps<JsonElement> ops) {
        Map<Identifier, List<EntryWithSource>> raw = this.scan(manager, ops);
        return this.resolve(raw);
    }

    private Map<Identifier, List<EntryWithSource>> scan(ResourceManager manager, ConditionalOps<JsonElement> ops) {
        Map<Identifier, List<EntryWithSource>> result = new HashMap<>();
        FileToIdConverter lister = FileToIdConverter.json(this.directory);
        for (Map.Entry<Identifier, List<Resource>> entry : lister.listMatchingResourceStacks(manager).entrySet()) {
            Identifier location = entry.getKey();
            Identifier id = lister.fileToId(location);
            for (Resource resource : entry.getValue()) {
                try (Reader reader = resource.openAsReader()) {
                    JsonElement element = JsonParser.parseReader(reader);
                    TagFile parsed = TagFile.CODEC.parse(new Dynamic<>(ops, element)).getOrThrow();
                    List<EntryWithSource> entries = result.computeIfAbsent(id, k -> new ArrayList<>());
                    if (parsed.replace()) {
                        entries.clear();
                    }
                    String sourceId = resource.sourcePackId();
                    parsed.entries().forEach(e -> entries.add(new EntryWithSource(e, sourceId, false)));
                    parsed.remove().forEach(e -> entries.add(new EntryWithSource(e, sourceId, true)));
                }
                catch (Exception ex) {
                    this.logger.error("Couldn't read tag list {} from {} in pack {}", id, location, resource.sourcePackId(), ex);
                }
            }
        }
        return result;
    }

    private Map<Identifier, List<Identifier>> resolve(Map<Identifier, List<EntryWithSource>> raw) {
        Map<Identifier, List<Identifier>> resolved = new HashMap<>();
        TagEntry.Lookup<Identifier> lookup = new TagEntry.Lookup<>(){

            @Override
            public Identifier element(Identifier key, boolean required) {
                return TagLoader.this.registry.getValue(key) != null ? key : null;
            }

            @Override
            public Collection<Identifier> tag(Identifier key) {
                return resolved.get(key);
            }
        };

        DependencySorter<Identifier, SortingEntry> sorter = new DependencySorter<>();
        raw.forEach((id, entries) -> sorter.addEntry(id, new SortingEntry(entries)));

        sorter.orderByDependencies((id, sortingEntry) -> {
            SequencedSet<Identifier> values = new LinkedHashSet<>();
            List<EntryWithSource> missing = new ArrayList<>();
            for (EntryWithSource e : sortingEntry.entries()) {
                if (!e.build(lookup, values)) {
                    missing.add(e);
                }
            }
            if (!missing.isEmpty()) {
                this.logger.error(
                    "Couldn't load tag {}/{} due to missing references: {}",
                    this.directory, id, missing);
            }
            resolved.put(id, List.copyOf(values));
        });

        return resolved;
    }

    /**
     * A single tag entry with its source pack id, and a flag indicating whether this entry is from the
     * {@code "remove"} list (in which case missing references are tolerated and the entry is subtracted
     * from the accumulated set rather than added).
     */
    public record EntryWithSource(TagEntry entry, String source, boolean remove) {

        /**
         * Applies this entry to the given accumulator. {@code add} entries call into vanilla's
         * {@link TagEntry#build} (which returns false on missing required references). {@code remove}
         * entries silently subtract; missing references are tolerated.
         *
         * @return True if the entry was applied successfully, false if a required reference was missing.
         */
        boolean build(TagEntry.Lookup<Identifier> lookup, SequencedSet<Identifier> accumulator) {
            if (this.remove) {
                if (this.entry.isTag()) {
                    Collection<Identifier> contents = lookup.tag(this.entry.getId());
                    if (contents != null) contents.forEach(accumulator::remove);
                }
                else {
                    accumulator.remove(this.entry.getId());
                }
                return true;
            }
            return this.entry.build(lookup, (Consumer<Identifier>) accumulator::add);
        }

        @Override
        public String toString() {
            return (this.remove ? "-" : "+") + this.entry + " (from " + this.source + ")";
        }
    }

    private record SortingEntry(List<EntryWithSource> entries) implements DependencySorter.Entry<Identifier> {

        @Override
        public void visitRequiredDependencies(Consumer<Identifier> output) {
            this.entries.forEach(e -> e.entry().visitRequiredDependencies(output));
        }

        @Override
        public void visitOptionalDependencies(Consumer<Identifier> output) {
            this.entries.forEach(e -> e.entry().visitOptionalDependencies(output));
        }
    }
}
