package shadows.placebo.patreon;

import java.util.function.Function;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import com.google.common.base.Suppliers;

import net.minecraft.client.KeyMapping;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import shadows.placebo.Placebo;
import shadows.placebo.patreon.wings.IWingModel;
import shadows.placebo.patreon.wings.Wing;

public class PatreonUtils {

	public static enum PatreonParticleType {
		ASH(() -> ParticleTypes.ASH),
		CAMPFIRE_SMOKE(() -> ParticleTypes.CAMPFIRE_COSY_SMOKE),
		CLOUD(() -> ParticleTypes.CLOUD),
		DMG_HEART(() -> ParticleTypes.DAMAGE_INDICATOR),
		DRAGON_BREATH(() -> ParticleTypes.DRAGON_BREATH),
		ELECTRIC_SPARK(() -> ParticleTypes.ELECTRIC_SPARK),
		END_ROD(() -> ParticleTypes.END_ROD),
		FIRE(() -> ParticleTypes.FLAME),
		FIREWORK(() -> ParticleTypes.FIREWORK),
		GLOW(() -> ParticleTypes.GLOW),
		GROWTH(() -> ParticleTypes.HAPPY_VILLAGER),
		HEART(() -> ParticleTypes.HEART),
		SCULK_SOUL(() -> ParticleTypes.SCULK_SOUL),
		SLIME(() -> ParticleTypes.ITEM_SLIME),
		SNOW(() -> ParticleTypes.ITEM_SNOWBALL),
		SOUL(() -> ParticleTypes.SOUL),
		SOUL_FIRE(() -> ParticleTypes.SOUL_FIRE_FLAME),
		WITCH(() -> ParticleTypes.WITCH);

		public final Supplier<ParticleOptions> type;

		PatreonParticleType(Supplier<ParticleOptions> type) {
			this.type = type;
		}
	}

	private static Function<Player, ResourceLocation> wingTex(String name) {
		var supp = Suppliers.memoize(() -> new ResourceLocation(Placebo.MODID, "textures/wings/" + name + ".png"));
		return player -> {
			return supp.get();
		};
	}

	public static enum WingType {
		ANGEL(() -> Wing.INSTANCE, wingTex("angel"), -0.5),
		ARMORED(() -> Wing.INSTANCE, wingTex("armored"), -0.7),
		BAT(() -> Wing.INSTANCE, wingTex("bat"), -0.7),
		BLAZE(() -> Wing.INSTANCE, wingTex("blaze"), -0.73),
		BONE(() -> Wing.INSTANCE, wingTex("bone"), -1),
		CLOUD(() -> Wing.INSTANCE, wingTex("cloud"), -1),
		DEMON(() -> Wing.INSTANCE, wingTex("demon"), -0.55),
		FAIRY(() -> Wing.INSTANCE, wingTex("fairy"), -0.85),
		FLY(() -> Wing.INSTANCE, wingTex("fly"), -0.58, 6),
		MECHANICAL(() -> Wing.INSTANCE, wingTex("mechanical"), -0.75),
		MONARCH(() -> Wing.INSTANCE, wingTex("monarch"), -0.85),
		PIXIE(() -> Wing.INSTANCE, wingTex("pixie"), -0.65),
		SPOOKY(() -> Wing.INSTANCE, wingTex("spooky"), -0.65);

		public final Supplier<IWingModel> model;
		public final Function<Player, ResourceLocation> textureGetter;
		public final double yOffset;
		public final double flapSpeed;

		WingType(Supplier<IWingModel> model, Function<Player, ResourceLocation> textureGetter, double yOffset) {
			this(model, textureGetter, yOffset, 1);
		}

		WingType(Supplier<IWingModel> model, Function<Player, ResourceLocation> textureGetter, double yOffset, double flapSpeed) {
			this.model = model;
			this.textureGetter = textureGetter;
			this.yOffset = yOffset;
			this.flapSpeed = flapSpeed;
		}
	}

}
