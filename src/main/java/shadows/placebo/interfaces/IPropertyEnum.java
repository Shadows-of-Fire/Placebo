package shadows.placebo.interfaces;

import java.util.Locale;

import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IPropertyEnum extends IStringSerializable {

	@Override
	default public String getName() {
		return ((Enum<?>) this).name().toLowerCase(Locale.ENGLISH);
	}

	default public int getPredicateIndex() {
		return ((Enum<?>) this).ordinal() / 16;
	};

	default public boolean useForRecipes() {
		return false;
	}

	default public int getMetadata() {
		return ((Enum<?>) this).ordinal() % 16;
	}

	/*
	 * Returns the value of this Enum constant as an ItemStack, or ItemStack.EMPTY, if invalid.
	 */
	public ItemStack get();

	default public ItemStack get(int size) {
		ItemStack s = get();
		s.setCount(size);
		return s;
	}

	default public void set(IForgeRegistryEntry<?> thing) {
	}

}
