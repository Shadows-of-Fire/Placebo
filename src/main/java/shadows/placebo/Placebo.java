package shadows.placebo;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import shadows.placebo.interfaces.IPostInitUpdate;

@Mod(modid = Placebo.MODID, name = Placebo.MODNAME, version = Placebo.VERSION)
public class Placebo {

	public static final String MODID = "placebo";
	public static final String MODNAME = "Placebo";
	public static final String VERSION = "1.1.2";

	public static final List<IPostInitUpdate> UPDATES = new ArrayList<>();

	@SidedProxy(serverSide = "shadows.placebo.Proxy", clientSide = "shadows.placebo.ClientProxy")
	public static Proxy PROXY;

	@EventHandler
	public void preInit(FMLPreInitializationEvent e) {

	}

	@EventHandler
	public void init(FMLInitializationEvent e) {

	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		for (IPostInitUpdate i : UPDATES)
			i.postInit(e);
	}
}
