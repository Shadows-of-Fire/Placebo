package shadows.placebo.events;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.EntityEvent;

/**
 * The ShieldBlockEvent is fired when an entity successfully blocks with a shield.<br>
 * Due to the difficulty associated with doing so, this event is not cancellable.<br>
 * It also does not allow modifications to the amount blocked or the damage the shield takes.<br>
 * It is fired from {@link LivingEntity#attackEntityFrom} shortly
 * after {@link LivingEntity#blockUsingShield} is called, but outside the if statement.<br>
 */
public class ShieldBlockEvent extends EntityEvent {

	protected final DamageSource source;
	protected final float blocked;
	protected float curBlocked;

	/**
	 * The ShieldBlockEvent is fired when an entity successfully blocks with a shield.<br>
	 * Note that by this point the shield may have been broken, and the entity may not have
	 * an active item stack.<br>
	 * @param blocker The entity blocking with the shield.
	 * @param source
	 * @param amount
	 */
	public ShieldBlockEvent(LivingEntity blocker, DamageSource source, float blocked) {
		super(blocker);
		this.source = source;
		this.blocked = blocked;
		this.curBlocked = blocked;
	}

	/**
	 * @return The damage source.
	 */
	public DamageSource getSource() {
		return source;
	}

	/**
	 * @return The original amount of damage blocked, which is the same as the original
	 * incoming attack value.
	 */
	public float getOriginalBlocked() {
		return blocked;
	}

	/**
	 * @return The current amount of damage blocked, as a result of this event.
	 */
	public float getBlocked() {
		return curBlocked;
	}

	/**
	 * Set how much damage is blocked by this action.<br>
	 * Note that initially the blocked amount is the entire attack.  Blocking more than the attack
	 * will have no effect.<br>
	 * However, setting this value to a negative number will increase the damage taken.
	 */
	public void setBlocked(float blocked) {
		this.curBlocked = Math.min(this.blocked, blocked);
	}

	public LivingEntity getEntity() {
		return (LivingEntity) super.getEntity();
	}

}
