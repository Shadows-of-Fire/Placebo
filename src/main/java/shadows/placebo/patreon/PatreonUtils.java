package shadows.placebo.patreon;

import java.util.function.Supplier;

import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;

public class PatreonUtils {

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

		public final Supplier<IParticleData> type;

		PatreonParticleType(Supplier<IParticleData> type) {
			this.type = type;
		}
	}

}
