package shadows.placebo.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.fml.LogicalSide;

/**
 * 
 */
@Cancelable
public class ItemUseEvent extends PlayerEvent {

	protected final Hand hand;
	protected final BlockPos pos;
	protected final Direction face;
	protected final Vector3d hitVec;
	protected ActionResultType cancellationResult = null;

	public ItemUseEvent(ItemUseContext ctx) {
		this(ctx.getPlayer(), ctx.getHand(), ctx.getPos(), ctx.getFace(), ctx.getHitVec());
	}

	public ItemUseEvent(PlayerEntity player, Hand hand, BlockPos pos, Direction face, Vector3d hitVec) {
		super(player);
		this.hand = hand;
		this.pos = pos;
		this.face = face;
		this.hitVec = hitVec;
	}

	/**
	 * @return The ray trace result targeting the block.
	 */
	public Vector3d getHitVec() {
		return hitVec;
	}

	/**
	 * @return The hand involved in this interaction. Will never be null.
	 */
	@Nonnull
	public Hand getHand() {
		return hand;
	}

	/**
	 * @return The itemstack involved in this interaction, {@code ItemStack.EMPTY} if the hand was empty.
	 */
	@Nonnull
	public ItemStack getItemStack() {
		return getPlayer().getHeldItem(hand);
	}

	/**
	 * If the interaction was on an entity, will be a BlockPos centered on the entity.
	 * If the interaction was on a block, will be the position of that block.
	 * Otherwise, will be a BlockPos centered on the player.
	 * Will never be null.
	 * @return The position involved in this interaction.
	 */
	@Nonnull
	public BlockPos getPos() {
		return pos;
	}

	/**
	 * @return The face involved in this interaction. For all non-block interactions, this will return null.
	 */
	@Nullable
	public Direction getFace() {
		return face;
	}

	/**
	 * @return Convenience method to get the world of this interaction.
	 */
	public World getWorld() {
		return getPlayer().getEntityWorld();
	}

	/**
	 * @return The effective, i.e. logical, side of this interaction. This will be {@link LogicalSide#CLIENT} on the client thread, and {@link LogicalSide#SERVER} on the server thread.
	 */
	public LogicalSide getSide() {
		return getWorld().isRemote ? LogicalSide.CLIENT : LogicalSide.SERVER;
	}

	/**
	 * @return The EnumActionResult that will be returned to vanilla if the event is cancelled, instead of calling the relevant
	 * method of the event. By default, this is {@link EnumActionResult#PASS}, meaning cancelled events will cause
	 * the client to keep trying more interactions until something works.
	 */
	public ActionResultType getCancellationResult() {
		return cancellationResult;
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
