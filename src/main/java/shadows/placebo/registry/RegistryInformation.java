package shadows.placebo.registry;

import java.util.List;

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

/**
 * See {@link RegistryInformationV2}
 */
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

	public List<Block> getBlockList() {
		return blocks;
	}

	public List<Item> getItemList() {
		return items;
	}

	public List<Potion> getPotionList() {
		return potions;
	}

	public List<Biome> getBiomeList() {
		return biomes;
	}

	public List<SoundEvent> getSoundList() {
		return sounds;
	}

	public List<PotionType> getPotionTypeList() {
		return potionTypes;
	}

	public List<Enchantment> getEnchantmentList() {
		return enchants;
	}

	public List<VillagerProfession> getProfessionList() {
		return professions;
	}

	public List<EntityEntry> getEntityEntryList() {
		return entities;
	}

	public List<IRecipe> getRecipeList() {
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
