package shadows.placebo.events;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;

/**
 * The AnvilFallEvent is fired when a falling anvil lands on a block.
 */
public class AnvilLandEvent extends Event {

	protected final Level level;
	protected final BlockPos pos;
	protected final BlockState newState;
	protected final BlockState oldState;
	protected final FallingBlockEntity entity;

	public AnvilLandEvent(Level level, BlockPos pos, BlockState newState, BlockState oldState, FallingBlockEntity entity) {
		this.level = level;
		this.pos = pos;
		this.newState = newState;
		this.oldState = oldState;
		this.entity = entity;
	}

	public Level getLevel() {
		return this.level;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public BlockState getNewState() {
		return this.newState;
	}

	public BlockState getOldState() {
		return this.oldState;
	}

	public FallingBlockEntity getEntity() {
		return this.entity;
	}

}
