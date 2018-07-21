package shadows.placebo.item;

import net.minecraft.item.ItemSpade;
import shadows.placebo.client.IHasModel;
import shadows.placebo.registry.RegistryInformation;

public class ItemShovelBase extends ItemSpade implements IHasModel {

	public ItemShovelBase(String name, RegistryInformation info, ToolMaterial mat) {
		super(mat);
		setRegistryName(name);
		setTranslationKey(info.getID() + "." + name);
		setCreativeTab(info.getDefaultTab());
		info.getItemList().add(this);
	}
}
