package shadows.placebo;

import java.io.File;
import java.util.HashMap;

import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint.DisplayTest;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;
import shadows.placebo.color.GradientColor;
import shadows.placebo.commands.PlaceboCommand;
import shadows.placebo.compat.TOPCompat;
import shadows.placebo.config.Configuration;
import shadows.placebo.network.MessageHelper;
import shadows.placebo.packets.ButtonClickMessage;
import shadows.placebo.packets.PatreonDisableMessage;
import shadows.placebo.packets.ReloadListenerPacket;
import shadows.placebo.util.PlaceboUtil;
import shadows.placebo.util.RegistryEvent;
import shadows.placebo.util.RegistryEvent.Register;

@Mod(Placebo.MODID)
public class Placebo {

	public static final String MODID = "placebo";
	public static final Logger LOGGER = LogManager.getLogger(MODID);
	static final File configDir = new File(FMLPaths.CONFIGDIR.get().toFile(), MODID);

	public static boolean firstPersonPatreonEffects = true;

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
		setupConfig();
		e.enqueueWork(() -> {
			PlaceboUtil.registerCustomColor(GradientColor.RAINBOW);
		});
	}

	@SubscribeEvent
	public void postRegistryEvents(RegisterEvent e) {
		checkAndPost(e, Block.class, ForgeRegistries.BLOCKS);
		checkAndPost(e, Fluid.class, ForgeRegistries.FLUIDS);
		checkAndPost(e, Item.class, ForgeRegistries.ITEMS);
		checkAndPost(e, MobEffect.class, ForgeRegistries.MOB_EFFECTS);
		checkAndPost(e, SoundEvent.class, ForgeRegistries.SOUND_EVENTS);
		checkAndPost(e, Potion.class, ForgeRegistries.POTIONS);
		checkAndPost(e, Enchantment.class, ForgeRegistries.ENCHANTMENTS);
		checkAndPost(e, EntityType.class, ForgeRegistries.ENTITY_TYPES);
		checkAndPost(e, BlockEntityType.class, ForgeRegistries.BLOCK_ENTITY_TYPES);
		checkAndPost(e, ParticleType.class, ForgeRegistries.PARTICLE_TYPES);
		checkAndPost(e, MenuType.class, ForgeRegistries.MENU_TYPES);
		checkAndPost(e, PaintingVariant.class, ForgeRegistries.PAINTING_VARIANTS);
		checkAndPost(e, RecipeType.class, ForgeRegistries.RECIPE_TYPES);
		checkAndPost(e, RecipeSerializer.class, ForgeRegistries.RECIPE_SERIALIZERS);
		checkAndPost(e, Attribute.class, ForgeRegistries.ATTRIBUTES);
		checkAndPost(e, StatType.class, ForgeRegistries.STAT_TYPES);
		checkAndPost(e, Feature.class, ForgeRegistries.FEATURES);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" }) // Stupid generics...
	private <T> void checkAndPost(RegisterEvent e, Class<T> clazz, IForgeRegistry<? extends T> reg) {
		if (e.getForgeRegistry() == reg) {
			var ctr = ModLoadingContext.get().getActiveContainer();
			ModLoader.get().postEventWithWrapInModOrder(new RegistryEvent.Register<>(clazz, (IForgeRegistry) reg), (mc, ev) -> ModLoadingContext.get().setActiveContainer(mc), (mc, ev) -> ModLoadingContext.get().setActiveContainer(null));
			ModLoadingContext.get().setActiveContainer(ctr);
		}
	}

	@SubscribeEvent
	public void registerElse(Register<RecipeType<?>> e) {
		PlaceboUtil.registerTypes();
	}

	public void registerCommands(RegisterCommandsEvent e) {
		PlaceboCommand.register(e.getDispatcher());
	}

	public void setupConfig() {
		Configuration config = new Configuration(new File(configDir, "placebo.cfg"));
		config.setTitle("Placebo Configuration");
		firstPersonPatreonEffects = config.getBoolean("Enable First Person Patreon Trail Effect", "patreon", true, "Render your Patreon trail particle effects in first person mode");
		config.save();
	}

}
