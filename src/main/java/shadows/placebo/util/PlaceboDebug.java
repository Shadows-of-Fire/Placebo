package shadows.placebo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import shadows.placebo.Placebo;

public class PlaceboDebug {

	public static void debug() {

		List<IRecipe> lest = new ArrayList<>();
		for (IRecipe r : ForgeRegistries.RECIPES) {
			if (r.getClass() == ShapelessRecipes.class) {
				FastShapelessRecipe res = new FastShapelessRecipe(r.getGroup(), r.getRecipeOutput(), r.getIngredients());
				res.setRegistryName(r.getRegistryName());
				lest.add(res);
			}
		}
		for (IRecipe r : lest)
			ForgeRegistries.RECIPES.register(r);
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

}
