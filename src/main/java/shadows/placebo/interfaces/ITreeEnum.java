package shadows.placebo.interfaces;

import net.minecraft.world.gen.feature.WorldGenerator;

public interface ITreeEnum extends IPropertyEnum {

	public WorldGenerator getTreeGen();

	public void setTreeGen(WorldGenerator k);

	@Override
	default public int getPredicateIndex() {
		return ((Enum<?>) this).ordinal() / 4;
	}

	@Override
	default public int getMetadata() {
		return ((Enum<?>) this).ordinal() % 4;
	}

}
