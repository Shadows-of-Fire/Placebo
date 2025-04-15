package dev.shadowsoffire.placebo.util;

import java.util.Locale;
import java.util.function.IntFunction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.config.Configuration;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

/**
 * A utility class for handling offsets and anchor points for GUI elements.
 * This class provides methods to calculate the position of an element based on its anchor point and offset.
 * 
 * @param anchor The anchor point of the element.
 * @param x      The X offset of the element.
 * @param y      The Y offset of the element.
 */
public record Offset(AnchorPoint anchor, int x, int y) {

    public static final Codec<Offset> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            AnchorPoint.CODEC.fieldOf("anchor").forGetter(Offset::anchor),
            Codec.INT.fieldOf("x").forGetter(Offset::x),
            Codec.INT.fieldOf("y").forGetter(Offset::y))
        .apply(inst, Offset::new));

    /**
     * Returns the X position of the element based on its anchor point and offset.
     * 
     * @param window  The size of the window.
     * @param element The size of the element.
     * @return The X position of the element.
     */
    public int getX(Box window, Box element) {
        return this.anchor.getX(window.width) - this.anchor.getX(element.width) + this.x;
    }

    /**
     * Returns the Y position of the element based on its anchor point and offset.
     * 
     * @param window  The size of the window.
     * @param element The size of the element.
     * @return The Y position of the element.
     */
    public int getY(Box window, Box element) {
        return this.anchor.getY(window.height) - this.anchor.getY(element.height) + this.y;
    }

    /**
     * Applies the X and Y coordinates for the element to the given {@link PoseStack}.
     * 
     * @param pose    The PoseStack to apply the translation to.
     * @param window  The size of the window.
     * @param element The size of the element.
     */
    public void apply(PoseStack pose, Box window, Box element) {
        pose.translate(this.getX(window, element), this.getY(window, element), 0);
    }

    /**
     * Loads an Offset from the given configuration.
     * 
     * @param key   The configuration key for the offset.
     * @param group The configuration group for the offset.
     * @param def   The default anchor point for the offset.
     * @param cfg   The configuration to load the offset from.
     */
    public static Offset load(String key, String group, AnchorPoint def, Configuration cfg) {
        AnchorPoint anchor = AnchorPoint.parse(cfg.getString(key + " Anchor Point", group, def.toString().toLowerCase(Locale.ROOT), "The anchor point for this element."));
        int x = cfg.getInt(key + " X Offset", group, 0, -1000, 1000, "The X offset for this element.");
        int y = cfg.getInt(key + " Y Offset", group, 0, -1000, 1000, "The Y Offset for this element.");
        return new Offset(anchor, x, y);
    }

    public static record Box(int width, int height) {}

    public static enum AnchorPoint implements StringRepresentable {
        TOP_LEFT("top_left", width -> 0, height -> 0),
        TOP_CENTER("top_center", width -> width / 2, height -> 0),
        TOP_RIGHT("top_right", width -> width, height -> 0),
        MIDDLE_LEFT("middle_left", width -> 0, height -> height / 2),
        MIDDLE_CENTER("middle_center", width -> width / 2, height -> height / 2),
        MIDDLE_RIGHT("middle_right", width -> width, height -> height / 2),
        BOTTOM_LEFT("bottom_left", width -> 0, height -> height),
        BOTTOM_CENTER("bottom_center", width -> width / 2, height -> height),
        BOTTOM_RIGHT("bottom_right", width -> width, height -> height);

        public static final IntFunction<AnchorPoint> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<AnchorPoint> CODEC = StringRepresentable.fromValues(AnchorPoint::values);
        public static final StreamCodec<ByteBuf, AnchorPoint> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

        private final String name;
        private final Int2IntFunction xPos;
        private final Int2IntFunction yPos;

        private AnchorPoint(String name, Int2IntFunction xPos, Int2IntFunction yPos) {
            this.name = name;
            this.xPos = xPos;
            this.yPos = yPos;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int getX(int width) {
            return this.xPos.apply(width);
        }

        public int getY(int height) {
            return this.yPos.apply(height);
        }

        public static AnchorPoint parse(String s) {
            try {
                return AnchorPoint.valueOf(s.toUpperCase(Locale.ROOT));
            }
            catch (Exception ex) {
                Placebo.LOGGER.error("Failed to parse invalid Anchor Point {}", s);
                ex.printStackTrace();
                return AnchorPoint.TOP_LEFT;
            }
        }
    }
}
