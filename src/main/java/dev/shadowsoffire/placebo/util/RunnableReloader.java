package dev.shadowsoffire.placebo.util;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * Simple reload listener that allows for lambda usage.
 */
public class RunnableReloader extends SimplePreparableReloadListener<Unit> {

    protected final Runnable r;

    public RunnableReloader(Runnable r) {
        this.r = r;
    }

    @Override
    protected Unit prepare(ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        return Unit.INSTANCE;
    }

    @Override
    protected void apply(Unit objectIn, ResourceManager resourceManagerIn, ProfilerFiller profilerIn) {
        this.r.run();
    }

    public static RunnableReloader of(Runnable r) {
        return new RunnableReloader(r);
    }

}
