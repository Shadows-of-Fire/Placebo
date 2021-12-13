package shadows.placebo.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import shadows.placebo.container.QuickMoveHandler.IExposedContainer;

public abstract class PlaceboContainerMenu extends AbstractContainerMenu implements IExposedContainer {

	protected final Level level;
	protected final QuickMoveHandler mover = new QuickMoveHandler();
	protected IDataUpdateListener updateListener;

	protected PlaceboContainerMenu(MenuType<?> type, int id, Inventory pInv) {
		super(type, id);
		this.level = pInv.player.level;
	}

	/**
	 * Adds the player slots at a given coordinate location.
	 */
	protected void addPlayerSlots(Inventory pInv, int x, int y) {
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 9; column++) {
				this.addSlot(new Slot(pInv, column + row * 9 + 9, x + column * 18, y + row * 18));
			}
		}

		for (int row = 0; row < 9; row++) {
			this.addSlot(new Slot(pInv, row, x + row * 18, y + 58));
		}
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

}
