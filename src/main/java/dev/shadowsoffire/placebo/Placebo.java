package dev.shadowsoffire.placebo;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.commands.PlaceboCommand;
import dev.shadowsoffire.placebo.compat.TOPCompat;
import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.packets.ButtonClickMessage;
import dev.shadowsoffire.placebo.packets.PatreonDisableMessage;
import dev.shadowsoffire.placebo.registry.RegistryEvent;
import dev.shadowsoffire.placebo.registry.RegistryEvent.Register;
import dev.shadowsoffire.placebo.reload.ReloadListenerPacket;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import dev.shadowsoffire.placebo.util.PlaceboUtil;
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

@Mod(Placebo.MODID)
@SuppressWarnings("deprecation")
public class Placebo {

    public static final String MODID = "placebo";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
        .named(new ResourceLocation(MODID, MODID))
        .clientAcceptedVersions(s -> true)
        .serverAcceptedVersions(s -> true)
        .networkProtocolVersion(() -> "1.0.0")
        .simpleChannel();

    public Placebo() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        String version = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString();
        ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> version, (remoteVer, isNetwork) -> remoteVer == null || version.equals(remoteVer)));
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        if (ModList.get().isLoaded("theoneprobe")) TOPCompat.register();
        TextColor.NAMED_COLORS = new HashMap<>(TextColor.NAMED_COLORS);
        bus.addListener(TabFillingRegistry::fillTabs);
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        MessageHelper.registerMessage(CHANNEL, 0, new ButtonClickMessage.Provider());
        MessageHelper.registerMessage(CHANNEL, 1, new PatreonDisableMessage.Provider());
        MessageHelper.registerMessage(CHANNEL, 2, new ReloadListenerPacket.Start.Provider());
        MessageHelper.registerMessage(CHANNEL, 3, new ReloadListenerPacket.Content.Provider<>());
        MessageHelper.registerMessage(CHANNEL, 4, new ReloadListenerPacket.End.Provider());
        e.enqueueWork(() -> {
            PlaceboUtil.registerCustomColor(GradientColor.RAINBOW);
        });
    }

    @SubscribeEvent
    public void postRegistryEvents(RegisterEvent e) {
        this.checkAndPost(e, Block.class, ForgeRegistries.BLOCKS);
        this.checkAndPost(e, Fluid.class, ForgeRegistries.FLUIDS);
        this.checkAndPost(e, Item.class, ForgeRegistries.ITEMS);
        this.checkAndPost(e, MobEffect.class, ForgeRegistries.MOB_EFFECTS);
        this.checkAndPost(e, SoundEvent.class, ForgeRegistries.SOUND_EVENTS);
        this.checkAndPost(e, Potion.class, ForgeRegistries.POTIONS);
        this.checkAndPost(e, Enchantment.class, ForgeRegistries.ENCHANTMENTS);
        this.checkAndPost(e, EntityType.class, ForgeRegistries.ENTITY_TYPES);
        this.checkAndPost(e, BlockEntityType.class, ForgeRegistries.BLOCK_ENTITY_TYPES);
        this.checkAndPost(e, ParticleType.class, ForgeRegistries.PARTICLE_TYPES);
        this.checkAndPost(e, MenuType.class, ForgeRegistries.MENU_TYPES);
        this.checkAndPost(e, PaintingVariant.class, ForgeRegistries.PAINTING_VARIANTS);
        this.checkAndPost(e, RecipeType.class, ForgeRegistries.RECIPE_TYPES);
        this.checkAndPost(e, RecipeSerializer.class, ForgeRegistries.RECIPE_SERIALIZERS);
        this.checkAndPost(e, Attribute.class, ForgeRegistries.ATTRIBUTES);
        this.checkAndPost(e, StatType.class, ForgeRegistries.STAT_TYPES);
        this.checkAndPost(e, Feature.class, ForgeRegistries.FEATURES);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" }) // Stupid generics...
    private <T> void checkAndPost(RegisterEvent e, Class<T> clazz, IForgeRegistry<? extends T> reg) {
        if (e.getForgeRegistry() == reg) {
            var ctr = ModLoadingContext.get().getActiveContainer();
            ModLoader.get().postEventWithWrapInModOrder(new RegistryEvent.Register<>(clazz, (IForgeRegistry) reg), (mc, ev) -> ModLoadingContext.get().setActiveContainer(mc),
                (mc, ev) -> ModLoadingContext.get().setActiveContainer(null));
            ModLoadingContext.get().setActiveContainer(ctr);
        }
    }

    @SubscribeEvent
    @SuppressWarnings("deprecation")
    public void registerElse(Register<RecipeType<?>> e) {
        PlaceboUtil.registerTypes();
    }

    public void registerCommands(RegisterCommandsEvent e) {
        PlaceboCommand.register(e.getDispatcher());
    }

}
