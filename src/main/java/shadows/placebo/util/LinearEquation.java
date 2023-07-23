package shadows.placebo.util;

import net.minecraft.world.phys.Vec3;

/**
 * Helper class for creating a point to point line in 3d space.
 */
public class LinearEquation {

    protected Vec3 src;
    protected Vec3 dest;
    protected Vec3 vec;

    public LinearEquation(Vec3 src, Vec3 dest) {
        this.src = src;
        this.dest = dest;
        this.vec = src.subtract(dest);
    }

    public Vec3 step(double step) {
        return new Vec3(this.dest.x() + step * this.vec.x(), this.dest.y() + step * this.vec.y(), this.dest.z() + step * this.vec.z());
    }

    public Vec3 getSrc() {
        return this.src;
    }

    public Vec3 getDest() {
        return this.dest;
    }

}
