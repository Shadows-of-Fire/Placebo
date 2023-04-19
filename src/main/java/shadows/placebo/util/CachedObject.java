package shadows.placebo.util;

import java.util.function.Function;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * A Cached Object is an immutable object which is stored in ItemStack NBT, but stored in Object form.<br>
 * The live object is (or should be considered) immutable.<br>
 * Changes to the ItemStack's NBT will result in the object being deserialized again.
 *
 * @param <T> The type of object being cached.
 */
public final class CachedObject<T> {

	public static final int HAS_NEVER_BEEN_INITIALIZED = -2;
	public static final int EMPTY_NBT = -1;

	protected final ResourceLocation id;
	protected final Function<ItemStack, T> deserializer;
	protected final ToIntFunction<ItemStack> hasher;

	protected T data = null;
	protected int lastNbtHash = HAS_NEVER_BEEN_INITIALIZED;

	/**
	 * Creates a new CachedObject.
	 * @param id The ID of this object.
	 * @param deserializer The deserialization function. May return null. The stack passed to the function may be empty.
	 * @param hasher A Function which can generate a hash from the relevant itemstack data.
	 */
	public CachedObject(ResourceLocation id, Function<ItemStack, T> deserializer, ToIntFunction<ItemStack> hasher) {
		this.id = id;
		this.deserializer = deserializer;
		this.hasher = hasher;
	}

	/**
	 * Creates a new CachedObject.
	 * @param id The ID of this object.
	 * @param deserializer The deserialization function. May return null. The stack passed to the function may be empty.
	 */
	public CachedObject(ResourceLocation id, Function<ItemStack, T> deserializer) {
		this(id, deserializer, CachedObject::defaultHash);
	}

	@Nullable
	public T get(ItemStack stack) {
		if (this.lastNbtHash == HAS_NEVER_BEEN_INITIALIZED) {
			refresh(stack);
			return this.data;
		}

		if (this.hasher.applyAsInt(stack) != this.lastNbtHash) {
			refresh(stack);
		}

		return data;
	}

	protected void refresh(ItemStack stack) {
		this.data = deserializer.apply(stack);
		this.lastNbtHash = this.hasher.applyAsInt(stack);
	}

	/**
	 * The default hash function hashes the entire compound tag.
	 */
	public static int defaultHash(ItemStack stack) {
		return stack.hasTag() ? stack.getTag().hashCode() : EMPTY_NBT;
	}

	/**
	 * Cast ItemStack to this interface.
	 */
	public interface CachedObjectSource {

		public <T> T getOrCreate(ResourceLocation id, Function<ItemStack, T> deserializer, ToIntFunction<ItemStack> hasher);

		public default <T> T getOrCreate(ResourceLocation id, Function<ItemStack, T> deserializer) {
			return getOrCreate(id, deserializer, CachedObject::defaultHash);
		}

		public static <T> T getOrCreate(ItemStack stack, ResourceLocation id, Function<ItemStack, T> deserializer, ToIntFunction<ItemStack> hasher) {
			return ((CachedObjectSource) (Object) stack).getOrCreate(id, deserializer, hasher);
		}

		public static <T> T getOrCreate(ItemStack stack, ResourceLocation id, Function<ItemStack, T> deserializer) {
			return ((CachedObjectSource) (Object) stack).getOrCreate(id, deserializer);
		}

	}

}
