package dev.shadowsoffire.placebo.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntityType;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntityType.TickSide;
import dev.shadowsoffire.placebo.menu.MenuUtil;
import dev.shadowsoffire.placebo.menu.MenuUtil.PosFactory;
import dev.shadowsoffire.placebo.util.DeferredSet;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityType.EntityFactory;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.MenuType.MenuSupplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

/**
 * Helper class that acts as a single point of entry for deferred registration of all registry entries.
 * <p>
 * Provides methods for the most common types of objects, as well as {@link #custom(String, ResourceKey, Supplier)} for other types.
 * <p>
 * Registration factories will only be invoked during registration for the target registry, using the same semantics of {@link DeferredRegister}.
 */
public class DeferredHelper {

    protected final String modid;
    protected final Map<ResourceKey<? extends Registry<?>>, List<Registrar<?>>> objects;
    protected final Map<ResourceKey<? extends Registry<?>>, List<Holder<?>>> resolvedObjects;

    /**
     * Creates a new DeferredHelper. DeferredHelpers must be registered to the mod event bus via {@link IEventBus#register}
     *
     * @param modid The modid of the owning mod.
     * @return A new DeferredHelper.
     */
    public static DeferredHelper create(String modid) {
        return new DeferredHelper(modid);
    }

    protected DeferredHelper(String modid) {
        this.modid = modid;
        this.objects = new IdentityHashMap<>();
        this.resolvedObjects = new IdentityHashMap<>();
    }

    /**
     * Registers a {@link Block} using a supplier.
     */
    public <T extends Block> DeferredBlock<T> block(String path, Supplier<T> factory) {
        this.register(path, Registries.BLOCK, factory);
        return DeferredBlock.createBlock(ResourceLocation.fromNamespaceAndPath(this.modid, path));
    }

    /**
     * Registers a {@link Block} with a reference to its constructor, configuring a new {@link Block.Properties} instance with the supplied operator.
     */
    public <T extends Block> DeferredBlock<T> block(String path, Function<Block.Properties, T> ctor, UnaryOperator<Block.Properties> properties) {
        return this.block(path, () -> ctor.apply(properties.apply(Block.Properties.of())));
    }

