package dev.shadowsoffire.placebo.cap;

import net.neoforged.neoforge.energy.EnergyStorage;

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

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setTransferRate(int transfer) {
        this.maxExtract = this.maxReceive = transfer;
    }

    public void setMaxExtract(int extract) {
        this.maxExtract = extract;
    }

    public void setMaxReceive(int receive) {
        this.maxReceive = receive;
    }

}
