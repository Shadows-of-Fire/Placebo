package dev.shadowsoffire.placebo.reload;

import dev.shadowsoffire.placebo.json.PSerializer.PSerializable;
import net.minecraft.resources.ResourceLocation;

/**
 * Provides get/set ID methods, used in {@link PlaceboJsonReloadListener}.
 */
public interface TypeKeyed {

    /**
     * Sets the ID of this object.
     *
     * @param id The object's ID.
     * @throws UnsupportedOperationException if the ID has already been set.
     */
    void setId(ResourceLocation id);

    /**
     * Returns the ID of this object.
     */
    ResourceLocation getId();

    /**
     * Intrusive base implementation of {@link TypeKeyed} which implements set/getId
     *
     * @param <V> This
     */
    public static abstract class TypeKeyedBase<V extends PSerializable<? super V>> implements TypeKeyed, PSerializable<V> {

        protected ResourceLocation id;

        @Override
        public void setId(ResourceLocation id) {
            if (this.id != null) throw new UnsupportedOperationException();
            this.id = id;
        }

        @Override
        public ResourceLocation getId() {
            return this.id;
        }

    }
}
