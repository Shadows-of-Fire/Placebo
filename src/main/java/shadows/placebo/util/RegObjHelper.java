package shadows.placebo.util;

import java.util.Locale;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public class RegObjHelper {

    protected final String modid;

    public RegObjHelper(String modid) {
        this.modid = modid;
    }

    public <T extends Block> RegistryObject<T> block(String path) {
        return create(this.modid, path, ForgeRegistries.BLOCKS);
    }

    public <T extends Fluid> RegistryObject<T> fluid(String path) {
        return create(this.modid, path, ForgeRegistries.FLUIDS);
    }

    public <T extends Item> RegistryObject<T> item(String path) {
        return create(this.modid, path, ForgeRegistries.ITEMS);
    }

    public <T extends MobEffect> RegistryObject<T> effect(String path) {
        return create(this.modid, path, ForgeRegistries.MOB_EFFECTS);
    }

    public <T extends SoundEvent> RegistryObject<T> sound(String path) {
        return create(this.modid, path, ForgeRegistries.SOUND_EVENTS);
    }

    public <T extends Potion> RegistryObject<T> potion(String path) {
        return create(this.modid, path, ForgeRegistries.POTIONS);
    }

    public <T extends Enchantment> RegistryObject<T> enchant(String path) {
        return create(this.modid, path, ForgeRegistries.ENCHANTMENTS);
    }

    public <U extends Entity, T extends EntityType<U>> RegistryObject<T> entity(String path) {
        return create(this.modid, path, ForgeRegistries.ENTITY_TYPES);
    }

    public <U extends BlockEntity, T extends BlockEntityType<U>> RegistryObject<T> blockEntity(String path) {
        return create(this.modid, path, ForgeRegistries.BLOCK_ENTITY_TYPES);
    }

    public <U extends ParticleOptions, T extends ParticleType<U>> RegistryObject<T> particle(String path) {
        return create(this.modid, path, ForgeRegistries.PARTICLE_TYPES);
    }

    public <U extends AbstractContainerMenu, T extends MenuType<U>> RegistryObject<T> menu(String path) {
        return create(this.modid, path, ForgeRegistries.MENU_TYPES);
    }

    public <T extends PaintingVariant> RegistryObject<T> painting(String path) {
        return create(this.modid, path, ForgeRegistries.PAINTING_VARIANTS);
    }

    public <C extends Container, U extends Recipe<C>, T extends RecipeType<U>> RegistryObject<T> recipe(String path) {
        return create(this.modid, path, ForgeRegistries.RECIPE_TYPES);
    }

    public <C extends Container, U extends Recipe<C>, T extends RecipeSerializer<U>> RegistryObject<T> recipeSerializer(String path) {
        return create(this.modid, path, ForgeRegistries.RECIPE_SERIALIZERS);
    }

    public <T extends Attribute> RegistryObject<T> attribute(String path) {
        return create(this.modid, path, ForgeRegistries.ATTRIBUTES);
    }

    public <S, U extends Stat<S>, T extends StatType<U>> RegistryObject<T> stat(String path) {
        return create(this.modid, path, ForgeRegistries.STAT_TYPES);
    }

    public <U extends FeatureConfiguration, T extends Feature<U>> RegistryObject<T> feature(String path) {
        return create(this.modid, path, ForgeRegistries.FEATURES);
    }

    public <T> RegistryObject<T> custom(String path, IForgeRegistry<? super T> registry) {
        return create(this.modid, path, registry);
    }

    public static <T> RegistryObject<T> create(String modid, String path, IForgeRegistry<? super T> registry) {
        return RegistryObject.create(new ResourceLocation(modid, path.toLowerCase(Locale.ROOT)), registry);
    }

}
