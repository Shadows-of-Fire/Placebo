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
 * {@link DynamicRegistry}'s reload-event handler. The {@link #prepare} step scans tag files and resolves tag
 * references via {@link TagLoader} (off-thread, parallel to other reload listeners). The {@link #apply} step then
 * binds the resolved tags into each registry on the main thread.
 *
 * @see DynamicRegistry#bindTags(Map)
 */
public class DynamicTagManager extends SimplePreparableReloadListener<Map<DynamicRegistry<?>, Map<Identifier, List<Identifier>>>> {

    public static final Identifier ID = Placebo.loc("dynamic_registry_tags");

    public static final DynamicTagManager INSTANCE = new DynamicTagManager();

    @Override
    protected Map<DynamicRegistry<?>, Map<Identifier, List<Identifier>>> prepare(ResourceManager manager, ProfilerFiller profiler) {
        ConditionalOps<JsonElement> ops = this.makeConditionalOps();
        Map<DynamicRegistry<?>, Map<Identifier, List<Identifier>>> result = new IdentityHashMap<>();
        for (DynamicRegistry<?> registry : DynamicRegistry.allRegistries().values()) {
            TagLoader<?> loader = new TagLoader<>(registry, registry.getLogger());
            result.put(registry, loader.loadTags(manager, ops));
        }
        return result;
    }

    @Override
    protected void apply(Map<DynamicRegistry<?>, Map<Identifier, List<Identifier>>> data, ResourceManager manager, ProfilerFiller profiler) {
        for (Map.Entry<DynamicRegistry<?>, Map<Identifier, List<Identifier>>> entry : data.entrySet()) {
            DynamicRegistry<?> registry = entry.getKey();
            registry.bindTags(entry.getValue());
            if (!entry.getValue().isEmpty()) {
                registry.getLogger().info("Loaded {} tags for {}.", entry.getValue().size(), registry.getId());
            }
        }
    }
}
