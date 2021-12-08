package shadows.placebo.container;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * The QuickMoveHandler is a registration helper for setting up {@link AbstractContainerMenu#quickMoveStack(Player, int)}
 */
public class QuickMoveHandler {

	protected List<QuickMoveRule> rules = new ArrayList<>();

	public <T extends AbstractContainerMenu & IExposedContainer> ItemStack quickMoveStack(T container, Player player, int index) {
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot slot = container.getSlot(index);
		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			slotStackCopy = slotStack.copy();
			for (QuickMoveRule rule : rules) {
				if (rule.req.test(slotStack, index)) {
					if (!container.moveItemStackTo(slotStack, rule.startIdx, rule.endIdx, rule.reversed)) return ItemStack.EMPTY;
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
		registerRule(req, startIdx, endIdx, false);
	}

	protected record QuickMoveRule(BiPredicate<ItemStack, Integer> req, int startIdx, int endIdx, boolean reversed) {

	}

	public interface IExposedContainer {
		public boolean moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection);
	}

}
