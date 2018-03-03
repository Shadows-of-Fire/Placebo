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

public class RegistryInformationV2 extends RegistryInformation {

	public RegistryInformationV2(String modid, CreativeTabs tab) {
		super(modid, tab);
	}

	@Override
	public RegistryList<Block> getBlockList() {
		return blocks;
	}

	@Override
	public RegistryList<Item> getItemList() {
		return items;
	}

	@Override
	public RegistryList<Potion> getPotionList() {
		return potions;
	}

	@Override
	public RegistryList<Biome> getBiomeList() {
		return biomes;
	}

	@Override
	public RegistryList<SoundEvent> getSoundList() {
		return sounds;
	}

	@Override
	public RegistryList<PotionType> getPotionTypeList() {
		return potionTypes;
	}

	@Override
	public RegistryList<Enchantment> getEnchantmentList() {
		return enchants;
	}

	@Override
	public RegistryList<VillagerProfession> getProfessionList() {
		return professions;
	}

	@Override
	public RegistryList<EntityEntry> getEntityEntryList() {
		return entities;
	}

	@Override
	public RegistryList<IRecipe> getRecipeList() {
		return recipes;
	}
}
