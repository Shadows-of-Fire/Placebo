package dev.shadowsoffire.placebo;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.commands.PlaceboCommand;
import dev.shadowsoffire.placebo.json.GearSetRegistry;
import dev.shadowsoffire.placebo.network.MessageHelper;
import dev.shadowsoffire.placebo.packets.ButtonClickMessage;
import dev.shadowsoffire.placebo.packets.PatreonDisableMessage;
import dev.shadowsoffire.placebo.reload.ReloadListenerPackets;
import dev.shadowsoffire.placebo.tabs.TabFillingRegistry;
import dev.shadowsoffire.placebo.util.PlaceboUtil;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.IExtensionPoint.DisplayTest;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Placebo.MODID)
@SuppressWarnings("deprecation")
public class Placebo {

    public static final String MODID = "placebo";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public Placebo(IEventBus bus) {
        bus.register(this);
        String version = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString();
        ModLoadingContext.get().registerExtensionPoint(DisplayTest.class, () -> new DisplayTest(() -> version, (remoteVer, isNetwork) -> remoteVer == null || version.equals(remoteVer)));
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        TextColor.NAMED_COLORS = new HashMap<>(TextColor.NAMED_COLORS);
        bus.addListener(TabFillingRegistry::fillTabs);
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent e) {
        MessageHelper.registerMessage(new ButtonClickMessage.Provider());
        MessageHelper.registerMessage(new PatreonDisableMessage.Provider());
        MessageHelper.registerMessage(new ReloadListenerPackets.Start.Provider());
        MessageHelper.registerMessage(new ReloadListenerPackets.Content.Provider<>());
        MessageHelper.registerMessage(new ReloadListenerPackets.End.Provider());
        e.enqueueWork(() -> {
            PlaceboUtil.registerCustomColor(GradientColor.RAINBOW);
        });
        GearSetRegistry.INSTANCE.registerToBus();
    }

    public void registerCommands(RegisterCommandsEvent e) {
        PlaceboCommand.register(e.getDispatcher(), e.getBuildContext());
    }

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MODID, path);
    }

}
