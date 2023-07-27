package dev.shadowsoffire.placebo.container;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;

public interface SlotUpdateListener {

    /**
     * Called when the client receives an item update from the server.<br>
     * Specifically, called from {@link PlaceboContainerMenu#setItem(int, int, ItemStack)} when a {@link ClientboundContainerSetSlotPacket} is received.
     *
     * @param id    The ID of the slot that was updated.
     * @param stack The new stack.
     */
    void slotUpdated(int id, ItemStack stack);

}
