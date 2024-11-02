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

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.ItemValue;
import net.minecraft.world.item.crafting.Ingredient.TagValue;
import net.minecraft.world.item.crafting.Ingredient.Value;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;

/**
 * Extension of {@link RecipeProvider} which allows creating recipes using the syntax from Placebo's old RecipeHelper.
 * <p>
 * Shaped recipes are written out as the output, width, height, and then a row-major vararg array of the actual inputs.
 * The pattern will be inferred from the inputs.
 */
public abstract class LegacyRecipeProvider extends RecipeProvider {

    private final String modid;
    protected final Set<String> usedPaths = new HashSet<>();

    /**
     * Populated during {@link #run(CachedOutput, Provider)} so that it doesn't need to be passed to each method individually.
     */
    private RecipeOutput recipeOutput;

    public LegacyRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, String modid) {
        super(output, registries);
        this.modid = modid;
    }

    protected abstract void genRecipes(RecipeOutput recipeOutput, HolderLookup.Provider registries);

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
    public void addShaped(ResourceLocation key, String group, Object output, int width, int height, Object... input) {
        if (width * height != input.length) {
            throw new UnsupportedOperationException("Attempted to create invalid shaped recipe. Expected " + width * height + " inputs, but got " + input.length);
        }

        ShapedRecipe recipe = new ShapedRecipe(group, CraftingBookCategory.MISC, toPattern(width, height, createInput(true, input)), makeStack(output));
        this.recipeOutput.accept(key, recipe, null);
    }

    /**
     * Stages a {@link ShapelessRecipe} for datagen.
     * 
     * @param key    The resource location of the recipe.
     * @param group  The recipe book group of the recipe.
     * @param output A {@linkplain #makeStack(Object) stack-like} output object.
     * @param input  A row-major vararg array of {@linkplain #createInput(boolean, Object...) input-like} objects. Empty inputs are not permitted.
     */
    public void addShapeless(ResourceLocation key, String group, Object output, Object... inputs) {
        ShapelessRecipe recipe = new ShapelessRecipe(group, CraftingBookCategory.MISC, makeStack(output), createInput(false, inputs));
        this.recipeOutput.accept(key, recipe, null);
    }

    /**
     * Stages a {@link ShapedRecipe} for datagen using the {@link #modid} as the group.
     * 
     * @see #addShaped(ResourceLocation, String, Object, int, int, Object...)
     */
    public void addShaped(ResourceLocation key, Object output, int width, int height, Object... input) {
        this.addShaped(key, this.modid, output, width, height, input);
    }

    /**
     * Stages a {@link ShapelessRecipe} for datagen using the {@link #modid} as the group.
     * 
     * @see #addShapeless(ResourceLocation, String, Object, Object...)
     */
    public void addShapeless(ResourceLocation key, Object output, Object... inputs) {
        this.addShapeless(key, this.modid, output, inputs);
    }

    /**
     * Stages a {@link ShapedRecipe} for datagen using the {@link #modid} as the group and the key's namespace,
     * while automatically determining a path from the output item.
     * 
     * @see #addShaped(ResourceLocation, String, Object, int, int, Object...)
     */
    public void addShaped(Object output, int width, int height, Object... input) {
        ItemStack out = makeStack(output);
        String path = this.resolvePath(out);
        this.addShaped(ResourceLocation.fromNamespaceAndPath(this.modid, path), this.modid, out, width, height, input);
    }

    /**
     * Stages a {@link ShapelessRecipe} for datagen using the {@link #modid} as the group and the key's namespace,
     * while automatically determining a path from the output item.
     * 
     * @see #addShapeless(ResourceLocation, String, Object, Object...)
     */
    public void addShapeless(Object output, Object... inputs) {
        ItemStack out = makeStack(output);
        String path = this.resolvePath(out);
        this.addShapeless(ResourceLocation.fromNamespaceAndPath(this.modid, path), this.modid, out, inputs);
    }

    /**
     * Creates an {@link Ingredient} matching a potion item with the given potion type.
     */
    public static Ingredient potionIngredient(Holder<Potion> type) {
        HolderSet<Item> items = HolderSet.direct(BuiltInRegistries.ITEM.wrapAsHolder(Items.POTION));
        DataComponentPredicate predicate = DataComponentPredicate.builder().expect(DataComponents.POTION_CONTENTS, new PotionContents(type)).build();
        return new Ingredient(new DataComponentIngredient(items, predicate, false));
    }

    @Override
    protected final void buildRecipes(RecipeOutput recipeOutput, HolderLookup.Provider registries) {
        this.recipeOutput = recipeOutput;
        this.genRecipes(recipeOutput, registries);
    }

    @Override
    protected final void buildRecipes(RecipeOutput recipeOutput) {}

    /**
     * Resolves a potential path for the given output object. Avoids duplicates by appending underscores.
     */
    private String resolvePath(ItemStack output) {
        String path = BuiltInRegistries.ITEM.getKey(output.getItem()).getPath();
        while (this.usedPaths.contains(path)) {
            path += "_";
        }
        this.usedPaths.add(path);
        return path;
    }

    /**
     * Transforms an object that could be converted into an {@link ItemStack} into one.
     * 
     * @param thing A potential candidate object. One of {@link ItemStack}, {@link ItemLike}, or a {@link Holder} containing an {@link ItemLike}.
     * @throws IllegalArgumentException if the type of object is unknown
     */
    private static ItemStack makeStack(Object thing) {
        if (thing instanceof ItemStack stack) return stack;
        if (thing instanceof ItemLike il) return new ItemStack(il);
        if (thing instanceof Holder<?> h) return new ItemStack((ItemLike) h.value());
        throw new IllegalArgumentException("Attempted to create an ItemStack from something that cannot be converted: " + thing);
    }

    /**
     * Converts an array of "input-like" objects into a list of {@link Ingredient}s.
     * <p>
     * The created {@link Ingredient} depends on the type of the object:
     * <ul>
     * <li>A {@link TagKey} will be converted to a tag ingredient.</li>
     * <li>A {@link String} will be parsed into a {@link ResourceLocation}, and treated as a {@link TagKey}.</li>
     * <li>An {@link ItemStack} will be converted into a single-stack ingredient.</li>
     * <li>An {@link ItemLike} or {@link Holder} will be passed to {@link #makeStack(Object)} and treated as an {@link ItemStack}.</li>
     * <li>An {@link Ingredient} will be casted and used directly.</li>
     * </ul>
     * If empty inputs are allowed, then {@code null}, {@link ItemStack#EMPTY}, or {@link Ingredient#EMPTY} will be converted to {@link Ingredient#EMPTY}.
     * 
     * @param allowEmpty If empty input values are allowed.
     * @param inputArr   An array of objects to translate into ingredients.
     * @return A list of ingredients resulting from the conversion.
     * @throws UnsupportedOperationException if the object cannot be converted.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static NonNullList<Ingredient> createInput(boolean allowEmpty, Object... inputArr) {
        NonNullList<Ingredient> inputL = NonNullList.create();
        for (int i = 0; i < inputArr.length; i++) {
            Object input = inputArr[i];
            if (input instanceof TagKey tag) inputL.add(i, Ingredient.of(tag));
            else if (input instanceof String str) inputL.add(i, Ingredient.of(ItemTags.create(ResourceLocation.parse(str))));
            else if (input instanceof ItemStack stack && !stack.isEmpty()) inputL.add(i, Ingredient.of(stack));
            else if (input instanceof ItemLike || input instanceof Holder) inputL.add(i, Ingredient.of(makeStack(input)));
            else if (input instanceof Ingredient ing && !ing.isEmpty()) inputL.add(i, ing);
            else if (allowEmpty && (input == null || input == ItemStack.EMPTY || input == Ingredient.EMPTY)) inputL.add(i, Ingredient.EMPTY);
            else throw new UnsupportedOperationException("Attempted to add invalid recipe. Input " + input + " not allowed.");
        }
        return inputL;
    }

    /**
     * Automatically determines a {@link ShapedRecipePattern} from a list of shaped recipe inputs.
     * <p>
     * Uses the first available character from the first valid item in each ingredient to form the key.
     */
    private static ShapedRecipePattern toPattern(int width, int height, NonNullList<Ingredient> input) {
        Map<Character, Ingredient> key = new HashMap<>();
        Map<Ingredient, Character> chars = new HashMap<>();
        List<String> rows = new ArrayList<>(height);
        for (int h = 0; h < height; h++) {
            String row = "";
            for (int w = 0; w < width; w++) {
                Ingredient ing = input.get(h * width + w);
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
    private static Character getFirstChar(Collection<Character> inUse, Ingredient ing) {
        String path;
        if (ing == Ingredient.EMPTY) {
            return ' ';
        }
        else if (ing.isCustom()) {
            ICustomIngredient custom = ing.getCustomIngredient();
            Item item = custom.getItems().findFirst().map(ItemStack::getItem).orElse(Items.AIR);
            path = BuiltInRegistries.ITEM.getKey(item).getPath();
        }
        else {
            Value v = ing.getValues()[0];
            if (v instanceof TagValue t) {
                path = t.tag().location().getPath();
            }
            else if (v instanceof ItemValue i) {
                path = BuiltInRegistries.ITEM.getKey(i.item().getItem()).getPath();
            }
            else {
                throw new UnsupportedOperationException("Unknown Ingredient$Value type: " + v.getClass().getCanonicalName());
            }
        }
        path = path.toUpperCase(Locale.ROOT);
        for (char c : path.toCharArray()) {
            if (!inUse.contains(c)) return c;
        }
        throw new UnsupportedOperationException("Failed to find any unused characters for ingredient: " + ing);
    }
}
