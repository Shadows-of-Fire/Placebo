package shadows.placebo.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

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

}
