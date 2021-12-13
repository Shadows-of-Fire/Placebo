package shadows.placebo.cap;

import net.minecraftforge.energy.EnergyStorage;

public class ModifiableEnergyStorage extends EnergyStorage {

	public ModifiableEnergyStorage(int capacity) {
		this(capacity, capacity, capacity, 0);
	}

	public ModifiableEnergyStorage(int capacity, int maxTransfer) {
		this(capacity, maxTransfer, maxTransfer, 0);
	}

	public ModifiableEnergyStorage(int capacity, int maxReceive, int maxExtract) {
		this(capacity, maxReceive, maxExtract, 0);
	}

	public ModifiableEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
		super(capacity, maxReceive, maxExtract, energy);
	}

	public void setEnergy(int energy) {
		this.energy = energy;
	}

}
