package shadows.placebo.container;

import net.minecraftforge.energy.EnergyStorage;
import shadows.placebo.cap.ModifiableEnergyStorage;

public class ContainerUtil {

	/**
	 * IIntArray can only send shorts, so we need to split the power values in two.
	 * @param energy Energy Value
	 * @param upper If sending the upper bits or not.
	 * @return The appropriate half of the integer.
	 */
	public static int getSerializedEnergy(EnergyStorage energy, boolean upper) {
		return split(energy.getEnergyStored(), upper);
	}

	public static void deserializeEnergy(ModifiableEnergyStorage energy, int value, boolean upper) {
		energy.setEnergy(merge(energy.getEnergyStored(), value, upper));
	}

	/**
	 * IIntArray can only send shorts, so we need to split int values in two.
	 * @param value The int to split
	 * @param upper If sending the upper bits or not.
	 * @return The appropriate half of the integer.
	 */
	public static int split(int value, boolean upper) {
		return upper ? value >> 16 : value & 0xFFFF;
	}

	/**
	 * IIntArray can only send shorts, so we need to split int values in two.
	 * @param current The current value
	 * @param value The split integer, recieved from network
	 * @param upper If receiving the upper bits or not.
	 * @return The updated value.
	 */
	public static int merge(int current, int value, boolean upper) {
		if (upper) {
			return current & 0x0000FFFF | value << 16;
		} else {
			return current & 0xFFFF0000 | value;
		}
	}

}
