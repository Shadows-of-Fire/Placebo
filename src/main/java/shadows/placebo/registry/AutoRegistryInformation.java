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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import shadows.placebo.util.PlaceboUtil;

public class AutoRegistryInformation extends RegistryInformation {

	public AutoRegistryInformation(String modid, CreativeTabs tab) {
		super(modid, tab);
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void blocks(Register<Block> event) {
		event.getRegistry().registerAll(PlaceboUtil.toArray(BLOCKS));
	}
	
	@SubscribeEvent
	public void items(Register<Item> event) {
		event.getRegistry().registerAll(PlaceboUtil.toArray(ITEMS));
	}

	@SubscribeEvent
	public void potions(Register<Potion> event) {
		event.getRegistry().registerAll(PlaceboUtil.toArray(POTIONS));
	}
	
	@SubscribeEvent
	public void biomes(Register<Biome> event) {
		event.getRegistry().registerAll(PlaceboUtil.toArray(BIOMES));
	}
	
	@SubscribeEvent
	public void sounds(Register<SoundEvent> event) {
		event.getRegistry().registerAll(PlaceboUtil.toArray(SOUND_EVENTS));
	}
	
	@SubscribeEvent
	public void potionTypes(Register<PotionType> event) {
		event.getRegistry().registerAll(PlaceboUtil.toArray(POTION_TYPES));
	}
	
	@SubscribeEvent
	public void enchantments(Register<Enchantment> event) {
		event.getRegistry().registerAll(PlaceboUtil.toArray(ENCHANTMENTS));
	}
	
	@SubscribeEvent
	public void villagerProfessions(Register<VillagerProfession> event) {
		event.getRegistry().registerAll(PlaceboUtil.toArray(VILLAGER_PROFESSIONS));
	}
	
	@SubscribeEvent
	public void entities(Register<EntityEntry> event) {
		event.getRegistry().registerAll(PlaceboUtil.toArray(ENTITIES));
	}
	
	@SubscribeEvent
	public void recipes(Register<IRecipe> event) {
		event.getRegistry().registerAll(PlaceboUtil.toArray(RECIPES));
	}



}
