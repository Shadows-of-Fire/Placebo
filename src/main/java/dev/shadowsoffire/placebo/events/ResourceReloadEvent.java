package dev.shadowsoffire.placebo.events;

import dev.shadowsoffire.placebo.config.Configuration;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;

/**
 * This event is fired whenever client or server resources are reloaded.
 * It can be used to subscribe to both types without needing register sided handlers, which is useful for reloading {@link Configuration} files.
 * <p>
 * Consumers of this event should not rely on its timing with respect to other {@link PreparableReloadListener}s.
 */
public class ResourceReloadEvent extends Event {

    protected final ResourceManager resourceManager;
    protected final LogicalSide side;

    public ResourceReloadEvent(ResourceManager resourceManager, LogicalSide side) {
        this.resourceManager = resourceManager;
        this.side = side;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public LogicalSide getSide() {
        return this.side;
    }

}
