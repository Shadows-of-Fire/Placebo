package shadows.placebo.events;

import com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is fired whenever the enchantment level of a particular item is requested for gameplay purposes.<br>
 * It is called from {@link IForgeItemStack#getEnchantmentLevel(Enchantment)}.
 * <p>
 * It is not fired for interactions with NBT, or interactions that directly iterate the enchantment map (like tooltips).<br>
 * This can be used to silently increase the levels of certain enchantments.
 * <p>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.<br>
 * This event is not cancellable.<br>
 * This event does not have a result.
 */
public class GetEnchantmentLevelEvent extends Event {

	protected final ItemStack stack;
	protected final Enchantment ench;
	protected int level;

	public GetEnchantmentLevelEvent(ItemStack stack, Enchantment ench, int originalLevel) {
		this.stack = stack;
		this.ench = ench;
		this.level = originalLevel;
	}

	/**
	 * Returns the item stack that is being queried.
	 */
	public ItemStack getStack() {
		return this.stack;
	}

	/**
	 * Returns the enchantment that the level is being requested for.
	 */
	public Enchantment getEnch() {
		return this.ench;
	}

	/**
	 * Returns the current level, which may have been modified by other handlers.
	 */
	public int getLevel() {
		return this.level;
	}

	/**
	 * Sets the enchantment level.<p>
	 * This method does not do sanity-checking, it is on the implementer to ensure that the passed value is "safe" for the specific enchantment.
	 * @throws IllegalArgumentException if newLevel is negative.
	 */
	public void setLevel(int newLevel) {
		Preconditions.checkArgument(newLevel >= 0);
		this.level = newLevel;
	}

}
