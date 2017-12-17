package shadows.placebo.interfaces;

import shadows.placebo.util.StackPrimer;

public interface IHarvestableEnum extends IPropertyEnum {

	@Override
	default public int getPredicateIndex() {
		return ((Enum<?>) this).ordinal() / 8;
	}

	@Override
	default public int getMetadata() {
		return ((Enum<?>) this).ordinal() % 8;
	}

	public StackPrimer[] getDrops();

}
