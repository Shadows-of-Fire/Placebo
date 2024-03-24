package dev.shadowsoffire.placebo.util;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * Collection of misc util stuff.
 */
@SuppressWarnings("deprecation")
public class PlaceboUtil {

    /**
     * Returns an ArrayList (non-fixed) with the provided elements.
     */
    @SafeVarargs
    public static <T> List<T> asList(T... objs) {
        ArrayList<T> list = new ArrayList<>();
        for (T t : objs)
            list.add(t);
        return list;
    }

    public static CompoundTag getStackNBT(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) stack.setTag(tag = new CompoundTag());
        return tag;
    }

    /**
     * Creates an ItemStack out of an appropriate stack-like object.<br>
     * An {@link ItemLike} returns a new itemstack with a size of 1.<br>
     * An {@link ItemStack} will return itself.
     *
     * @param thing An {@link ItemLike} or {@link ItemStack}
     * @return An ItemStack representing <code>thing</code>.
     * @throws IllegalArgumentException if <code>thing</code> is not a valid type.
     */
    public static ItemStack makeStack(Object thing) {
        if (thing instanceof ItemStack stack) return stack;
        if (thing instanceof ItemLike il) return new ItemStack(il);
        if (thing instanceof Holder h) return makeStack(h.value());
        throw new IllegalArgumentException("Attempted to create an ItemStack from something that cannot be converted: " + thing);
    }

    public static ItemStack[] toStackArray(Object... args) {
        ItemStack[] out = new ItemStack[args.length];
        for (int i = 0; i < args.length; i++)
            out[i] = makeStack(args[i]);
        return out;
    }

    /**
     * Returns a mutable version of the passed list.
     * If the list is already mutable, the list is returned.
     */
    public static <T> List<T> toMutable(List<T> list) {
        if (list instanceof ImmutableList) {
            list = new ArrayList<>(list);
        }
        return list;
    }

    /**
     * Attempt to break a block as this player.
     *
     * @param player The player breaking the block.
     * @param pos    The location of the block to break.
     * @return If a block was successfully broken.
     */
    public static boolean tryHarvestBlock(ServerPlayer player, BlockPos pos) {
        return player.gameMode.destroyBlock(pos);
    }

    /**
     * Adds a component to the lore tag of an itemstack/
     *
     * @param stack The ItemStack to append lore to.
     * @param lore  The actual lore.
     */
    public static void addLore(ItemStack stack, Component lore) {
        CompoundTag display = stack.getOrCreateTagElement("display");
        ListTag tag = display.getList("Lore", 8);
        tag.add(StringTag.valueOf(Component.Serializer.toJson(lore)));
        display.put("Lore", tag);
    }

    /**
     * Used to register a custom named color that extends TextColor.
     * Should be called during common setup from within an enqueue work call.
     * This is not required for any static color values, because they can be represented as a hex int.
     */
    public static <T extends TextColor> void registerCustomColor(T color) {
        TextColor.NAMED_COLORS.put(color.serialize(), color);
    }

}
