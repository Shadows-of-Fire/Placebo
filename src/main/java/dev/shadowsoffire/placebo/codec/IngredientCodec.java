package dev.shadowsoffire.placebo.codec;

import java.util.Arrays;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.placebo.json.ItemAdapter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

/**
 * One-Way Ingredient codec. When initialliy deserializing, supports a mix of tag keys and item registry names.<br>
 * When serialzing, it will only serialize the actual item list, which will not include the original tag key.
 */
public class IngredientCodec implements Codec<Ingredient> {

    public static IngredientCodec INSTANCE = new IngredientCodec();

    private static Codec<List<ItemStack>> ITEM_LIST_CODEC = ItemAdapter.CODEC.listOf();

    @Override
    public <T> DataResult<T> encode(Ingredient input, DynamicOps<T> ops, T prefix) {
        return ITEM_LIST_CODEC.encode(Arrays.asList(input.getItems()), ops, prefix);
    }

    @Override
    public <T> DataResult<Pair<Ingredient, T>> decode(DynamicOps<T> ops, T input) {
        JsonElement json = input instanceof JsonElement j ? j : ops.convertTo(JsonOps.INSTANCE, input);
        try {
            return DataResult.success(Pair.of(CraftingHelper.getIngredient(json, true), input));
        }
        catch (JsonSyntaxException ex) {
            return DataResult.error(ex::getMessage);
        }
    }

}
