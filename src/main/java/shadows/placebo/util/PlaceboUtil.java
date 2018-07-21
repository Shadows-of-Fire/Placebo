package shadows.placebo.util;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryEntry;

public class PlaceboUtil {

	public static void sMRL(Item k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(k, meta, new ModelResourceLocation(k.getRegistryName(), variant));
	}

	public static void sMRL(Block k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(k), meta, new ModelResourceLocation(k.getRegistryName(), variant));
	}

	public static void sMRL(String statePath, Block k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(k), meta, new ModelResourceLocation(k.getRegistryName().getNamespace() + ":" + statePath, variant));
	}

	public static void sMRL(String statePath, Item k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(k, meta, new ModelResourceLocation(k.getRegistryName().getNamespace() + ":" + statePath, variant));
	}

	public static void sMRL(String domain, String statePath, Block k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(k), meta, new ModelResourceLocation(domain + ":" + statePath, variant));
	}

	public static void sMRL(String domain, String statePath, Item k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(k, meta, new ModelResourceLocation(domain + ":" + statePath, variant));
	}

	public static boolean isOwnedBy(IForgeRegistryEntry<?> thing, String owner) {
		return thing.getRegistryName().getNamespace().equals(owner);
	}

	public static Item getItemByName(String regname) {
		return ForgeRegistries.ITEMS.getValue(new ResourceLocation(regname));
	}

	public static Item[] getItemsByNames(String... regnames) {
		Item[] items = new Item[regnames.length];
		for (int i = 0; i < regnames.length; i++) {
			items[i] = ForgeRegistries.ITEMS.getValue(new ResourceLocation(regnames[i]));
		}
		return items;
	}

	public static void setRegNameIllegally(IForgeRegistryEntry<?> entry, String name) {
		Loader l = Loader.instance();
		ModContainer k = l.activeModContainer();
		l.setActiveModContainer(l.getMinecraftModContainer());
		entry.setRegistryName(new ResourceLocation("minecraft", name));
		l.setActiveModContainer(k);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(List<T> list) {
		return (T[]) list.toArray();
	}

}
