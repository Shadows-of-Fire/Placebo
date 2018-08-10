package shadows.placebo.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shadows.placebo.interfaces.IPropertyEnum;
import shadows.placebo.registry.RegistryInformation;
import shadows.placebo.util.PlaceboUtil;

public class ItemBigEnum<E extends Enum<E> & IPropertyEnum> extends ItemBase {

	public final E[] values;

	public ItemBigEnum(String name, E[] values, RegistryInformation info) {
		super(name, info);
		setHasSubtypes(true);
		this.values = values;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) for (E e : values)
			items.add(new ItemStack(this, 1, e.ordinal()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void initModels(ModelRegistryEvent ev) {
		for (E e : values)
			PlaceboUtil.sMRL("items", this, e.ordinal(), "item=" + e.getName());
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		return "item." + this.getRegistryName().getNamespace() + "." + values[stack.getMetadata() % values.length].getName();
	}

}
