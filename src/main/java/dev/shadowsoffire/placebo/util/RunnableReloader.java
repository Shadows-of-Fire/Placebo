package dev.shadowsoffire.placebo.util;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Simple reload listener that allows for lambda usage.
 */
public class RunnableReloader extends SimplePreparableReloadListener<Object> {

    protected final Runnable r;

    public RunnableReloader(Runnable r) {
        this.r = r;
    }

    @Override
    protected Object prepare(ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        return null;
    }

    @Override
    protected void apply(Object objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        this.r.run();
    }

    public static RunnableReloader of(Runnable r) {
        return new RunnableReloader(r);
    }

}
