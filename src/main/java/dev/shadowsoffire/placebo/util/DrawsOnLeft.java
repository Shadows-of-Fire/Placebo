package dev.shadowsoffire.placebo.util;

import java.util.ArrayList;
import java.util.List;

import dev.shadowsoffire.placebo.mixin.client.AbstractContainerScreenMixin;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

/**
 * Implement this on a screen class to be able to call {@link #drawOnLeft(GuiGraphics, List, int)}
 * <p>
 * Applied to all screens via {@link AbstractContainerScreenMixin}.
 */
public interface DrawsOnLeft {

    /**
     * Renders a list of text as a tooltip attached to the left edge of the currently open container screen.
     * <p>
     * This method will automatically compress the text to fit in the available space between the left edge of the screen and the left edge of the game window.
     */
    default void drawOnLeft(GuiGraphics gfx, List<? extends FormattedText> list, int y) {
        if (list.isEmpty()) return;
        int xPos = __ths().getGuiLeft() - 16 - list.stream().map(__ths().font::width).max(Integer::compare).get();
        int maxWidth = 9999;
        if (xPos < 0) {
            maxWidth = __ths().getGuiLeft() - 6;
            xPos = -8;
        }

        List<FormattedText> split = new ArrayList<>();
        int _maxWidth = maxWidth;
        list.forEach(text -> {
            Style style = text instanceof Component comp ? comp.getStyle() : Style.EMPTY;
            __ths().font.getSplitter().splitLines(text, _maxWidth, style, (splitLine, isBlank) -> split.add(splitLine));
        });

        gfx.renderComponentTooltip(__ths().font, split, xPos, y, ItemStack.EMPTY);
    }

    /**
     * Renders a list of text as a tooltip attached to the left edge of the currently open container screen.
     * <p>
     * This method will compress the text to fit in the specified maxWidth, ignoring the size of the game window.
     */
    default void drawOnLeft(GuiGraphics gfx, List<? extends FormattedText> list, int y, int maxWidth) {
        if (list.isEmpty()) {
            return;
        }

        List<FormattedText> split = new ArrayList<>();
        list.forEach(text -> {
            Style style = text instanceof Component comp ? comp.getStyle() : Style.EMPTY;
            __ths().font.getSplitter().splitLines(text, maxWidth, style, (splitLine, isBlank) -> split.add(splitLine));
        });

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
