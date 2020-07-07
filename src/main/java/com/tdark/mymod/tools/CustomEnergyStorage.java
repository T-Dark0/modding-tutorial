package com.tdark.mymod.tools;

import net.minecraftforge.energy.EnergyStorage;

public class CustomEnergyStorage extends EnergyStorage {
    public CustomEnergyStorage(int capacity) {
        super(capacity);
    }

    public CustomEnergyStorage(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    /**
     * If {@code 0 <= energy <= maximumCapacity}, sets this storage's energy to {@code energy}. If {@code energy < 0}, sets it to 0,
     * otherwise sets it to the maximum capacity
     * @param energy the energy to set
     * @return the value that was set, in the range {@code [0, maximumCapacity]}
     */
    public int setEnergy(int energy) {
        int newEnergy = this.energy = Math.max(0, Math.min(energy, capacity));
        onContentsChanged();
        return newEnergy;
    }

    /**
     * Shortcut to call {@link #receiveEnergy(int, boolean)} with {@code false} as its second parameter
     * @param energy the energy to receive
     * @return the energy that was received, in the range {@code [0, energy]}
     */
    public int receiveEnergy(int energy) {
        return receiveEnergy(energy, false);
    }

    /**
     * Shortcut to call {@link #extractEnergy(int, boolean)} with {@code false} as its second parameter
     * @param energy the energy to extract
     * @return the energy that was extracted, in the range {@code [energy, maximumCapacity - energy]}
     */
    public int extractEnergy(int energy) {
        return extractEnergy(energy, false);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        onContentsChanged();
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        onContentsChanged();
        return extracted;
    }

    /**
     * Called whenever the energy contained in this storage is changed. The default implementation is a no-op, meant for
     * overriding
     */
    public void onContentsChanged() {
        //for overriding
    }
}
