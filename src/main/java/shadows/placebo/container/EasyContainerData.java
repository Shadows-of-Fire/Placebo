package shadows.placebo.container;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import org.apache.commons.lang3.tuple.Pair;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.world.inventory.ContainerData;
import shadows.placebo.cap.ModifiableEnergyStorage;

/**
 * Simple ContainerData implementation that allows for lambda registration.
 * The other option is creation of anonymous classes.
 */
public class EasyContainerData implements ContainerData {

	protected List<Pair<IntSupplier, IntConsumer>> data = new ArrayList<>();

	@Override
	public int get(int pIndex) {
		return data.get(pIndex).getLeft().getAsInt();
	}

	@Override
	public void set(int pIndex, int pValue) {
		data.get(pIndex).getRight().accept(pValue);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	public void addData(IntSupplier getter, IntConsumer setter) {
		data.add(Pair.of(getter, setter));
	}

	public void addData(BooleanSupplier getter, BooleanConsumer setter) {
		addData(() -> getter.getAsBoolean() ? 1 : 0, v -> setter.accept(v == 1));
	}

	/**
	 * Registers an energy storage for tracking.  Note that an energy storage uses two slots!
	 */
	public void addEnergy(ModifiableEnergyStorage energy) {
		addData(() -> ContainerUtil.getSerializedEnergy(energy, false), v -> ContainerUtil.deserializeEnergy(energy, v, false));
		addData(() -> ContainerUtil.getSerializedEnergy(energy, true), v -> ContainerUtil.deserializeEnergy(energy, v, true));
	}

	public interface IDataAutoRegister {
		public ContainerData getData();
	}
}
