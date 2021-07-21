package shadows.placebo.events;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * This event is fired when an item would be used via {@link Item#onItemUse}<br>
 * It allows for the usage to be changed or cancelled.
 */
@Cancelable
public class ItemUseEvent extends PlayerEvent {

	protected final ItemUseContext ctx;
	protected ActionResultType cancellationResult = null;

	public ItemUseEvent(ItemUseContext ctx) {
		super(ctx.getPlayer());
		this.ctx = ctx;
	}

	public ItemUseEvent(PlayerEntity player, Hand hand, BlockRayTraceResult res) {
		this(new ItemUseContext(player, hand, res));
	}

	public BlockPos getPos() {
		return this.ctx.getClickedPos();
	}

	public Direction getFace() {
		return this.ctx.getClickedFace();
	}

	public Vector3d getHitVec() {
		return this.ctx.getClickLocation();
	}

	public boolean isInside() {
		return this.ctx.isInside();
	}

	public ItemUseContext getContext() {
		return this.ctx;
	}

	/**
	 * @return The hand involved in this interaction. Will never be null.
	 */
	@Nonnull
	public Hand getHand() {
		return this.ctx.getHand();
	}

	/**
	 * @return The itemstack involved in this interaction, {@code ItemStack.EMPTY} if the hand was empty.
	 */
	@Nonnull
	public ItemStack getItemStack() {
		return this.getPlayer().getItemInHand(this.getHand());
	}

	/**
	 * @return Convenience method to get the world of this interaction.
	 */
	public World getWorld() {
		return this.getPlayer().getCommandSenderWorld();
	}

	/**
	 * @return The EnumActionResult that will be returned to vanilla if the event is cancelled, instead of calling the relevant
	 * method of the event. By default, this is {@link EnumActionResult#PASS}, meaning cancelled events will cause
	 * the client to keep trying more interactions until something works.
	 */
	public ActionResultType getCancellationResult() {
		return this.cancellationResult;
	}

	/**
	 * Set the EnumActionResult that will be returned to vanilla if the event is cancelled, instead of calling the relevant
	 * method of the event.
	 * Note that this only has an effect on {@link RightClickBlock}, {@link RightClickItem}, {@link EntityInteract}, and {@link EntityInteractSpecific}.
	 */
	public void setCancellationResult(ActionResultType result) {
		this.cancellationResult = result;
	}
}
