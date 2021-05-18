package shadows.placebo.util;

import net.minecraft.util.math.vector.Vector3d;

/**
 * Helper class for creating a point to point line in 3d space.
 */
public class LinearEquation {

	protected Vector3d src;
	protected Vector3d dest;
	protected Vector3d vec;

	public LinearEquation(Vector3d src, Vector3d dest) {
		this.src = src;
		this.dest = dest;
		this.vec = src.subtract(dest);
	}

	public Vector3d step(double step) {
		return new Vector3d(this.dest.getX() + step * this.vec.getX(), this.dest.getY() + step * this.vec.getY(), this.dest.getZ() + step * this.vec.getZ());
	}

	public Vector3d getSrc() {
		return this.src;
	}

	public Vector3d getDest() {
		return this.dest;
	}

}