package dev.shadowsoffire.placebo.dynreg.tag;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.dynreg.DynamicRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

/**
 * Reload listener responsible for loading tag JSON files for every constructed {@link DynamicRegistry}.
 * <p>
 * Runs after every {@code DynamicRegistry} reload listener via dependency edges added in
 * {@link DynamicRegistry}'s reload-event handler. The {@link #prepare} step scans tag JSON files
 * (off-thread, parallel to other reload listeners' prepare phases). The {@link #apply} step resolves
 * the scanned entries against the now-populated registries and binds the resolved tags — resolution must
 * happen during apply because preparation runs in parallel with content listeners' prepare and the registry
 * content isn't yet populated at that point.
 *
 * @see DynamicRegistry#bindTags(Map)
 */
public class DynamicTagManager extends SimplePreparableReloadListener<Map<DynamicRegistry<?>, ScannedTags<?>>> {

    public static final Identifier ID = Placebo.loc("dynamic_registry_tags");

    public static final DynamicTagManager INSTANCE = new DynamicTagManager();

    @Override
    protected Map<DynamicRegistry<?>, ScannedTags<?>> prepare(ResourceManager manager, ProfilerFiller profiler) {
        ConditionalOps<JsonElement> ops = this.makeConditionalOps();
        Map<DynamicRegistry<?>, ScannedTags<?>> result = new IdentityHashMap<>();
        for (DynamicRegistry<?> registry : DynamicRegistry.allRegistries().values()) {
            result.put(registry, scanFor(registry, manager, ops));
        }
        return result;
    }

    private static <R> ScannedTags<R> scanFor(DynamicRegistry<R> registry, ResourceManager manager, ConditionalOps<JsonElement> ops) {
        TagLoader<R> loader = new TagLoader<>(registry, registry.getLogger());
        return new ScannedTags<>(loader, loader.scan(manager, ops));
    }

    @Override
    protected void apply(Map<DynamicRegistry<?>, ScannedTags<?>> data, ResourceManager manager, ProfilerFiller profiler) {
        for (Map.Entry<DynamicRegistry<?>, ScannedTags<?>> entry : data.entrySet()) {
            DynamicRegistry<?> registry = entry.getKey();
            Map<Identifier, List<Identifier>> resolved = entry.getValue().resolve();
            registry.bindTags(resolved);
            if (!resolved.isEmpty()) {
                registry.getLogger().info("Loaded {} tags for {}.", resolved.size(), registry.getId());
            }
        }
    }
}
