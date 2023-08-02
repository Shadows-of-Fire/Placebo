package dev.shadowsoffire.placebo.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * A locked slot is one which may not be interacted with.
 */
public class LockedSlot extends Slot {

    public LockedSlot(Inventory inv, int index, int x, int y) {
        super(inv, index, x, y);
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
