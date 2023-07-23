package shadows.placebo.json;

import net.minecraft.resources.ResourceLocation;
import shadows.placebo.json.PSerializer.PSerializable;

/**
 * An interface supporting ID's and Serializers with generic types.
 *
 * @param <V> This
 */
public interface TypeKeyed<V extends TypeKeyed<V>>extends PSerializable<V> {
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
    public static abstract class TypeKeyedBase<V extends TypeKeyed<V>> implements TypeKeyed<V> {
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
