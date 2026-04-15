package dev.shadowsoffire.placebo.datagen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

/**
 * Extension of {@link RecipeProvider.Runner} which allows creating recipes using the syntax from Placebo's old RecipeHelper.
 * <p>
 * Shaped recipes are written out as the output, width, height, and then a row-major vararg array of the actual inputs.
 * The pattern will be inferred from the inputs.
 * <p>
 * In 26.1 the vanilla {@link RecipeProvider} became a tiny logic class that needs to be constructed with a
 * resolved {@link HolderLookup.Provider} and an open {@link RecipeOutput}, and the datagen {@link net.minecraft.data.DataProvider}
 * side lives on the nested {@link RecipeProvider.Runner}. This class exposes the same downstream-facing API (subclass, override
 * {@link #genRecipes}, call {@link #addShaped}/{@link #addShapeless}) by being a {@code Runner} that internally stands up an
 * anonymous {@link RecipeProvider} and temporarily exposes the live {@link RecipeOutput} on a field.
 */
public abstract class LegacyRecipeProvider extends RecipeProvider.Runner {

    private final String modid;
    protected final Set<String> usedPaths = new HashSet<>();

    /**
     * Populated while an inner {@link RecipeProvider#buildRecipes()} is running, so the
     * {@link #addShaped}/{@link #addShapeless} helpers can emit recipes directly on {@code this}.
     */
    @Nullable
    protected RecipeOutput recipeOutput;
    @Nullable
    protected HolderLookup.Provider currentRegistries;

