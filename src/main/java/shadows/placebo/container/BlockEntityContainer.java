package shadows.placebo.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import shadows.placebo.container.EasyContainerData.IDataAutoRegister;
import shadows.placebo.container.QuickMoveHandler.IExposedContainer;

public abstract class BlockEntityContainer<T extends BlockEntity> extends AbstractContainerMenu implements IExposedContainer {

	protected final BlockPos pos;
	protected final Level level;
	protected final T tile;
	protected final QuickMoveHandler mover = new QuickMoveHandler();

	@SuppressWarnings("unchecked")
	protected BlockEntityContainer(int id, Inventory pInv, BlockPos pos) {
		super(null, id);
		this.pos = pos;
		this.level = pInv.player.level;
		this.tile = (T) this.level.getBlockEntity(pos);
		this.addSlots();
		if (this.tile instanceof IDataAutoRegister) {
			this.addDataSlots(((IDataAutoRegister) this.tile).getData());
		}
	}

	public abstract MenuType<?> getType();

	protected abstract void addSlots();

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
	public boolean stillValid(Player pPlayer) {
		return this.tile != null && this.tile.getType().isValid(this.level.getBlockState(this.pos));
	}

	public boolean moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
		return super.moveItemStackTo(pStack, pStartIndex, pEndIndex, pReverseDirection);
	}

}
