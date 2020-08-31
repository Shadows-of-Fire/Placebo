package shadows.placebo.util;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class BiomeUtil {

	/**
	 * Adds a feature to the given biome.
	 * @param b The biome to add the feature to.
	 * @param stage The generation stage that the feature will generate at.
	 * @param f The feature itself.
	 */
	public static void addFeature(Biome b, GenerationStage.Decoration stage, ConfiguredFeature<?, ?> f) {
		BiomeGenerationSettings genSettings = b.func_242440_e();
		genSettings.field_242484_f = PlaceboUtil.toMutable(genSettings.field_242484_f);
		genSettings.field_242484_f.set(stage.ordinal(), PlaceboUtil.toMutable(genSettings.field_242484_f.get(stage.ordinal())));
		genSettings.field_242484_f.get(stage.ordinal()).add(() -> f);
	}

}
