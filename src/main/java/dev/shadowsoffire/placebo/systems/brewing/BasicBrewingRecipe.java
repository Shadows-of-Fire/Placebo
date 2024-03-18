package dev.shadowsoffire.placebo.systems.brewing;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.json.ItemAdapter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

public class BasicBrewingRecipe extends BrewingRecipe implements JsonBrewingRecipe {

    public static final Codec<BasicBrewingRecipe> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(BrewingRecipe::getInput),
            Ingredient.CODEC_NONEMPTY.fieldOf("reagent").forGetter(BrewingRecipe::getIngredient),
            ItemAdapter.CODEC.fieldOf("output").forGetter(BrewingRecipe::getOutput))
        .apply(inst, BasicBrewingRecipe::new));

    public BasicBrewingRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
        super(input, ingredient, output);
    }

    @Override
    public Codec<? extends JsonBrewingRecipe> getCodec() {
        return CODEC;
    }

}