    /**
     * Registers a {@link Fluid} using a supplier.
     */
    public <T extends Fluid> DeferredHolder<Fluid, T> fluid(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.FLUID, factory);
    }

    /**
     * Registers an {@link Item} using a supplier.
     */
    public <T extends Item> DeferredItem<T> item(String path, Supplier<T> factory) {
        this.register(path, Registries.ITEM, factory);
        return DeferredItem.createItem(ResourceLocation.fromNamespaceAndPath(this.modid, path));
    }

    /**
     * Registers an {@link Item} with a reference to its constructor, configuring a new {@link Item.Properties} instance with the supplied operator.
     */
    public <T extends Item> DeferredItem<T> item(String path, Function<Item.Properties, T> ctor, UnaryOperator<Item.Properties> properties) {
        return item(path, () -> ctor.apply(properties.apply(new Item.Properties())));
    }

    /**
     * Registers an {@link Item} with a reference to its constructor, using a default {@link Item.Properties} instance.
     */
    public <T extends Item> DeferredItem<T> item(String path, Function<Item.Properties, T> ctor) {
        return item(path, ctor, UnaryOperator.identity());
    }

    /**
     * Registers a subclass of {@link BlockItem} given a target block, the constructor, and an {@link Item.Properties} factory.
     */
    public <T extends BlockItem> DeferredItem<T> blockItem(String path, Holder<Block> block, BiFunction<Block, Item.Properties, T> ctor, UnaryOperator<Item.Properties> properties) {
        return item(path, () -> ctor.apply(block.value(), properties.apply(new Item.Properties())));
    }

    /**
     * Registers a {@link BlockItem} given a target block and an {@link Item.Properties} factory.
     */
    public DeferredItem<BlockItem> blockItem(String path, Holder<Block> block, UnaryOperator<Item.Properties> properties) {
        return blockItem(path, block, BlockItem::new, properties);
    }

    /**
     * Registers a {@link BlockItem} given a target block, using a default {@link Item.Properties} instance.
     */
    public DeferredItem<BlockItem> blockItem(String path, Holder<Block> block) {
        return blockItem(path, block, UnaryOperator.identity());
    }

    /**
     * Registers a {@link MobEffect} using a supplier.
     */
    public <T extends MobEffect> DeferredHolder<MobEffect, T> effect(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.MOB_EFFECT, factory);
    }

    /**
     * Registers a {@link SoundEvent} using a supplier.
     */
    public <T extends SoundEvent> DeferredHolder<SoundEvent, T> sound(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.SOUND_EVENT, factory);
    }

    /**
     * Registers a {@link SoundEvent} using the given path via {@link SoundEvent#createVariableRangeEvent}.
     */
    public Holder<SoundEvent> sound(String path) {
        return this.sound(path, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(this.modid, path)));
    }

    /**
     * Registers a {@link Potion} using a supplier.
     */
    public <T extends Potion> DeferredHolder<Potion, T> potion(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.POTION, factory);
    }

    /**
     * Registers a {@link Potion} containing only one mob effect, with the language key of the underlying mob effect.
     */
    public DeferredHolder<Potion, Potion> singlePotion(String path, Supplier<MobEffectInstance> factory) {
        return this.registerDH(path, Registries.POTION, () -> {
            MobEffectInstance inst = factory.get();
            ResourceLocation key = inst.getEffect().getKey().location();
            return new Potion(key.toLanguageKey(), inst);
        });
    }

    /**
     * Registers a {@link Potion} containing multiple mob effects, with a language key automatically generated from the path.
     */
    public DeferredHolder<Potion, Potion> multiPotion(String path, Supplier<List<MobEffectInstance>> factory) {
        String key = ResourceLocation.fromNamespaceAndPath(this.modid, path).toLanguageKey("potion");
        return this.registerDH(path, Registries.POTION, () -> new Potion(key, factory.get().toArray(new MobEffectInstance[0])));
    }

    /**
     * Registers an {@link EntityType} using a supplier.
     */
    public <U extends Entity, T extends EntityType<U>> DeferredHolder<EntityType<?>, T> entity(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.ENTITY_TYPE, factory);
    }

    /**
     * Registers an {@link EntityType} given the {@link EntityFactory}, {@link MobCategory}, and a function to configure the type.
     */
    public <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> entity(String path, EntityFactory<T> factory, MobCategory category, UnaryOperator<EntityType.Builder<T>> op) {
        String key = ResourceLocation.fromNamespaceAndPath(this.modid, path).toLanguageKey("entity");
        return this.entity(path, () -> op.apply(EntityType.Builder.of(factory, category)).build(key));
    }

    /**
     * Registers a {@link BlockEntityType} given the {@link BlockEntitySupplier} and a supplier to the set of valid blocks.
     */
    public <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> blockEntity(String path, BlockEntitySupplier<T> factory, Supplier<Set<Block>> validBlocks) {
        return this.registerDH(path, Registries.BLOCK_ENTITY_TYPE, () -> new BlockEntityType<T>(factory, validBlocks.get(), null));
    }

    /**
     * Registers a {@link BlockEntityType} given the {@link BlockEntitySupplier} and a vararg array of valid blocks.
     * <p>
     * Immediately constructs the {@link BlockEntityType} and returns it. Registration is deferred until the appropriate time. The set of valid blocks will not
     * attempt to be resolved until registration.
     */
    @SafeVarargs
    public final <T extends BlockEntity> BlockEntityType<T> blockEntity(String path, BlockEntitySupplier<T> factory, Holder<Block>... validBlocks) {
        unfreezeBETypeRegistry();
        BlockEntityType<T> type = new BlockEntityType<>(factory, new DeferredSet<>(() -> Arrays.stream(validBlocks).map(Holder::value).collect(Collectors.toSet())), null);
        this.register(path, Registries.BLOCK_ENTITY_TYPE, () -> {
            type.getValidBlocks(); // Force resolution of the DeferredSet during registration
            return type;
        });
        return type;
    }

    /**
     * Registers a {@link TickingBlockEntityType} for a {@link TickingBlockEntity} given the {@link BlockEntitySupplier}, the target {@link TickSide}, and a vararg
     * array of valid blocks.
     * <p>
     * Immediately constructs the {@link BlockEntityType} and returns it. Registration is deferred until the appropriate time. The set of valid blocks will not
     * attempt to be resolved until registration.
     */
    @SafeVarargs
    public final <T extends BlockEntity & TickingBlockEntity> TickingBlockEntityType<T> tickingBlockEntity(String path, BlockEntitySupplier<T> factory, TickSide side, Holder<Block>... validBlocks) {
        unfreezeBETypeRegistry();
        TickingBlockEntityType<T> type = new TickingBlockEntityType<>(factory, new DeferredSet<>(() -> Arrays.stream(validBlocks).map(Holder::value).collect(Collectors.toSet())), side);
        this.register(path, Registries.BLOCK_ENTITY_TYPE, () -> {
            type.getValidBlocks(); // Force resolution of the DeferredSet during registration
            return type;
        });
        return type;
    }

    /**
     * Registers a {@link ParticleType} using a supplier.
     */
    public <U extends ParticleOptions, T extends ParticleType<U>> DeferredHolder<ParticleType<?>, T> particle(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.PARTICLE_TYPE, factory);
    }

    /**
     * Registers a {@link SimpleParticleType}.
     */
    public DeferredHolder<ParticleType<?>, SimpleParticleType> simpleParticle(String path, boolean overrideLimit) {
        return this.particle(path, () -> new SimpleParticleType(overrideLimit));
    }

    /**
     * Registers a {@link ParticleType} with custom serialization. Both the codec and stream codec must be provided.
     */
    public <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> particle(String path, boolean overrideLimit, Function<ParticleType<T>, MapCodec<T>> codec,
        Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodec) {
        return this.particle(path, () -> {
            return new ParticleType<T>(overrideLimit){

                @Override
                public MapCodec<T> codec() {
                    return codec.apply(this);
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                    return streamCodec.apply(this);
                }

            };
        });
    }

    /**
     * Registers a {@link MenuType} using a supplier.
     */
    public <U extends AbstractContainerMenu, T extends MenuType<U>> T menuType(String path, T type) {
        this.register(path, Registries.MENU, () -> type);
        return type;
    }

    /**
     * Registers a {@link MenuType} for the provided {@link MenuSupplier}.
     */
    public <T extends AbstractContainerMenu> MenuType<T> menu(String path, MenuSupplier<T> factory) {
        return this.menuType(path, MenuUtil.type(factory));
    }

    /**
     * Registers a {@link MenuType} for the provided {@link PosFactory}.
     */
    public <T extends AbstractContainerMenu> MenuType<T> menuWithPos(String path, PosFactory<T> factory) {
        return this.menuType(path, MenuUtil.posType(factory));
    }

    /**
     * Registers a {@link MenuType} for the provided {@link IContainerFactory}.
     */
    public <T extends AbstractContainerMenu> MenuType<T> menuWithData(String path, IContainerFactory<T> factory) {
        return this.menuType(path, MenuUtil.bufType(factory));
    }

    /**
     * Registers a {@link RecipeType} using a supplier.
     */
    public <C extends RecipeInput, U extends Recipe<C>, T extends RecipeType<U>> DeferredHolder<RecipeType<?>, T> recipe(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.RECIPE_TYPE, factory);
    }

    /**
     * Registers a {@link RecipeType} using {@link RecipeType#simple(ResourceLocation)}.
     * <p>
     * Immediately constructs the {@link RecipeType} and returns it. Registration is deferred until the appropriate time.
     */
    public <C extends RecipeInput, U extends Recipe<C>> RecipeType<U> recipe(String path) {
        RecipeType<U> type = RecipeType.simple(ResourceLocation.fromNamespaceAndPath(this.modid, path));
        this.recipe(path, () -> type);
        return type;
    }

    /**
     * Registers a {@link RecipeSerializer} using a supplier.
     */
    public <C extends RecipeInput, U extends Recipe<C>, T extends RecipeSerializer<U>> DeferredHolder<RecipeSerializer<?>, T> recipeSerializer(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.RECIPE_SERIALIZER, factory);
    }

    /**
     * Registers an {@link Attribute} using a supplier.
     */
    public <T extends Attribute> DeferredHolder<Attribute, T> attribute(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.ATTRIBUTE, factory);
    }

    /**
     * Registers a {@link RangedAttribute}.
     */
    public DeferredHolder<Attribute, RangedAttribute> rangedAttribute(String path, double defaultValue, double min, double max) {
        String key = ResourceLocation.fromNamespaceAndPath(this.modid, path).toLanguageKey("attribute");
        return this.attribute(path, () -> new RangedAttribute(key, defaultValue, min, max));
    }

    /**
     * Registers a {@link StatType} using a supplier.
     */
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
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.modid, path);
            Stats.CUSTOM.get(id, formatter);
            return id;
        });
    }

    /**
     * Registers a {@link Feature} using a supplier.
     */
    public <U extends FeatureConfiguration, T extends Feature<U>> DeferredHolder<Feature<?>, T> feature(String path, Supplier<T> factory) {
        return this.registerDH(path, Registries.FEATURE, factory);
    }

    /**
     * Registers a {@link CreativeModeTab} that is configured with the supplied operator.
     */
    public DeferredHolder<CreativeModeTab, CreativeModeTab> creativeTab(String path, UnaryOperator<CreativeModeTab.Builder> operator) {
        return this.registerDH(path, Registries.CREATIVE_MODE_TAB, () -> operator.apply(CreativeModeTab.builder()).build());
    }

    /**
     * Registers an {@linkplain DataComponentType enchantment effect component} that is configured with the supplied operator.
     * <p>
     * Immediately constructs the {@link DataComponentType} and returns it. Registration is deferred until the appropriate time.
     */
    public <T> DataComponentType<T> enchantmentEffect(String path, UnaryOperator<DataComponentType.Builder<T>> operator) {
        DataComponentType<T> type = operator.apply(DataComponentType.builder()).build();
        this.register(path, Registries.ENCHANTMENT_EFFECT_COMPONENT_TYPE, () -> type);
        return type;
    }

    /**
     * Registers a {@link DataComponentType} that is configured with the supplied operator.
     * <p>
     * Immediately constructs the {@link DataComponentType} and returns it. Registration is deferred until the appropriate time.
     */
    public <T> DataComponentType<T> component(String path, UnaryOperator<DataComponentType.Builder<T>> operator) {
        DataComponentType<T> type = operator.apply(DataComponentType.builder()).build();
        this.register(path, Registries.DATA_COMPONENT_TYPE, () -> type);
        return type;
    }

    /**
     * Registers an {@link AttachmentType} with the specified default value, that is configured with the supplied operator.
     * <p>
     * Immediately constructs the {@link AttachmentType} and returns it. Registration is deferred until the appropriate time.
     */
    public <T> AttachmentType<T> attachment(String path, Supplier<T> defaultValue, UnaryOperator<AttachmentType.Builder<T>> operator) {
        AttachmentType<T> type = operator.apply(AttachmentType.builder(defaultValue)).build();
        this.register(path, NeoForgeRegistries.Keys.ATTACHMENT_TYPES, () -> type);
        return type;
    }

    /**
     * Registers an {@link AttachmentType} with the specified default value, that is configured with the supplied operator.
     * <p>
     * Immediately constructs the {@link AttachmentType} and returns it. Registration is deferred until the appropriate time.
     */
    public <T> AttachmentType<T> attachment(String path, Function<IAttachmentHolder, T> defaultValue, UnaryOperator<AttachmentType.Builder<T>> operator) {
        AttachmentType<T> type = operator.apply(AttachmentType.builder(defaultValue)).build();
        this.register(path, NeoForgeRegistries.Keys.ATTACHMENT_TYPES, () -> type);
        return type;
    }

    /**
     * Registers a {@link LootPoolEntryType} and returns it.
     */
    public LootPoolEntryType lootPoolEntry(String path, LootPoolEntryType type) {
        this.register(path, Registries.LOOT_POOL_ENTRY_TYPE, () -> type);
        return type;
    }

    /**
     * Registers a codec for an {@link IGlobalLootModifier} and returns it.
     */
    public <T extends IGlobalLootModifier> MapCodec<T> lootModifier(String path, MapCodec<T> codec) {
        this.register(path, NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, () -> codec);
        return codec;
    }

    /**
     * Registers a codec for a {@link LootItemCondition} and returns the new {@link LootItemConditionType}.
     */
    public LootItemConditionType lootCondition(String path, MapCodec<? extends LootItemCondition> codec) {
        LootItemConditionType type = new LootItemConditionType(codec);
        this.register(path, Registries.LOOT_CONDITION_TYPE, () -> type);
        return type;
    }

    /**
     * Registers an {@link IngredientType} and returns it.
     */
    public <T extends ICustomIngredient> IngredientType<T> ingredient(String path, IngredientType<T> type) {
        this.register(path, NeoForgeRegistries.Keys.INGREDIENT_TYPES, () -> type);
        return type;
    }

    /**
     * Registers a {@link CriterionTrigger} and returns it.
     */
    public <T extends CriterionTrigger<?>> T criteriaTrigger(String path, T trigger) {
        this.register(path, Registries.TRIGGER_TYPE, () -> trigger);
        return trigger;
    }

    /**
     * Registers an {@link ItemSubPredicate.Type} and returns it.
     */
    public <T extends ItemSubPredicate> ItemSubPredicate.Type<T> itemSubPredicate(String path, Codec<T> codec) {
        ItemSubPredicate.Type<T> type = new ItemSubPredicate.Type<>(codec);
        this.register(path, Registries.ITEM_SUB_PREDICATE_TYPE, () -> type);
        return type;
    }

    /**
     * Registers a {@link StructureProcessorType} and returns it.
     */
    public <T extends StructureProcessor> StructureProcessorType<T> structureProcessor(String path, MapCodec<T> codec) {
        StructureProcessorType<T> type = () -> codec;
        this.register(path, Registries.STRUCTURE_PROCESSOR, () -> type);
        return type;
    }

    /**
     * Registers a custom object to the target registry using a supplier.
     */
    public <R, T extends R> DeferredHolder<R, T> custom(String path, ResourceKey<Registry<R>> registry, Supplier<T> factory) {
        return this.registerDH(path, registry, factory);
    }

    /**
     * Returns a list of all objects for a given registry that were registered by this {@link DeferredHelper}.
     * <p>
     * If registration for the target registry has not happened yet, this list will be empty.
     */
    @ApiStatus.Experimental
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <R> List<Holder<R>> getRegisteredObjects(ResourceKey<? extends Registry<R>> key) {
        return (List) Collections.unmodifiableList(this.resolvedObjects.getOrDefault(key, List.of()));
    }

    /**
     * Stages the supplier for registration without creating a {@link DeferredHolder}.
     */
    protected <R, T extends R> void register(String path, ResourceKey<Registry<R>> regKey, Supplier<T> factory) {
        List<Registrar<?>> registrars = this.objects.computeIfAbsent(regKey, k -> new ArrayList<>());
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.modid, path);
        registrars.add(new Registrar<>(id, factory));
    }

    /**
     * Stages the supplier for registration and creates a {@link DeferredHolder} pointing to it.
     */
    protected <R, T extends R> DeferredHolder<R, T> registerDH(String path, ResourceKey<Registry<R>> regKey, Supplier<T> factory) {
        this.register(path, regKey, factory);
        return DeferredHolder.create(regKey, ResourceLocation.fromNamespaceAndPath(this.modid, path));
    }

    @SubscribeEvent
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void register(RegisterEvent e) {
        Registry registry = e.getRegistry();
        for (Registrar<?> registrar : this.objects.getOrDefault(e.getRegistryKey(), Collections.emptyList())) {
            try {
                Object obj = registrar.factory.get();
                Registry.register(registry, registrar.id, obj);
                this.resolvedObjects.computeIfAbsent(e.getRegistryKey(), k -> new ArrayList<>()).add(registry.wrapAsHolder(obj));
            }
            catch (Throwable ex) {
                Placebo.LOGGER.error("Exception thrown during registration of {}", registrar.id);
                throw ex;
            }
        }
        this.objects.remove(e.getRegistryKey());
    }

    /**
     * BE Types have an intrusive holder, so on top of {@link DeferredSet}, we also need to unfreeze the registry to construct them.
     */
    @SuppressWarnings("deprecation")
    private static void unfreezeBETypeRegistry() {
        ((MappedRegistry<BlockEntityType<?>>) BuiltInRegistries.BLOCK_ENTITY_TYPE).unfreeze();
    }

    protected static record Registrar<T>(ResourceLocation id, Supplier<T> factory) {

    }

}
