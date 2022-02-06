package shadows.placebo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import shadows.placebo.commands.PlaceboCommand;
import shadows.placebo.network.MessageHelper;
import shadows.placebo.packets.ButtonClickMessage;
import shadows.placebo.packets.PatreonDisableMessage;
import shadows.placebo.packets.ReloadListenerPacket;
import shadows.placebo.recipe.TagIngredient;

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
		bus.addListener(this::setup);
		String version = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString();
		ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> version, (remoteVer, isNetwork) -> remoteVer == null || version.equals(remoteVer)));
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
	}

	@SubscribeEvent
	public void setup(FMLCommonSetupEvent e) {
		CraftingHelper.register(new ResourceLocation(Placebo.MODID, "tag"), TagIngredient.SERIALIZER);
		MessageHelper.registerMessage(CHANNEL, 0, new ButtonClickMessage());
		MessageHelper.registerMessage(CHANNEL, 1, new PatreonDisableMessage(0));
		MessageHelper.registerMessage(CHANNEL, 2, new ReloadListenerPacket.Start(""));
		MessageHelper.registerMessage(CHANNEL, 3, new ReloadListenerPacket.Content<>("", null, null));
		MessageHelper.registerMessage(CHANNEL, 4, new ReloadListenerPacket.End(""));
	}

	public void registerCommands(RegisterCommandsEvent e) {
		PlaceboCommand.register(e.getDispatcher());
	}

}
