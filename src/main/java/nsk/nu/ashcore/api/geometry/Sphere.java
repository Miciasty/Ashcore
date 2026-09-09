package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.math.Vector3;

/**
 * Closed sphere with a finite center and finite non-negative radius in the same position units.
 * Zero radius represents a point. Invalid values throw IllegalArgumentException; a null center throws
 * NullPointerException. Intersection operations are provided by CollisionTests.
 */
public record Sphere(Vector3 center, double radius) {
    public Sphere {
        if (!Double.isFinite(center.x()) || !Double.isFinite(center.y()) || !Double.isFinite(center.z())) {
            throw new IllegalArgumentException("Center must be finite");
        }
        if (radius < 0 || !Double.isFinite(radius)) throw new IllegalArgumentException("Radius must be finite and non-negative");
    }
}
