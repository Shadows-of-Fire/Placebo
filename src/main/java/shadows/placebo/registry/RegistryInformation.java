package shadows.placebo.registry;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionType;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import shadows.placebo.util.collections.RegistryList;

public class RegistryInformation {

	protected RegistryList<Block> blocks = new RegistryList<>();
	protected RegistryList<Item> items = new RegistryList<>();
	protected RegistryList<Potion> potions = new RegistryList<>();
	protected RegistryList<Biome> biomes = new RegistryList<>();
	protected RegistryList<SoundEvent> sounds = new RegistryList<>();
	protected RegistryList<PotionType> potionTypes = new RegistryList<>();
	protected RegistryList<Enchantment> enchants = new RegistryList<>();
	protected RegistryList<VillagerProfession> professions = new RegistryList<>();
	protected RegistryList<EntityEntry> entities = new RegistryList<>();
	protected RegistryList<IRecipe> recipes = new RegistryList<>();
	protected String MODID;
	protected CreativeTabs DEFAULT_TAB;

	public RegistryInformation(String modid, CreativeTabs tab) {
		MODID = modid;
		DEFAULT_TAB = tab;
	}

	public RegistryList<Block> getBlockList() {
		return blocks;
	}

	public RegistryList<Item> getItemList() {
		return items;
	}

	public RegistryList<Potion> getPotionList() {
		return potions;
	}

	public RegistryList<Biome> getBiomeList() {
		return biomes;
	}

	public RegistryList<SoundEvent> getSoundList() {
		return sounds;
	}

	public RegistryList<PotionType> getPotionTypeList() {
		return potionTypes;
	}

	public RegistryList<Enchantment> getEnchantmentList() {
		return enchants;
	}

	public RegistryList<VillagerProfession> getProfessionList() {
		return professions;
	}

	public RegistryList<EntityEntry> getEntityEntryList() {
		return entities;
	}

	public RegistryList<IRecipe> getRecipeList() {
		return recipes;
	}

	public String getID() {
		return MODID;
	}

	public CreativeTabs getDefaultTab() {
		return DEFAULT_TAB;
	}

	/**
	 * Sets all lists to null, making this registry information invalidated.
	 */
	public void purge() {
		blocks = null;
		items = null;
		potions = null;
		biomes = null;
		sounds = null;
		potionTypes = null;
		enchants = null;
		professions = null;
		entities = null;
		recipes = null;
	}

}
