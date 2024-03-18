package dev.shadowsoffire.placebo.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.RegisterEvent;

public class DeferredHelper {

    protected final String modid;
    protected final Map<ResourceKey<? extends Registry<?>>, List<Registrar<?>>> objects;

    /**
     * Creates a new DeferredHelper. DeferredHelpers must be registered to the mod event bus via {@link IEventBus#register}
     *
     * @param modid The modid of the owning mod.
     * @return A new DeferredHelper.
     */
    public static DeferredHelper create(String modid) {
        DeferredHelper helper = new DeferredHelper(modid);
        return helper;
    }

    protected DeferredHelper(String modid) {
        this.modid = modid;
        this.objects = new IdentityHashMap<>();
    }

    public <T extends Block> DeferredBlock<T> block(String path, Supplier<T> factory) {
        this.register(path, Registries.BLOCK, factory);
        return DeferredBlock.createBlock(new ResourceLocation(modid, path));
    }

    public <T extends Fluid> DeferredHolder<Fluid, T> fluid(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.FLUID, factory);
    }

    public <T extends Item> DeferredItem<T> item(String path, Supplier<T> factory) {
        this.register(path, Registries.ITEM, factory);
        return DeferredItem.createItem(new ResourceLocation(modid, path));
    }

    public <T extends MobEffect> DeferredHolder<MobEffect, T> effect(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.MOB_EFFECT, factory);
    }

    public <T extends SoundEvent> DeferredHolder<SoundEvent, T> sound(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.SOUND_EVENT, factory);
    }

    public Holder<SoundEvent> sound(String path) {
        return sound(path, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(modid, path)));
    }

    public <T extends Potion> DeferredHolder<Potion, T> potion(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.POTION, factory);
    }

    public DeferredHolder<Potion, Potion> singlePotion(String path, Supplier<MobEffectInstance> factory) {
        return this.registerDH(path, Registries.POTION, () -> {
            MobEffectInstance inst = factory.get();
            ResourceLocation key = BuiltInRegistries.MOB_EFFECT.getKey(inst.getEffect());
            return new Potion(key.toLanguageKey(), inst);
        });
    }

    public DeferredHolder<Potion, Potion> multiPotion(String path, Supplier<List<MobEffectInstance>> factory) {
        return this.registerDH(path, Registries.POTION, () -> new Potion(this.modid + "." + path, factory.get().toArray(new MobEffectInstance[0])));
    }

    public <T extends Enchantment> DeferredHolder<Enchantment, T> enchant(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.ENCHANTMENT, factory);
    }

    public <U extends Entity, T extends EntityType<U>> DeferredHolder<EntityType<?>, T> entity(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.ENTITY_TYPE, factory);
    }

    public <U extends BlockEntity, T extends BlockEntityType<U>> DeferredHolder<BlockEntityType<?>, T> blockEntity(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.BLOCK_ENTITY_TYPE, factory);
    }

    public <U extends ParticleOptions, T extends ParticleType<U>> DeferredHolder<ParticleType<?>, T> particle(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.PARTICLE_TYPE, factory);
    }

    public <U extends AbstractContainerMenu, T extends MenuType<U>> DeferredHolder<MenuType<?>, T> menu(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.MENU, factory);
    }

    public <T extends PaintingVariant> DeferredHolder<PaintingVariant, T> painting(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.PAINTING_VARIANT, factory);
    }

    public <C extends Container, U extends Recipe<C>, T extends RecipeType<U>> DeferredHolder<RecipeType<?>, T> recipe(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.RECIPE_TYPE, factory);
    }

    public <C extends Container, U extends Recipe<C>, T extends RecipeSerializer<U>> DeferredHolder<RecipeSerializer<?>, T> recipeSerializer(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.RECIPE_SERIALIZER, factory);
    }

    public <T extends Attribute> DeferredHolder<Attribute, T> attribute(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.ATTRIBUTE, factory);
    }

    public <S, U extends StatType<S>, T extends StatType<U>> DeferredHolder<StatType<?>, T> stat(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.STAT_TYPE, factory);
    }

    /**
     * Creates a custom stat with the given path and formatter.<br>
     * Calling {@link StatType#get} on {@link Stats#CUSTOM} is required for full registration, for some reason.
     *
     * @see Stats#makeCustomStat
     */
    public Holder<ResourceLocation> customStat(String path, StatFormatter formatter) {
        return this.registerDH(path, Registries.CUSTOM_STAT, () -> {
            ResourceLocation id = new ResourceLocation(this.modid, path);
            Stats.CUSTOM.get(id, formatter);
            return id;
        });
    }

    public <U extends FeatureConfiguration, T extends Feature<U>> DeferredHolder<Feature<?>, T> feature(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.FEATURE, factory);
    }

    public <T extends CreativeModeTab> DeferredHolder<CreativeModeTab, T> tab(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.CREATIVE_MODE_TAB, factory);
    }

    public <R, T extends R> DeferredHolder<R, T> custom(String path, ResourceKey<Registry<R>> registry, Supplier<T> factory) {
        return this.registerDH(path, registry, factory);
    }

    /**
     * Stages the supplier for registration.
     */
    protected <R, T extends R> void register(String path, ResourceKey<Registry<R>> regKey, Supplier<T> factory) {
        List<Registrar<?>> registrars = this.objects.computeIfAbsent(regKey, k -> new ArrayList<>());
        ResourceLocation id = new ResourceLocation(this.modid, path);
        registrars.add(new Registrar<>(id, factory));
    }

    /**
     * Stages the supplier for registration and creates a {@link DeferredHolder} pointing to it.
     */
    protected <R, T extends R> DeferredHolder<R, T> registerDH(String path, ResourceKey<Registry<R>> regKey, Supplier<T> factory) {
        register(path, regKey, factory);
        return DeferredHolder.create(regKey, new ResourceLocation(this.modid, path));
    }

    @SubscribeEvent
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void register(RegisterEvent e) {
        this.objects.getOrDefault(e.getRegistryKey(), Collections.emptyList()).forEach(registrar -> {
            e.register((ResourceKey) e.getRegistryKey(), registrar.id, (Supplier) registrar.factory);
        });
    }

    protected static record Registrar<T>(ResourceLocation id, Supplier<T> factory) {

    }

}
