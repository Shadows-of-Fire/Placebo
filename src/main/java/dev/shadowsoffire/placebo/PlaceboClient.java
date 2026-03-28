package dev.shadowsoffire.placebo;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.placebo.events.ResourceReloadEvent;
import dev.shadowsoffire.placebo.patreon.TrailsManager;
import dev.shadowsoffire.placebo.patreon.WingsManager;
import dev.shadowsoffire.placebo.patreon.wings.Wing;
import dev.shadowsoffire.placebo.patreon.wings.WingLayer;
import dev.shadowsoffire.placebo.util.SpecialTooltipItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = Placebo.MODID)
public class PlaceboClient {

    public static long ticks = 0;
    private static int scrollIdx = 0;
    private static ItemStack currentTooltipItem = ItemStack.EMPTY;
    private static long tooltipTick = 0;

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e) {
        TrailsManager.init();
        WingsManager.init(e);
        NeoForge.EVENT_BUS.addListener(PlaceboClient::tick);
        NeoForge.EVENT_BUS.addListener(PlaceboClient::tooltip);
        NeoForge.EVENT_BUS.addListener(PlaceboClient::scroll);
        NeoForge.EVENT_BUS.addListener(PlaceboClient::scroll2);
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(TrailsManager.TOGGLE);
        e.register(WingsManager.TOGGLE);
    }

    @SubscribeEvent
    public static void clientResource(AddClientReloadListenersEvent e) {
        e.addListener(Placebo.loc("client_reload_event"), (ResourceManagerReloadListener) res -> NeoForge.EVENT_BUS.post(new ResourceReloadEvent(res, LogicalSide.CLIENT)));
    }

    @SubscribeEvent
    public static void addLayers(AddLayers e) {
        Wing.INSTANCE = new Wing(e.getEntityModels().bakeLayer(WingsManager.WING_LOC));
        for (PlayerSkin.Model s : e.getSkins()) {
            LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> skin = e.getSkin(s);
            skin.addLayer(new WingLayer(skin));
        }
    }

    public static float getColorTicks() {
        return (ticks + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false)) / 0.5F;
    }

    @Nullable
    public static PotionBrewing getBrewingRegistry() {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? null : level.potionBrewing();
    }

    public static int getTooltipScrollIndex() {
        return scrollIdx;
    }

    public static int getTooltipScrollIndex(int size) {
        return Math.floorMod(scrollIdx, size);
    }

    public static void tick(ClientTickEvent.Post e) {
        ticks++;
    }

    public static void scroll(ScreenEvent.MouseScrolled.Pre e) {
        if (currentTooltipItem.getItem() instanceof SpecialTooltipItem && tooltipTick == PlaceboClient.ticks && Screen.hasShiftDown()) {
            scrollIdx += e.getScrollDeltaY() < 0 ? 1 : -1;
            e.setCanceled(true);
        }
    }

    public static void scroll2(InputEvent.MouseScrollingEvent e) {
        if (currentTooltipItem.getItem() instanceof SpecialTooltipItem && tooltipTick == PlaceboClient.ticks && Screen.hasShiftDown()) {
            scrollIdx += e.getScrollDeltaY() < 0 ? 1 : -1;
            e.setCanceled(true);
        }
    }

    public static void tooltip(ItemTooltipEvent e) {
        currentTooltipItem = e.getItemStack();
        tooltipTick = PlaceboClient.ticks;
    }
}
