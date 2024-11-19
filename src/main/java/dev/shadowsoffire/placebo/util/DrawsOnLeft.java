package dev.shadowsoffire.placebo.util;

import java.util.ArrayList;
import java.util.List;

import dev.shadowsoffire.placebo.mixin.client.AbstractContainerScreenMixin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.item.ItemStack;

/**
 * Implement this on a screen class to be able to call {@link #drawOnLeft(GuiGraphics, List, int)}
 * <p>
 * Applied to all screens via {@link AbstractContainerScreenMixin}.
 */
public interface DrawsOnLeft {

    /**
     * Renders a list of text as a tooltip attached to the left edge of the currently open container screen.
     */
    default void drawOnLeft(GuiGraphics gfx, List<Component> list, int y) {
        if (list.isEmpty()) return;
        int xPos = __ths().getGuiLeft() - 16 - list.stream().map(__ths().font::width).max(Integer::compare).get();
        int maxWidth = 9999;
        if (xPos < 0) {
            maxWidth = __ths().getGuiLeft() - 6;
            xPos = -8;
        }

        List<FormattedText> split = new ArrayList<>();
        int lambdastupid = maxWidth;
        list.forEach(comp -> split.addAll(__ths().font.getSplitter().splitLines(comp, lambdastupid, comp.getStyle())));

        gfx.renderComponentTooltip(__ths().font, split, xPos, y, ItemStack.EMPTY);
    }

    /**
     * Renders a list of text as a tooltip attached to the left edge of the currently open container screen.
     */
    default void drawOnLeft(GuiGraphics gfx, List<Component> list, int y, int maxWidth) {
        if (list.isEmpty()) {
            return;
        }

        List<FormattedText> split = new ArrayList<>();
        list.forEach(comp -> split.addAll(__ths().font.getSplitter().splitLines(comp, maxWidth, comp.getStyle())));

        int xPos = __ths().getGuiLeft() - 16 - split.stream().map(__ths().font::width).max(Integer::compare).get();
        gfx.renderComponentTooltip(__ths().font, split, xPos, y, ItemStack.EMPTY);
    }

    default AbstractContainerScreen<?> __ths() {
        return (AbstractContainerScreen<?>) this;
    }

    public static void draw(AbstractContainerScreen<?> screen, GuiGraphics gfx, List<Component> list, int y) {
        ((DrawsOnLeft) screen).drawOnLeft(gfx, list, y);
    }

}
