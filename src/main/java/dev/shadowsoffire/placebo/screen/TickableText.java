package dev.shadowsoffire.placebo.screen;

import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public class TickableText {

    protected int ticks = 0;
    protected final String message;
    protected final int color;
    protected final boolean newline;
    protected final float tickRate;

    public TickableText(String message, int color, boolean newline, float tickRate) {
        this.message = message;
        this.color = color;
        this.newline = newline;
        this.tickRate = tickRate;
    }

    public TickableText(String message, int color) {
        this(message, color, true, 1);
    }

    public void tick() {
        this.ticks++;
    }

    public TickableText setTicks(int ticks) {
        this.ticks = ticks;
        return this;
    }

    public void render(Font font, GuiGraphics graphics, int x, int y) {
        graphics.drawString(font, this.message.substring(0, Mth.ceil(Math.min(this.ticks * this.tickRate, this.message.length()))), x, y, this.color);
    }

    public int getMaxUsefulTicks() {
        return Mth.floor(this.message.length() / this.tickRate);
    }

    public void reset() {
        this.ticks = 0;
    }

    public boolean isDone() {
        return this.ticks * this.tickRate >= this.message.length();
    }

    public boolean causesNewLine() {
        return this.newline;
    }

    public int getWidth(Font font) {
        return font.width(this.message);
    }

    public static void tickList(List<TickableText> texts) {
        for (int i = 0; i < texts.size(); i++) {
            TickableText txt = texts.get(i);
            if (!txt.isDone()) {
                txt.tick();
                break;
            }
        }
    }
}
