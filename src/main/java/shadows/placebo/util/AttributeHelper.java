package shadows.placebo.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import shadows.placebo.Placebo;

/**
 * Util class to modify IAttributes of entities.
 * @author Shadows
 *
 */
public class AttributeHelper {

	/*
	 * An explanation on operation: ModifiableAttributeInstance#computeValue
	 * There are three valid operations: 0, 1, and 2.  They are executed in order.
	 * Operation 0 adds the given modifier to the base value of the attribute.
	 * Operation 1 adds (modifier * new base value) to the final value.
	 * Operation 2 multiplies the final value by (1.0 + modifier).
	 * The IAttribute has the ability to clamp the final modified value.
	 * 
	 * For example, if we had an attribute with a base of 1.0, applying an AttributeModifier(name, 1, 0) would result in a value of 2.0 (1 + 1).
	 * Additionally applying an AttributeModifier(name, 1.5, 1) would result in a value of 5.0 (2.0 + 1.5 * 2.0).
	 * Further applying an AttributeModifier(name, 0.75, 2) would result in 8.75 (5.0 * 1.75).
	 */

	/**
	 * Modifies the given attribute, always setting the modifier to saved.
	 * @param modifier The value to modify
	 * @param operation See above.
	 */
	public static void modify(LivingEntity entity, Attribute attribute, String name, double modifier, Operation operation) {
		IAttributeInstance inst = entity.getAttribute(attribute);
		if (inst != null) inst.applyModifier(new AttributeModifier(Placebo.MODID + ":" + name, modifier, operation).setSaved(true));
	}

	/**
	 * Adds the given modifier to the base value of the attribute.
	 */
	public static void addToBase(LivingEntity entity, Attribute attribute, String name, double modifier) {
		modify(entity, attribute, name, modifier, Operation.ADDITION);
	}

	/**
	 * Adds (modifier * new base value) to the final value of the attribute.
	 * New base value is the base value plus all additions (operation 0 AttributeModifiers).
	 */
	public static void addXTimesNewBase(LivingEntity entity, Attribute attribute, String name, double modifier) {
		modify(entity, attribute, name, modifier, Operation.MULTIPLY_BASE);
	}

	/**
	 * Multiplies the final value of this attribute by 1.0 + modifier.
	 * Final value is the value after computing all operation 0 and 1 AttributeModifiers.
	 */
	public static void multiplyFinal(LivingEntity entity, Attribute attribute, String name, double modifier) {
		modify(entity, attribute, name, modifier, Operation.MULTIPLY_TOTAL);
	}

	/**
	 * Forces the base value to equal the given value, overriding previous modifiers.
	 */
	public static void setBaseValue(LivingEntity entity, Attribute attribute, String name, double value) {
		IAttributeInstance inst = entity.getAttribute(attribute);
		// TODO MCP-name: func_225504_a_ -> getModifiers
		inst.func_225504_a_(Operation.ADDITION).clear();
		modify(entity, attribute, name, value - inst.getBaseValue(), Operation.ADDITION);
	}

	/**
	 * Forces the base value to be (at minimum) the given value, overriding previous modifiers.
	 */
	public static void min(LivingEntity entity, Attribute attribute, String name, double value) {
		if (value < entity.getAttribute(attribute).getBaseValue()) {
			setBaseValue(entity, attribute, name, value);
		}
	}

	/**
	 * Forces the base value to be (at maximum) the given value, overriding previous modifiers.
	 */
	public static void max(LivingEntity entity, Attribute attribute, String name, double value) {
		if (value > entity.getAttribute(attribute).getBaseValue()) {
			setBaseValue(entity, attribute, name, value);
		}
	}
}