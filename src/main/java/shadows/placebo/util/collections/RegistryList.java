package shadows.placebo.util.collections;

import java.util.ArrayList;

import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class RegistryList<T extends IForgeRegistryEntry<T>> extends ArrayList<T> {

	private static final long serialVersionUID = -471665354174636694L;

	public void register(IForgeRegistry<T> reg) {
		for (T t : this)
			reg.register(t);
	}

}
