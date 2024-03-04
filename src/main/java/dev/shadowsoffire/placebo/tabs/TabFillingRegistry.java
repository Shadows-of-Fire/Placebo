package dev.shadowsoffire.placebo.tabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

/**
 * Class for managing the new method of filling creative tabs,
 * without having to bulk all the logic into one master method.
 */
public class TabFillingRegistry {

    private static final Map<ResourceKey<CreativeModeTab>, List<ITabFiller>> FILLERS = new IdentityHashMap<>();

    /**
     * Registers an {@link ITabFiller} for all passed creative tabs.
     *
     * @param filler The tab filler.
     * @param tabs   A list of creative tabs to register the filler for.
     */
    @SafeVarargs
    public static void register(ITabFiller filler, ResourceKey<CreativeModeTab>... tabs) {
        for (var tab : tabs) registerInternal(tab, filler);
    }

    /**
     * Registers multiple {@link ITabFiller}s to a single creative tab.
     *
     * @param tab     The creative tab.
     * @param fillers A list of tab fillers to register to the creative tab.
     */
    public static void register(ResourceKey<CreativeModeTab> tab, ITabFiller... fillers) {
        for (var filler : fillers) registerInternal(tab, filler);
    }

    /**
     * Registers an {@link ItemLike} as an {@linkplain ITabFiller#simple(ItemLike) simple tab filler}.<br>
     * This will cause the default instance of this item to be added to all passed tabs.
     *
     * @param item An item-like object.
     * @param tabs A list of creative tabs to register the item to.
     */
    @SafeVarargs
    public static void registerSimple(ItemLike item, ResourceKey<CreativeModeTab>... tabs) {
        for (var tab : tabs) registerInternal(tab, ITabFiller.simple(item));
    }

    /**
     * Registers multiple {@link ItemLike}s as a {@linkplain ITabFiller#simple(ItemLike) simple tab fillers}.<br>
     * This will cause the default instance of these items to be added to the passed tab.
     *
     * @param tab   The creative tab.
     * @param items A list of items to register to the creative tab.
     */
    public static void registerSimple(ResourceKey<CreativeModeTab> tab, ItemLike... items) {
        for (var item : items) registerInternal(tab, ITabFiller.simple(item));
    }

    /**
     * Registers multiple item-like suppliers to a single tab as {@linkplain ITabFiller#delegating(Supplier) a delegating tab filler}.<br>
     * A delegating tab filler will invoke {@link ITabFiller#fillItemCategory} if the supplied object is a tab filler, but otherwise
     * behave similar to {@link #registerSimple}.
     *
     * @param tab   The creative tab.
     * @param items A list of suppliers to register to the creative tab.
     */
    @SafeVarargs
    public static void register(ResourceKey<CreativeModeTab> tab, Supplier<? extends ItemLike>... items) {
        for (var item : items) registerInternal(tab, ITabFiller.delegating(item));
    }

    @ApiStatus.Internal
    public static void fillTabs(BuildCreativeModeTabContentsEvent e) {
        FILLERS.getOrDefault(e.getTabKey(), Collections.emptyList()).forEach(f -> f.fillItemCategory(e.getTab(), e));
    }

    private static void registerInternal(ResourceKey<CreativeModeTab> tab, ITabFiller filler) {
        FILLERS.computeIfAbsent(tab, k -> new ArrayList<>()).add(filler);
    }

}