    public LegacyRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        super(output, registries);
        this.modid = modid;
    }

    /**
     * Subclasses implement this to declare their recipes. The {@link RecipeOutput} and
     * {@link HolderLookup.Provider} are provided both as arguments and as the fields
     * {@link #recipeOutput} / {@link #currentRegistries} for the duration of the call.
     */
    protected abstract void genRecipes(RecipeOutput recipeOutput, HolderLookup.Provider registries);

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        final LegacyRecipeProvider self = this;
        return new RecipeProvider(registries, output){
            @Override
            protected void buildRecipes() {
                self.recipeOutput = this.output;
                self.currentRegistries = this.registries;
                try {
                    self.genRecipes(this.output, this.registries);
                }
                finally {
                    self.recipeOutput = null;
                    self.currentRegistries = null;
                }
            }
        };
    }

    /**
     * Stages a {@link ShapedRecipe} for datagen.
     *
     * @param key    The resource location of the recipe.
     * @param group  The recipe book group of the recipe.
     * @param output A {@linkplain #makeStack(Object) stack-like} output object.
     * @param width  The width of the recipe.
     * @param height The height of the recipe.
     * @param input  A row-major vararg array of {@linkplain #createInput(boolean, Object...) input-like} objects. Must be the same length as width * height.
     */
    public void addShaped(Identifier key, String group, Object output, int width, int height, Object... input) {
        if (width * height != input.length) {
            throw new UnsupportedOperationException("Attempted to create invalid shaped recipe. Expected " + width * height + " inputs, but got " + input.length);
        }

        Recipe.CommonInfo commonInfo = new Recipe.CommonInfo(true);
        CraftingRecipe.CraftingBookInfo bookInfo = new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, group);
        ShapedRecipe recipe = new ShapedRecipe(commonInfo, bookInfo, toPattern(width, height, createInput(true, input)), makeTemplate(output));
        this.recipeOutput.accept(recipeKey(key), recipe, null);
    }

    /**
     * Stages a {@link ShapelessRecipe} for datagen.
     *
     * @param key    The resource location of the recipe.
     * @param group  The recipe book group of the recipe.
     * @param output A {@linkplain #makeStack(Object) stack-like} output object.
     * @param inputs A row-major vararg array of {@linkplain #createInput(boolean, Object...) input-like} objects. Empty inputs are not permitted.
     */
    public void addShapeless(Identifier key, String group, Object output, Object... inputs) {
        Recipe.CommonInfo commonInfo = new Recipe.CommonInfo(true);
        CraftingRecipe.CraftingBookInfo bookInfo = new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, group);
        ShapelessRecipe recipe = new ShapelessRecipe(commonInfo, bookInfo, makeTemplate(output), createInput(false, inputs));
        this.recipeOutput.accept(recipeKey(key), recipe, null);
    }

    /**
     * Stages a {@link ShapedRecipe} for datagen using the {@link #modid} as the group.
     *
     * @see #addShaped(Identifier, String, Object, int, int, Object...)
     */
    public void addShaped(Identifier key, Object output, int width, int height, Object... input) {
        this.addShaped(key, this.modid, output, width, height, input);
    }

    /**
     * Stages a {@link ShapelessRecipe} for datagen using the {@link #modid} as the group.
     *
     * @see #addShapeless(Identifier, String, Object, Object...)
     */
    public void addShapeless(Identifier key, Object output, Object... inputs) {
        this.addShapeless(key, this.modid, output, inputs);
    }

    /**
     * Stages a {@link ShapedRecipe} for datagen using the {@link #modid} as the group and the key's namespace,
     * while automatically determining a path from the output item.
     *
     * @see #addShaped(Identifier, String, Object, int, int, Object...)
     */
    public void addShaped(Object output, int width, int height, Object... input) {
        ItemStackTemplate out = makeTemplate(output);
        String path = this.resolvePath(out);
        this.addShaped(Identifier.fromNamespaceAndPath(this.modid, path), this.modid, out, width, height, input);
    }

    /**
     * Stages a {@link ShapelessRecipe} for datagen using the {@link #modid} as the group and the key's namespace,
     * while automatically determining a path from the output item.
     *
     * @see #addShapeless(Identifier, String, Object, Object...)
     */
    public void addShapeless(Object output, Object... inputs) {
        ItemStackTemplate out = makeTemplate(output);
        String path = this.resolvePath(out);
        this.addShapeless(Identifier.fromNamespaceAndPath(this.modid, path), this.modid, out, inputs);
    }

    /**
     * Creates an {@link Ingredient} matching a potion item with the given potion type.
     */
    public static Ingredient potionIngredient(Holder<Potion> type) {
        return DataComponentIngredient.of(false, DataComponents.POTION_CONTENTS, new PotionContents(type), Items.POTION);
    }

    /**
     * Resolves a potential path for the given output template. Avoids duplicates by appending underscores.
     */
    protected String resolvePath(ItemStackTemplate output) {
        String path = BuiltInRegistries.ITEM.getKey(output.item().value()).getPath();
        while (this.usedPaths.contains(path)) {
            path += "_";
        }
        this.usedPaths.add(path);
        return path;
    }

    /**
     * Transforms an object that could be converted into an {@link ItemStackTemplate} into one.
     * <p>
     * During 26.1 datagen, default components may not yet be bound on registered items, so this helper
     * avoids constructing an {@link ItemStack} and instead builds the template directly from a holder.
     *
     * @param thing A candidate object. One of {@link ItemStackTemplate}, {@link ItemStack}, {@link ItemLike}, or a {@link Holder} containing an {@link ItemLike}.
     * @throws IllegalArgumentException if the type of object is unknown.
     */
    protected static ItemStackTemplate makeTemplate(Object thing) {
        if (thing instanceof ItemStackTemplate template) {
            return template;
        }
        if (thing instanceof ItemStack stack) {
            return new ItemStackTemplate(stack.getItem(), stack.getCount(), stack.getComponentsPatch());
        }
        if (thing instanceof ItemLike il) {
            return new ItemStackTemplate(il.asItem().builtInRegistryHolder(), 1, DataComponentPatch.EMPTY);
        }
        if (thing instanceof Holder<?> h && h.value() instanceof ItemLike il) {
            return new ItemStackTemplate(il.asItem().builtInRegistryHolder(), 1, DataComponentPatch.EMPTY);
        }
        throw new IllegalArgumentException("Attempted to create an ItemStackTemplate from something that cannot be converted: " + thing);
    }

    private static ResourceKey<Recipe<?>> recipeKey(Identifier id) {
        return ResourceKey.create(Registries.RECIPE, id);
    }

    /**
     * Converts an array of "input-like" objects into a list of {@link Ingredient}s.
     * <p>
     * The created {@link Ingredient} depends on the type of the object:
     * <ul>
     * <li>A {@link TagKey} will be converted to a tag ingredient.</li>
     * <li>A {@link String} will be parsed into a {@link Identifier}, and treated as a {@link TagKey}.</li>
     * <li>An {@link ItemStack} will be converted into a single-stack ingredient. Component data is preserved via {@link DataComponentIngredient}.</li>
     * <li>An {@link ItemLike} or {@link Holder} will be passed to {@link #makeStack(Object)} and treated as an {@link ItemStack}.</li>
     * <li>An {@link Ingredient} will be used directly.</li>
     * </ul>
     * If empty inputs are allowed, then {@code null} or {@link ItemStack#EMPTY} will be converted to a sentinel empty {@link Ingredient}.
     *
     * @param allowEmpty If empty input values are allowed.
     * @param inputArr   An array of objects to translate into ingredients.
     * @return A list of ingredients resulting from the conversion.
     * @throws UnsupportedOperationException if the object cannot be converted.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected NonNullList<Ingredient> createInput(boolean allowEmpty, Object... inputArr) {
        HolderGetter<Item> items = this.currentRegistries.lookupOrThrow(Registries.ITEM);
        // Cache so repeated inputs resolve to the SAME Ingredient instance — 26.1 Ingredients do not
        // implement content-based equality, so toPattern's dedup HashMap relies on instance identity.
        Map<Object, Ingredient> cache = new HashMap<>();
        NonNullList<Ingredient> inputL = NonNullList.create();
        for (int i = 0; i < inputArr.length; i++) {
            Object input = inputArr[i];
            if (allowEmpty && (input == null || input == ItemStack.EMPTY)) {
                inputL.add(i, EMPTY_INGREDIENT_SENTINEL);
                continue;
            }
            Ingredient cached = cache.get(input);
            if (cached != null) {
                inputL.add(i, cached);
                continue;
            }
            Ingredient ingredient;
            if (input instanceof TagKey tag) {
                ingredient = Ingredient.of(items.getOrThrow((TagKey<Item>) tag));
            }
            else if (input instanceof String str) {
                TagKey<Item> parsed = ItemTags.create(Identifier.parse(str));
                ingredient = Ingredient.of(items.getOrThrow(parsed));
            }
            else if (input instanceof ItemStackTemplate template) {
                if (template.components().isEmpty()) {
                    ingredient = Ingredient.of(template.item().value());
                }
                else {
                    ingredient = DataComponentIngredient.of(false, template);
                }
            }
            else if (input instanceof ItemStack stack && !stack.isEmpty()) {
                if (stack.getComponentsPatch().isEmpty()) {
                    ingredient = Ingredient.of(stack.getItem());
                }
                else {
                    ingredient = DataComponentIngredient.of(false, stack);
                }
            }
            else if (input instanceof ItemLike il) {
                ingredient = Ingredient.of(il.asItem());
            }
            else if (input instanceof Holder<?> h && h.value() instanceof ItemLike il) {
                ingredient = Ingredient.of(il.asItem());
            }
            else if (input instanceof Ingredient ing) {
                ingredient = ing;
            }
            else {
                throw new UnsupportedOperationException("Attempted to add invalid recipe. Input " + input + " not allowed.");
            }
            cache.put(input, ingredient);
            inputL.add(i, ingredient);
        }
        return inputL;
    }

    /**
     * Sentinel used in place of the removed {@code Ingredient.EMPTY}. Shaped recipes represent empty slots
     * through the pattern layout rather than through an empty {@link Ingredient}. The inferred-pattern path
     * recognizes this sentinel and emits a space in the pattern, stripping the slot from the key map.
     */
    private static final Ingredient EMPTY_INGREDIENT_SENTINEL = Ingredient.of(Items.SEA_PICKLE);

    /**
     * Automatically determines a {@link ShapedRecipePattern} from a list of shaped recipe inputs.
     * <p>
     * Uses the first available character from the first valid item in each ingredient to form the key.
     */
    protected static ShapedRecipePattern toPattern(int width, int height, NonNullList<Ingredient> input) {
        Map<Character, Ingredient> key = new HashMap<>();
        Map<Ingredient, Character> chars = new HashMap<>();
        List<String> rows = new ArrayList<>(height);
        for (int h = 0; h < height; h++) {
            String row = "";
            for (int w = 0; w < width; w++) {
                Ingredient ing = input.get(h * width + w);
                if (ing == EMPTY_INGREDIENT_SENTINEL) {
                    row += ' ';
                    continue;
                }
                if (chars.containsKey(ing)) {
                    row += chars.get(ing);
                    continue;
                }
                else {
                    Character c = getFirstChar(chars.values(), ing);
                    key.put(c, ing);
                    chars.put(ing, c);
                    row += c;
                    continue;
                }
            }
            rows.add(row);
        }
        key.remove(' ');
        return ShapedRecipePattern.of(key, rows);
    }

    /**
     * Resolves the first available character from an ingredient, given the currently in-use characters.
     */
    protected static Character getFirstChar(Collection<Character> inUse, Ingredient ing) {
        String path;
        if (ing.isCustom()) {
            ICustomIngredient custom = ing.getCustomIngredient();
            Item item = custom.items().map(Holder::value).findFirst().orElse(Items.AIR);
            path = BuiltInRegistries.ITEM.getKey(item).getPath();
        }
        else {
            HolderSet<Item> values = ing.getValues();
            if (values instanceof HolderSet.Named<Item> named) {
                path = named.key().location().getPath();
            }
            else {
                Holder<Item> first = values.stream().findFirst().orElse(null);
                if (first == null) {
                    throw new UnsupportedOperationException("Empty ingredient values for: " + ing);
                }
                path = BuiltInRegistries.ITEM.getKey(first.value()).getPath();
            }
        }
        path = path.toUpperCase(Locale.ROOT);
        for (char c : path.toCharArray()) {
            if (!inUse.contains(c)) {
                return c;
            }
        }
        throw new UnsupportedOperationException("Failed to find any unused characters for ingredient: " + ing);
    }
}
