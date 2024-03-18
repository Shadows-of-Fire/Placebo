package dev.shadowsoffire.placebo.systems.brewing;

import java.util.List;

import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.brewing.IBrewingRecipe;

public class BrewingRecipeRegistry extends DynamicRegistry<JsonBrewingRecipe> {

    public static final BrewingRecipeRegistry INSTANCE = new BrewingRecipeRegistry();

    public BrewingRecipeRegistry() {
        super(Placebo.LOGGER, "brewing_recipes", true, true);
    }

    @Override
    protected void registerBuiltinCodecs() {
        this.registerDefaultCodec(Placebo.loc("brewing"), BasicBrewingRecipe.CODEC);
    }

    @Override
    protected void beginReload() {
        getForgeBrewingRecipes().removeAll(this.getValues());
        super.beginReload();
    }

    @Override
    protected void onReload() {
        super.onReload();
        getForgeBrewingRecipes().addAll(this.getValues());
    }

    private static List<IBrewingRecipe> getForgeBrewingRecipes() {
        return ObfuscationReflectionHelper.getPrivateValue(net.neoforged.neoforge.common.brewing.BrewingRecipeRegistry.class, null, "recipes");
    }

}
