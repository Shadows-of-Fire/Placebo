package shadows.placebo.util;

import net.minecraft.item.IItemTier;
import net.minecraft.item.crafting.Ingredient;

public class ItemTier implements IItemTier {

	protected int durability, level, enchantability;
	protected float efficiency, damage;
	protected Ingredient repair;

	public ItemTier(int level, int durability, float efficiency, float damage, int enchantability, Ingredient repair) {
		this.level = level;
		this.durability = durability;
		this.efficiency = efficiency;
		this.damage = damage;
		this.enchantability = enchantability;
		this.repair = repair;
	}

	@Override
	public int getMaxUses() {
		return this.durability;
	}

	@Override
	public float getEfficiency() {
		return this.efficiency;
	}

	@Override
	public float getAttackDamage() {
		return this.damage;
	}

	@Override
	public int getHarvestLevel() {
		return this.level;
	}

	@Override
	public int getEnchantability() {
		return this.enchantability;
	}

	@Override
	public Ingredient getRepairMaterial() {
		return this.repair;
	}

}
