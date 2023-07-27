package dev.shadowsoffire.placebo.cap;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

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
