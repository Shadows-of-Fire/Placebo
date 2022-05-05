package shadows.placebo.container;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import shadows.placebo.mixin.AbstractContainerMenuInvoker;

/**
 * The QuickMoveHandler is a registration helper for setting up {@link AbstractContainerMenu#quickMoveStack(Player, int)}
 */
public class QuickMoveHandler {

	protected List<QuickMoveRule> rules = new ArrayList<>();

	public ItemStack quickMoveStack(IExposedContainer container, Player player, int index) {
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot slot = container.getMenuSlot(index);
		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			slotStackCopy = slotStack.copy();
			for (QuickMoveRule rule : this.rules) {
				if (rule.req.test(slotStack, index)) {
					if (!container.moveMenuItemStackTo(slotStack, rule.startIdx, rule.endIdx, rule.reversed)) {
						slot.setChanged();
						return ItemStack.EMPTY;
					}
				}
			}
			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return slotStackCopy;
	}

	public void registerRule(BiPredicate<ItemStack, Integer> req, int startIdx, int endIdx, boolean reversed) {
		this.rules.add(new QuickMoveRule(req, startIdx, endIdx, reversed));
	}

	public void registerRule(BiPredicate<ItemStack, Integer> req, int startIdx, int endIdx) {
		this.registerRule(req, startIdx, endIdx, false);
	}

	protected record QuickMoveRule(BiPredicate<ItemStack, Integer> req, int startIdx, int endIdx, boolean reversed) {

	}

	public interface IExposedContainer {
		public default boolean moveMenuItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection) {
			return ((AbstractContainerMenuInvoker) this)._moveItemStackTo(pStack, pStartIndex, pEndIndex, pReverseDirection);
		}

		public default Slot getMenuSlot(int index) {
			return ((AbstractContainerMenu) this).getSlot(index);
		}
	}

}
