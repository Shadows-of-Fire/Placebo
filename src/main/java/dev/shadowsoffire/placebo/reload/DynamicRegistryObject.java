package dev.shadowsoffire.placebo.reload;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;

public class DynamicRegistryObject<T extends TypeKeyed> implements Supplier<T> {

    protected final ResourceLocation id;
    protected final PlaceboJsonReloadListener<? super T> manager;

    protected T value;

    /**
     * Constructor for extension classes. Extenders need to ensure they wire up the invalidation hook appropriately.
     *
     * @see PlaceboJsonReloadListener#registryObject(ResourceLocation)
     */
    protected DynamicRegistryObject(ResourceLocation id, PlaceboJsonReloadListener<? super T> manager) {
        this.id = id;
        this.manager = manager;
    }

    /**
     * @return The target value.
     * @throws NullPointerException if the value is not {@linkplain #isPresent() present}.
     */
    @Override
    public T get() {
        this.bind();
        Objects.requireNonNull(this.value, "Trying to access unbound value: " + this.id);
        return this.value;
    }

    /**
     * @return an optional containing the target value if available, otherwise {@link Optional#empty()}.
     */
    public Optional<T> getOptional() {
        return this.isPresent() ? Optional.of(this.get()) : Optional.empty();
    }

    /**
     * @return The ID of the item being targetted
     */
    public ResourceLocation getId() {
        return this.id;
    }

    /**
     * @return If this object is present or not. Will attempt to resolve the object.
     */
    public boolean isPresent() {
        this.bind();
        return this.value != null;
    }

    /**
     * Binds this DynamicRegistryObject to the value stored in the registry.<br>
     * Does nothing if already bound.
     */
    @SuppressWarnings("unchecked")
    protected void bind() {
        if (this.value != null) return;
        this.value = (T) this.manager.getValue(this.id);
    }

    /**
     * Resets the contained value to null.<br>
     * Called when the manager reloads.
     */
    protected void reset() {
        this.value = null;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof DynamicRegistryObject dObj && dObj.manager == this.manager && dObj.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.manager);
    }

}
