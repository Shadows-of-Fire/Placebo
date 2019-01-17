package shadows.placebo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import shadows.placebo.Placebo;
import shadows.placebo.loot.PlaceboLootEntry;
import shadows.placebo.loot.PlaceboLootPool.PoolBuilder;
import shadows.placebo.loot.PlaceboLootSystem;

public class PlaceboDebug {

	public static void debug() {
		PoolBuilder build = new PoolBuilder(2, 5, 1, 4);
		build.addEntries(new PlaceboLootEntry(new ItemStack(Items.COAL), 2, 5, 1, 2));
		PlaceboLootSystem.registerLootTable(new ResourceLocation("placebo", "debug"), new LootTable(new LootPool[] { build.build() }));
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
		Placebo.LOG.info("Beginning replacement of all shapeless recipes...");
		Placebo.LOG.info("Expect log spam from FML!");
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
		Placebo.LOG.info("Successfully replaced {} recipes with fast recipes.", fastRecipes.size());
	}

}
