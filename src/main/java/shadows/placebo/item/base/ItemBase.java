package shadows.placebo.item.base;

import net.minecraft.item.Item;
import shadows.placebo.client.IHasModel;
import shadows.placebo.registry.RegistryInformation;

public abstract class ItemBase extends Item implements IHasModel {

	public ItemBase(String name, RegistryInformation info) {
		setRegistryName(name);
		setUnlocalizedName(info.getID() + "." + name);
		setCreativeTab(info.getDefaultTab());
		info.getItemList().add(this);
	}

}
