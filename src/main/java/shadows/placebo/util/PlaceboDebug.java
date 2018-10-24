package shadows.placebo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.init.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import shadows.placebo.Placebo;

public class PlaceboDebug {

	public static void debug() {
		RecipeHelper h = new RecipeHelper("dad", "dad", new ArrayList<>());
		h.addSimpleShapeless(Items.STICK, Items.ENDER_PEARL, 9);
		h.addSimpleShapeless(Items.STICK, Items.ENDER_EYE, 8);
		h.addSimpleShapeless(Items.STICK, Items.BOAT, 7);
		h.addSimpleShapeless(Items.STICK, Items.BONE, 6);
		h.addSimpleShapeless(Items.STICK, Items.BAKED_POTATO, 5);
		h.register(ForgeRegistries.RECIPES);
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

	public static void enableFastShapeless() {
		List<IRecipe> fastRecipes = new ArrayList<>();
		for (IRecipe r : ForgeRegistries.RECIPES) {
			if (r.getClass() == ShapelessRecipes.class || r.getClass() == ShapelessOreRecipe.class) {
				FastShapelessRecipe res = new FastShapelessRecipe(r.getGroup(), r.getRecipeOutput(), r.getIngredients());
				res.setRegistryName(r.getRegistryName());
				fastRecipes.add(res);
			}
		}
		for (IRecipe r : fastRecipes)
			ForgeRegistries.RECIPES.register(r);
	}

}
