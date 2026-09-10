package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.math.Vector3;

/**
 * Geometric contact of ordered shapes A and B, in their common coordinate system.
 * A miss has depth -infinity and null vectors. Otherwise depth is finite and non-negative in position units:
 * zero means touching, positive means penetration, including containment of a zero-radius point.
 * The dimensionless unit normal points from A toward B for external contact; query methods define containment/ties.
 * pointA and pointB lie on the respective surfaces and satisfy pointA - pointB = normal * depth within rounding.
 * They are witnesses, not a unique contact manifold. Moving A by -normal * depth would reach tangency in exact
 * arithmetic for the supported shapes; this record does not instruct a solver or describe forces, ray distance or time.
 * Construction checks finite fields and normal length within absolute 1e-12 of one, or the complete miss sentinel.
 * Invalid values throw IllegalArgumentException; null fields on a contact throw NullPointerException.
 */
public record Contact(double depth, Vector3 normal, Vector3 pointA, Vector3 pointB) {
    public Contact {
        boolean miss = depth == Double.NEGATIVE_INFINITY && normal == null && pointA == null && pointB == null;
        if (!miss) {
            if (!Double.isFinite(depth) || depth < 0) throw new IllegalArgumentException("Depth must be finite and non-negative");
            requireFinite(normal);
            requireFinite(pointA);
            requireFinite(pointB);
            if (Math.abs(normal.length() - 1) > 1e-12) throw new IllegalArgumentException("Normal must have unit length");
        }
    }

    /** @return true for touching or penetration */
    public boolean hit() { return depth >= 0; }
    /** @return true only for a contact with zero depth */
    public boolean touching() { return depth == 0; }

    static Contact miss() { return new Contact(Double.NEGATIVE_INFINITY, null, null, null); }

    private static void requireFinite(Vector3 v) {
        if (!Double.isFinite(v.x()) || !Double.isFinite(v.y()) || !Double.isFinite(v.z())) {
            throw new IllegalArgumentException("Contact vectors must be finite");
        }
    }
}
