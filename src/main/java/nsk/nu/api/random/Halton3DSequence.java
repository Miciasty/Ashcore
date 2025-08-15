package nsk.nu.api.random;

import nsk.nu.api.math.Vector3;

/**
 * Incremental 3D Halton sequence (default bases 2,3,5).
 * Each {@link #nextUnitCube()} is amortized O(1) by updating only changed base-b digits.
 */
public final class Halton3DSequence {
    private final HaltonSequence x, y, z;

    /** Uses bases 2 (X), 3 (Y), 5 (Z). */
    public Halton3DSequence() { this(2, 3, 5); }

    public Halton3DSequence(int baseX, int baseY, int baseZ) {
        this.x = new HaltonSequence(baseX);
        this.y = new HaltonSequence(baseY);
        this.z = new HaltonSequence(baseZ);
    }

    /** Resets all dimensions. */
    public void reset() { x.reset(); y.reset(); z.reset(); }
    /** Next point in the unit cube [0,1)^3. */
    public Vector3 nextUnitCube() { return new Vector3(x.next(), y.next(), z.next()); }
    /** Next direction uniformly distributed on the unit sphere (surface). */
    public Vector3 nextUnitSphereDirection() {
        Vector3 u = nextUnitCube();
        return LowDiscrepancy.mapToUniformSphere(u.x(), u.y());
    }
    /** Next point uniformly distributed inside the unit sphere (volume). */
    public Vector3 nextUnitSpherePoint() {
        Vector3 u = nextUnitCube();
        return LowDiscrepancy.mapToUniformSphereVolume(u.x(), u.y(), u.z());
    }
    /** Next direction on the hemisphere (y up), uniform. */
    public Vector3 nextHemisphereYUp() {
        Vector3 u = nextUnitCube();
        return LowDiscrepancy.mapToUniformHemisphere(u.x(), u.y());
    }
    /** Next direction inside a cone around +Y with half-angle (radians). */
    public Vector3 nextConeYUp(double halfAngleRad) {
        Vector3 u = nextUnitCube();
        return LowDiscrepancy.mapToUniformCone(u.x(), u.y(), Math.cos(halfAngleRad));
    }
}