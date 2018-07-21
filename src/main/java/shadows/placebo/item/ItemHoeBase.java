package shadows.placebo.item;

import net.minecraft.item.ItemHoe;
import shadows.placebo.client.IHasModel;
import shadows.placebo.registry.RegistryInformation;

public class ItemHoeBase extends ItemHoe implements IHasModel {

	public ItemHoeBase(String name, RegistryInformation info, ToolMaterial material) {
		super(material);
		setRegistryName(name);
		setTranslationKey(info.getID() + "." + name);
		setCreativeTab(info.getDefaultTab());
		info.getItemList().add(this);
	}

}
