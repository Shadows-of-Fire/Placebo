package dev.shadowsoffire.placebo.menu;

import java.util.function.Predicate;

import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.menu.QuickMoveHandler.IExposedContainer;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class PlaceboContainerMenu extends AbstractContainerMenu implements IExposedContainer {

    protected final Level level;
    protected final QuickMoveHandler mover = new QuickMoveHandler();

    protected int playerInvStart = -1, hotbarStart = -1;

    protected PlaceboContainerMenu(MenuType<?> type, int id, Inventory pInv) {
        super(type, id);
        this.level = pInv.player.level();
    }

    /**
     * Adds the player slots at a given coordinate location.
     */
    protected void addPlayerSlots(Inventory pInv, int x, int y) {
        this.playerInvStart = this.slots.size();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                this.addSlot(new Slot(pInv, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }

        this.hotbarStart = this.slots.size();
        for (int row = 0; row < 9; row++) {
            this.addSlot(new Slot(pInv, row, x + row * 18, y + 58));
        }
    }

    /**
     * Registers default mover rules that allow for items to shuffle between the inventory and the hotbar.
     */
    protected void registerInvShuffleRules() {
        if (this.hotbarStart == -1 || this.playerInvStart == -1) {
            throw new UnsupportedOperationException("Attempted to register inv shuffle rules with no player inv slots.");
        }
        this.mover.registerRule((stack, slot) -> slot >= this.hotbarStart, this.playerInvStart, this.hotbarStart);
        this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart, this.hotbarStart, this.slots.size());
    }

    /**
     * This method is called when a click of type QUICK_MOVE (a shift click) is received.<br>
     * It will be called repeatedly until the returned item is empty or it differs from the item in the specified slot.
     *
     * @apiNote this method is free to mutate all state about the container, which means the stack in the specified slot may change.
     */
    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return this.mover.quickMoveStack(this, pPlayer, pIndex);
    }

    @Override
    public boolean moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
        return super.moveItemStackTo(pStack, pStartIndex, pEndIndex, pReverseDirection);
    }

    /**
     * Causes {@link ContainerListener#dataChanged(AbstractContainerMenu, int, int)} to be called on the client.
     * <p>
     * This method is normally only called via {@link ClientGamePacketListener#handleContainerSetData(ClientboundContainerSetDataPacket)}
     */
    @Override
    public void setData(int pId, int pData) {
        super.setData(pId, pData);
        this.updateDataSlotListeners(pId, pData);
    }

    /**
     * Adds a data update listener. These are invoked on both sides, whenever data changes.
     * 
     * @param listener
     */
    public void addDataListener(IDataUpdateListener listener) {
        this.addSlotListener(new ContainerListener(){

            @Override
            public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {}

            @Override
            public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {
                listener.dataUpdated(pDataSlotIndex, pValue);
            }

        });
    }

    /**
     * Adds a slot update listener, which is only invoked on the server, when it feels like it™
     * Probably best to avoid this...
     * 
     * @param listener
     */
    public void addSlotListener(SlotUpdateListener listener) {
        this.addSlotListener(new ContainerListener(){

            @Override
            public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {
                listener.slotUpdated(pDataSlotIndex, pStack);
            }

            @Override
            public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {}

        });
    }

    protected class UpdatingSlot extends FilteredSlot {

        public UpdatingSlot(InternalItemHandler handler, int index, int x, int y, Predicate<ItemStack> filter) {
            super(handler, index, x, y, filter);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            PlaceboContainerMenu.this.slotsChanged(null);
        }
    }

}
