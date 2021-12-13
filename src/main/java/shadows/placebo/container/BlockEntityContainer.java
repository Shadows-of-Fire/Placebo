package shadows.placebo.container;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import shadows.placebo.container.EasyContainerData.IDataAutoRegister;
import shadows.placebo.container.QuickMoveHandler.IExposedContainer;

public abstract class BlockEntityContainer<T extends BlockEntity> extends PlaceboContainerMenu implements IExposedContainer {

	protected final BlockPos pos;
	protected final T tile;

	@SuppressWarnings("unchecked")
	protected BlockEntityContainer(MenuType<?> type, int id, Inventory pInv, BlockPos pos) {
		super(type, id, pInv);
		this.pos = pos;
		this.tile = (T) this.level.getBlockEntity(pos);
		if (this.tile instanceof IDataAutoRegister) {
			this.addDataSlots(((IDataAutoRegister) this.tile).getData());
		}
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return this.tile != null && this.tile.getType().isValid(this.level.getBlockState(this.pos));
	}

}
