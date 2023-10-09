package dev.shadowsoffire.placebo.reload;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

/**
 * A Dynamic Holder is an implementation of {@link Holder} for {@link DynamicRegistry}.<br>
 * As compared with normal holders, it can be invalidated, as the reload listeners can reload at runtime.
 * <p>
 * Dynamic Holders are interned, and as such are reference (==) comparable with others from the same registry.
 *
 * @param <T> The type of the target value.
 */
public class DynamicHolder<T> implements Supplier<T> {

    /**
     * The ID of an "empty" holder.
     * 
     * @see DynamicRegistry#emptyHolder()
     */
    public static final ResourceLocation EMPTY = new ResourceLocation("empty", "empty");

    protected final DynamicRegistry<? super T> registry;
    protected final ResourceLocation id;

    /**
     * The current data. Null when unbound.
     */
    @Nullable
    protected T value;

    /**
     * @see DynamicRegistry#holder(ResourceLocation)
     */
    DynamicHolder(DynamicRegistry<? super T> registry, ResourceLocation id) {
        this.id = id;
        this.registry = registry;
    }

    /**
     * Checks if the target value is present in the registry, resolving it if possible.
     * 
     * @return True, if the value is present, and {@link #get()} may be called.
     */
    public boolean isBound() {
        bind();
        return this.value != null;
    }

    /**
     * Gets the value, if available. This method will resolve the value if possible.
     * 
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
     * @return An optional containing the target value if this {@link #isBound()}, otherwise {@link Optional#empty()}.
     */
    public Optional<T> getOptional() {
        return this.isBound() ? Optional.of(this.value()) : Optional.empty();
    }

    /**
     * @return The ID of the target value.
     */
    public ResourceLocation getId() {
        return this.id;
    }

    /**
     * Checks if this holder is targetting the value with the specified id.
     * 
     * @param id The id to check against.
     * @return True, if the passed id equals the target id.
     */
    public boolean is(ResourceLocation id) {
        return this.id.equals(id);
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof DynamicHolder dh && dh.registry == this.registry && dh.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.registry);
    }

    /**
     * Binds this DynamicRegistryObject to the value stored in the registry.<br>
     * Does nothing if already bound.
     */
    @SuppressWarnings("unchecked")
    void bind() {
        if (this.value != null) return;
        this.value = (T) this.registry.getValue(this.id);
    }

    /**
     * Resets the contained value to null.<br>
     * Called when the manager reloads.
     */
    void unbind() {
        this.value = null;
    }

    @Deprecated(forRemoval = true)
    public T value() {
        return this.get();
    }

}
