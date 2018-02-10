package shadows.placebo.item;

import net.minecraft.item.ItemPickaxe;
import shadows.placebo.client.IHasModel;
import shadows.placebo.registry.RegistryInformation;

public class ItemPickaxeBase extends ItemPickaxe implements IHasModel {

	public ItemPickaxeBase(String name, RegistryInformation info, ToolMaterial mat) {
		super(mat);
		setRegistryName(name);
		setUnlocalizedName(info.getID() + "." + name);
		setCreativeTab(info.getDefaultTab());
		info.getItemList().add(this);
	}

}
