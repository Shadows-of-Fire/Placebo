package shadows.placebo.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.Level;
import shadows.placebo.container.ContainerUtil.PosFactory;

public class SimplerMenuProvider<M extends AbstractContainerMenu> implements MenuProvider {
	private final Component title;
	private final MenuConstructor menuConstructor;

	public SimplerMenuProvider(Level level, BlockPos pos, PosFactory<M> factory) {
		this.menuConstructor = (id, inv, player) -> factory.create(id, inv, pos);
		this.title = Component.translatable(level.getBlockState(pos).getBlock().getDescriptionId());
	}

	@Override
	public Component getDisplayName() {
		return this.title;
	}

	@Override
	public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
		return this.menuConstructor.createMenu(pContainerId, pInventory, pPlayer);
	}

}