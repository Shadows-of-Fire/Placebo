package shadows.placebo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import net.minecraftforge.registries.IForgeRegistryEntry;
import shadows.placebo.Placebo;

public class PlaceboUtil {

	public static void sMRL(Item k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(k, meta, new ModelResourceLocation(k.getRegistryName(), variant));
	}

	public static void sMRL(Block k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(k), meta, new ModelResourceLocation(k.getRegistryName(), variant));
	}

	public static void sMRL(String statePath, Block k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(k), meta, new ModelResourceLocation(k.getRegistryName().getResourceDomain() + ":" + statePath, variant));
	}

	public static void sMRL(String statePath, Item k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(k, meta, new ModelResourceLocation(k.getRegistryName().getResourceDomain() + ":" + statePath, variant));
	}

	public static void sMRL(String domain, String statePath, Block k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(k), meta, new ModelResourceLocation(domain + ":" + statePath, variant));
	}

	public static void sMRL(String domain, String statePath, Item k, int meta, String variant) {
		ModelLoader.setCustomModelResourceLocation(k, meta, new ModelResourceLocation(domain + ":" + statePath, variant));
	}

	public static boolean isOwnedBy(IForgeRegistryEntry<?> thing, String owner) {
		return thing.getRegistryName().getResourceDomain().equals(owner);
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

	public static void dumpEventHandlers() {
		try {
			ConcurrentHashMap<Object, ArrayList<IEventListener>> map = ReflectionHelper.getPrivateValue(EventBus.class, MinecraftForge.EVENT_BUS, "listeners");
			for (Object o : map.keySet()) {
				for (IEventListener iel : map.get(o)) {
					String desc = "";
					if (iel instanceof ASMEventHandler) {
						desc += ReflectionHelper.getPrivateValue(ASMEventHandler.class, (ASMEventHandler) iel, "readable");
						Placebo.LOG.info("Found event handler: " + desc);
					} else Placebo.LOG.info("Class " + o.getClass().getName() + " has event handler, but it is not an ASMEventHandler!");
				}
			}
		} catch (UnableToAccessFieldException e) {
			Placebo.LOG.error("Failed to dump event handlers!");
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(List<T> list) {
		return (T[]) list.toArray();
	}

}
