package shadows.placebo;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import shadows.placebo.color.GradientColor;
import shadows.placebo.commands.PlaceboCommand;
import shadows.placebo.compat.TOPCompat;
import shadows.placebo.network.MessageHelper;
import shadows.placebo.packets.ButtonClickMessage;
import shadows.placebo.packets.PatreonDisableMessage;
import shadows.placebo.packets.ReloadListenerPacket;
import shadows.placebo.util.PlaceboUtil;

@Mod(Placebo.MODID)
public class Placebo {

	public static final String MODID = "placebo";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	//Formatter::off
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(MODID, MODID))
            .clientAcceptedVersions(s->true)
            .serverAcceptedVersions(s->true)
            .networkProtocolVersion(() -> "1.0.0")
            .simpleChannel();
    //Formatter::on

	public Placebo() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.register(this);
		String version = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString();
		ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> version, (remoteVer, isNetwork) -> remoteVer == null || version.equals(remoteVer)));
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		if (ModList.get().isLoaded("theoneprobe")) TOPCompat.register();
		TextColor.NAMED_COLORS = new HashMap<>(TextColor.NAMED_COLORS);
	}

	@SubscribeEvent
	public void setup(FMLCommonSetupEvent e) {
		MessageHelper.registerMessage(CHANNEL, 0, new ButtonClickMessage());
		MessageHelper.registerMessage(CHANNEL, 1, new PatreonDisableMessage(0));
		MessageHelper.registerMessage(CHANNEL, 2, new ReloadListenerPacket.Start(""));
		MessageHelper.registerMessage(CHANNEL, 3, new ReloadListenerPacket.Content<>("", null, null));
		MessageHelper.registerMessage(CHANNEL, 4, new ReloadListenerPacket.End(""));
		e.enqueueWork(() -> {
			PlaceboUtil.registerCustomColor(GradientColor.RAINBOW);
		});
	}

	@SubscribeEvent
	public void registerElse(RegisterEvent e) {
		if (e.getForgeRegistry() == (Object) ForgeRegistries.RECIPE_TYPES) PlaceboUtil.registerTypes();
	}

	public void registerCommands(RegisterCommandsEvent e) {
		PlaceboCommand.register(e.getDispatcher());
	}

}
