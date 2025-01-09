package dev.shadowsoffire.placebo.screen;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.mutable.MutableFloat;
import org.joml.Matrix4f;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;

/**
 * A list of tickable text. This can be used to populate a terminal-like screen, where each character pops into the screen based on time elapsed.
 * <p>
 * This class has full support for styled text, and will automatically break on newlines encoded in the text, as well as based on a set max width.
 */
public class TickableTextList {

    protected final Font font;
    protected final List<TickableText> texts;

    protected int ticks;
    protected int maxWidth;
    protected int lineSpacing;

    /**
     * Creates a new tickable text list with the given font and specified max width.
     */
    public TickableTextList(Font font, int maxWidth) {
        this.font = font;
        this.texts = new ArrayList<>();
        this.ticks = 0;
        this.maxWidth = maxWidth;
        this.lineSpacing = font.lineHeight + 3;
    }

    /**
     * Adds a new line of text.
     * <p>
     * When rendered, this text will be automatically split such that it fits into {@link #maxWidth}.
     * 
     * @param text     The line of text to add.
     * @param tickRate The number of characters that will appear per tick. A value of 1 is one character/tick.
     */
    public void addLine(FormattedText text, float tickRate) {
        this.texts.add(new TickableText(text, Math.max(0.01F, tickRate)));
    }

    /**
     * Adds text that is appended to the last line of text, without forcing a newline.
     * If you want space between this text and the previous entry, you must add it manually.
     * <p>
     * When rendered, this text will be automatically split such that it fits into {@link #maxWidth}.
     * 
     * @implNote This method will override the speed of the line it merges into, if any.
     */
    public void continueLine(FormattedText text, float tickRate) {
        if (this.texts.isEmpty()) {
            // If we have no line to continue, add a new line and call it a day.
            this.addLine(text, tickRate);
        }
        else {
            // Otherwise merge it with the last line in the list.
            TickableText last = texts.removeLast();
            this.addLine(FormattedText.composite(last.text, text), tickRate);
        }
    }

    /**
     * Renders all visible lines of text from this list.
     * <p>
     * The parameters are the same as
     * {@link Font#drawInBatch(FormattedCharSequence, float, float, int, boolean, Matrix4f, MultiBufferSource, DisplayMode, int, int)}.
     */
    public void render(float x, float y, int color, boolean dropShadow, Matrix4f matrix, MultiBufferSource buffer, Font.DisplayMode mode, int bgColor, int packedLight) {
        int line = 0;
        MutableFloat timeLeft = new MutableFloat(this.ticks);

        for (TickableText tickable : this.texts) {
            for (FormattedCharSequence seq : this.font.split(tickable.text, this.maxWidth)) {
                seq = wrap(seq, tickable.tickRate, timeLeft);
                this.font.drawInBatch(seq, x, y + this.lineSpacing * line, color, dropShadow, matrix, buffer, mode, bgColor, packedLight);
                line++;
            }
        }
    }

    /**
     * Renders all visible lines of text from this list.
     * 
     * @param x          The x position to draw text at.
     * @param y          The y position to draw the first line at. Subsequent lines will be offset by {@link #lineSpacing}.
     * @param color      The default text color. Will be used if the text did not specify a color itself.
     * @param dropShadow If text will be rendered with a drop shadow.
     */
    public void render(GuiGraphics gfx, float x, float y, int color, boolean dropShadow) {
        this.render(x, y, color, dropShadow, gfx.pose().last().pose(), gfx.bufferSource(), DisplayMode.NORMAL, 0, 0xF000F0);
    }

    /**
     * Calls {@link #render(GuiGraphics, float, float, int, boolean)} with a color of white and no drop shadow.
     */
    public void render(GuiGraphics gfx, float x, float y) {
        this.render(gfx, x, y, 0xFFFFFFFF, false);
    }

    /**
     * Removes all lines of text from this list, and resets the tick counter to zero.
     */
    public void clear() {
        this.texts.clear();
        this.ticks = 0;
    }

    /**
     * Returns the current tick count of this list.
     */
    public int getTicks() {
        return this.ticks;
    }

    /**
     * Sets the tick count for this list. Can be used to fast-forward the display of text.
     */
    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    /**
     * Returns the current max width.
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * Sets the max width. Rendered text will be instantly updated to reflect the new max value.
     */
    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    /**
     * Ticks the entire list. Each tick will usually show an additional character, depending on the tick rate of the line.
     */
    public void tick() {
        this.ticks++;
    }

    /**
     * Wraps a char sequence in a new one that will wrap any incoming sinks in a {@link TimeLimitedCharSink}.
     * <p>
     * This custom sink class will limit the amount of outgoing text based on {@link #ticks the current tick count}.
     */
    private FormattedCharSequence wrap(FormattedCharSequence text, float tickRate, MutableFloat timeLeft) {
        return sink -> text.accept(new TimeLimitedCharSink(sink, tickRate, timeLeft));
    }

    private record TickableText(FormattedText text, float tickRate) {}

    private class TimeLimitedCharSink implements FormattedCharSink {
        private final FormattedCharSink wrapped;
        private final float tickRate;
        private final MutableFloat timeLeft;

        public TimeLimitedCharSink(FormattedCharSink wrapped, float tickRate, MutableFloat timeLeft) {
            this.wrapped = wrapped;
            this.tickRate = tickRate;
            this.timeLeft = timeLeft;
        }

        @Override
        public boolean accept(int positionInCurrentSequence, Style style, int codePoint) {
            this.timeLeft.subtract(1 / tickRate);
            if (this.timeLeft.getValue() >= 0.0F) {
                this.wrapped.accept(positionInCurrentSequence, style, codePoint);
                return true;
            }
            else {
                return false;
            }
        }

    }

}
