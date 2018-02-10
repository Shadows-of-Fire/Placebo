package shadows.placebo.item;

import net.minecraft.item.ItemSword;
import shadows.placebo.client.IHasModel;
import shadows.placebo.registry.RegistryInformation;

public class ItemSwordBase extends ItemSword implements IHasModel {

	public ItemSwordBase(String name, RegistryInformation info, ToolMaterial mat) {
		super(mat);
		setRegistryName(name);
		setUnlocalizedName(info.getID() + "." + name);
		setCreativeTab(info.getDefaultTab());
		info.getItemList().add(this);
	}
	
}
