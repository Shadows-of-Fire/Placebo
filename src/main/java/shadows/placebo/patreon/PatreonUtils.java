package shadows.placebo.patreon;

import java.util.function.Function;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import shadows.placebo.Placebo;
import shadows.placebo.patreon.wings.IWingModel;
import shadows.placebo.patreon.wings.Wing;

public class PatreonUtils {

	public static final KeyMapping TOGGLE_T = new KeyMapping("placebo.toggleTrails", GLFW.GLFW_KEY_KP_8, "key.categories.placebo");
	public static final KeyMapping TOGGLE_W = new KeyMapping("placebo.toggleWings", GLFW.GLFW_KEY_KP_9, "key.categories.placebo");

	public static enum PatreonParticleType {
		SOUL_FIRE(() -> ParticleTypes.SOUL_FIRE_FLAME),
		FIRE(() -> ParticleTypes.FLAME),
		CAMPFIRE_SMOKE(() -> ParticleTypes.CAMPFIRE_COSY_SMOKE),
		CLOUD(() -> ParticleTypes.CLOUD),
		GROWTH(() -> ParticleTypes.HAPPY_VILLAGER),
		DMG_HEART(() -> ParticleTypes.DAMAGE_INDICATOR),
		HEART(() -> ParticleTypes.HEART),
		DRAGON_BREATH(() -> ParticleTypes.DRAGON_BREATH),
		END_ROD(() -> ParticleTypes.END_ROD),
		FIREWORK(() -> ParticleTypes.FIREWORK),
		SLIME(() -> ParticleTypes.ITEM_SLIME),
		SNOW(() -> ParticleTypes.ITEM_SNOWBALL),
		SOUL(() -> ParticleTypes.SOUL),
		WITCH(() -> ParticleTypes.WITCH);

		public final Supplier<ParticleOptions> type;

		PatreonParticleType(Supplier<ParticleOptions> type) {
			this.type = type;
		}
	}

	public static final ResourceLocation ANGEL_TEX = new ResourceLocation(Placebo.MODID, "textures/wings/angel.png");
	public static final ResourceLocation BAT_TEX = new ResourceLocation(Placebo.MODID, "textures/wings/bat.png");
	public static final ResourceLocation DEMON_TEX = new ResourceLocation(Placebo.MODID, "textures/wings/demon.png");
	public static final ResourceLocation FLY_TEX = new ResourceLocation(Placebo.MODID, "textures/wings/fly.png");
	public static final ResourceLocation PIXIE_TEX = new ResourceLocation(Placebo.MODID, "textures/wings/pixie.png");
	public static final ResourceLocation SPOOKY_TEX = new ResourceLocation(Placebo.MODID, "textures/wings/spooky.png");

	public static enum WingType {
		ANGEL(() -> Wing.INSTANCE, p -> ANGEL_TEX, -0.5),
		BAT(() -> Wing.INSTANCE, p -> BAT_TEX, -0.7),
		DEMON(() -> Wing.INSTANCE, p -> DEMON_TEX, -0.55),
		FLY(() -> Wing.INSTANCE, p -> FLY_TEX, -0.58),
		PIXIE(() -> Wing.INSTANCE, p -> PIXIE_TEX, -0.65),
		SPOOKY(() -> Wing.INSTANCE, p -> SPOOKY_TEX, -0.65),;

		public final Supplier<IWingModel> model;
		public final Function<Player, ResourceLocation> textureGetter;
		public final double yOffset;

		WingType(Supplier<IWingModel> model, Function<Player, ResourceLocation> textureGetter, double yOffset) {
			this.model = model;
			this.textureGetter = textureGetter;
			this.yOffset = yOffset;
		}
	}

}
