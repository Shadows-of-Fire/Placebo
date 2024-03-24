package dev.shadowsoffire.placebo.cap;

import dev.shadowsoffire.placebo.menu.FilteredSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * Extension of {@link ItemStackHandler} which provides access to the unrestricted {@link #extractItem} and {@link #insertItem} methods.
 * <p>
 * Used by {@link FilteredSlot} so that menus may define their own logic that differs from the logic used by automation.
 */
public class InternalItemHandler extends ItemStackHandler {

    public InternalItemHandler(int size) {
        super(size);
    }

    public ItemStack extractItemInternal(int slot, int amount, boolean simulate) {
        return super.extractItem(slot, amount, simulate);
    }

    public ItemStack insertItemInternal(int slot, ItemStack stack, boolean simulate) {
        return super.insertItem(slot, stack, simulate);
    }
}
