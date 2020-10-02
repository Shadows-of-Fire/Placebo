package shadows.placebo.util;

import net.minecraft.client.resources.ReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;

/**
 * Simple reload listener that allows for lambda usage.
 */
public class RunnableReloader extends ReloadListener<Object> {

	protected final Runnable r;

	public RunnableReloader(Runnable r) {
		this.r = r;
	}

	@Override
	protected Object prepare(IResourceManager resourceManagerIn, IProfiler profilerIn) {
		return null;
	}

	@Override
	protected void apply(Object objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn) {
		r.run();
	}

	public static RunnableReloader of(Runnable r) {
		return new RunnableReloader(r);
	}

}
