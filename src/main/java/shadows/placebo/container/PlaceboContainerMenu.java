package shadows.placebo.container;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import shadows.placebo.container.QuickMoveHandler.IExposedContainer;

public abstract class PlaceboContainerMenu extends AbstractContainerMenu implements IExposedContainer {

	protected final Level level;
	protected final QuickMoveHandler mover = new QuickMoveHandler();
	protected final List<Int2IntFunction> syncTransformers = new ArrayList<>();
	protected IDataUpdateListener updateListener;

	protected int playerInvStart = -1, hotbarStart = -1;

	protected PlaceboContainerMenu(MenuType<?> type, int id, Inventory pInv) {
		super(type, id);
		this.level = pInv.player.level;
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

	@Override
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return this.mover.quickMoveStack(this, pPlayer, pIndex);
	}

	@Override
	public boolean moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
		return super.moveItemStackTo(pStack, pStartIndex, pEndIndex, pReverseDirection);
	}

	public void setDataListener(IDataUpdateListener listener) {
		this.updateListener = listener;
	}

	@Override
	public void setData(int pId, int pData) {
		super.setData(pId, pData);
		if (this.updateListener != null) this.updateListener.dataUpdated(pId, pData);
	}

	@Override
	protected DataSlot addDataSlot(DataSlot slot) {
		if (slot instanceof Int2IntFunction fun) {
			this.syncTransformers.add(fun);
		} else this.syncTransformers.add(Int2IntFunction.identity());
		return super.addDataSlot(slot);
	}

	@Override
	public void synchronizeDataSlotToRemote(int slotId, int value) {
		if (!this.suppressRemoteUpdates) {
			int i = this.remoteDataSlots.getInt(slotId);
			if (i != value) {
				this.remoteDataSlots.set(slotId, value);
				if (this.synchronizer != null) {
					this.synchronizer.sendDataChange(this, slotId, this.syncTransformers.get(slotId).applyAsInt(value));
				}
			}

		}
	}

}
