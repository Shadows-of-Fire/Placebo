package shadows.placebo.json;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import shadows.placebo.json.PlaceboJsonReloadListener.TypeKeyed;

@SuppressWarnings("rawtypes")
public class DynamicRegistryObject<T extends TypeKeyed<? super T>> implements Supplier<T>, ListenerCallback {

	protected final ResourceLocation id;
	protected final PlaceboJsonReloadListener<? super T> manager;

	protected T object;

	public DynamicRegistryObject(ResourceLocation id, PlaceboJsonReloadListener<? super T> manager) {
		this.id = id;
		this.manager = manager;
	}

	/**
	 * Returns the object, resolving it if necessary.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T get() {
		return object == null ? object = (T) manager.getValue(id) : object;
	}

	/**
	 * @return The ID of the item being targetted
	 */
	public ResourceLocation getId() {
		return this.id;
	}

	/**
	 * @return If this object is present or not.
	 */
	public boolean isPresent() {
		return object != null || manager.getValue(id) != null;
	}

	/**
	 * Invalidates the contained reference to the held object.
	 * Called when reloading so the registry object has to be refreshed.
	 */
	public void invalidate() {
		this.object = null;
	}

	/**
	 * Executes the given consumer iff this item is present.
	 */
	public void ifPresent(Consumer<? super T> consumer) {
		if (isPresent()) consumer.accept(get());
	}

	@Override
	public void beginReload(PlaceboJsonReloadListener manager) {
		this.invalidate();
	}

	@Override
	public void onReload(PlaceboJsonReloadListener manager) {
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DynamicRegistryObject dObj && dObj.manager == this.manager && dObj.id.equals(this.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.manager);
	}

}
