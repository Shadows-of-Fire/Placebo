package shadows.placebo.registry;

import java.util.ArrayList;
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

public class RegistryInformation {

	protected final List<Block> BLOCKS = new ArrayList<>();
	protected final List<Item> ITEMS = new ArrayList<>();
	protected final List<Potion> POTIONS = new ArrayList<>();
	protected final List<Biome> BIOMES = new ArrayList<>();
	protected final List<SoundEvent> SOUND_EVENTS = new ArrayList<>();
	protected final List<PotionType> POTION_TYPES = new ArrayList<>();
	protected final List<Enchantment> ENCHANTMENTS = new ArrayList<>();
	protected final List<VillagerProfession> VILLAGER_PROFESSIONS = new ArrayList<>();
	protected final List<EntityEntry> ENTITIES = new ArrayList<>();
	protected final List<IRecipe> RECIPES = new ArrayList<>();
	protected final String MODID;
	protected final CreativeTabs DEFAULT_TAB;

	public RegistryInformation(String modid, CreativeTabs tab) {
		MODID = modid;
		DEFAULT_TAB = tab;
	}

	public List<Block> getBlockList() {
		return BLOCKS;
	}

	public List<Item> getItemList() {
		return ITEMS;
	}

	public List<Potion> getPotionList() {
		return POTIONS;
	}

	public List<Biome> getBiomeList() {
		return BIOMES;
	}

	public List<SoundEvent> getSoundList() {
		return SOUND_EVENTS;
	}

	public List<PotionType> getPotionTypeList() {
		return POTION_TYPES;
	}

	public List<Enchantment> getEnchantmentList() {
		return ENCHANTMENTS;
	}

	public List<VillagerProfession> getProfessionList() {
		return VILLAGER_PROFESSIONS;
	}

	public List<EntityEntry> getEntityEntryList() {
		return ENTITIES;
	}

	public List<IRecipe> getRecipeList() {
		return RECIPES;
	}

	public String getID() {
		return MODID;
	}

	public CreativeTabs getDefaultTab() {
		return DEFAULT_TAB;
	}

	public void purge() {
		BLOCKS.clear();
		ITEMS.clear();
		POTIONS.clear();
		BIOMES.clear();
		SOUND_EVENTS.clear();
		POTION_TYPES.clear();
		ENCHANTMENTS.clear();
		VILLAGER_PROFESSIONS.clear();
		ENTITIES.clear();
		RECIPES.clear();
	}

}
