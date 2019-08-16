package shadows.placebo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import shadows.placebo.loot.LootSystem;
import shadows.placebo.net.MessageButtonClick;
import shadows.placebo.recipe.RecipeHelper;
import shadows.placebo.recipe.TagIngredient;
import shadows.placebo.trading.VillagerTradingManager;
import shadows.placebo.util.NetworkUtils;

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
	}

	@SubscribeEvent
	public void setup(FMLCommonSetupEvent e) {
		CraftingHelper.register(new ResourceLocation(Placebo.MODID, "tag"), TagIngredient.SERIALIZER);
		MinecraftForge.EVENT_BUS.addListener(RecipeHelper::serverStart);
		MinecraftForge.EVENT_BUS.addListener(LootSystem::serverStart);
		MinecraftForge.EVENT_BUS.addListener(this::serverStart);
		NetworkUtils.registerMessage(CHANNEL, 0, new MessageButtonClick());
	}

	public void serverStart(FMLServerAboutToStartEvent e) {
		VillagerTradingManager.postEvents();
	}
}
