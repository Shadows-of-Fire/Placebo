package shadows.placebo;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import shadows.placebo.interfaces.IPostInitUpdate;
import shadows.placebo.loot.PlaceboLootSystem;
import shadows.placebo.util.PlaceboDebug;
import shadows.placebo.util.RecipeHelper;

@Mod(modid = Placebo.MODID, name = Placebo.MODNAME, version = Placebo.VERSION, acceptableRemoteVersions = "*")
public class Placebo {

	public static final String MODID = "placebo";
	public static final String MODNAME = "Placebo";
	public static final String VERSION = "1.6.0";

	public static final List<IPostInitUpdate> UPDATES = new ArrayList<>();

	@SidedProxy(serverSide = "shadows.placebo.Proxy", clientSide = "shadows.placebo.ClientProxy")
	public static Proxy PROXY;

	public static final Logger LOG = LogManager.getLogger(MODID);

	public static Configuration config;

	static boolean dumpHandlers = false;
	static boolean fastRecipes = false;

	private final boolean debug = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		config = new Configuration(e.getSuggestedConfigurationFile());
		config.load();
		dumpHandlers = config.getBoolean("Dump event handlers", "general", false, "If placebo will dump all event handlers to the log in post init.");
		fastRecipes = config.getBoolean("Fast Shapeless Recipes", "general", true, "If placebo will replace all ShapelessRecipes and ShapelessOreRecipes with FastShapelessRecipes.");
		if (config.hasChanged()) config.save();
		MinecraftForge.EVENT_BUS.register(new PlaceboLootSystem());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		for (IPostInitUpdate i : UPDATES)
			i.postInit(e);
		if (debug) PlaceboDebug.debug();
		if (dumpHandlers) PlaceboDebug.dumpEventHandlers();
		if (fastRecipes) PlaceboDebug.enableFastShapeless();
		RecipeHelper.CachedOreIngredient.ing = null;
	}
}
