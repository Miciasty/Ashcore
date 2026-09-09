package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.math.Vector3;

/**
 * Semi-infinite ray defined by origin and normalized direction: P(t) = origin + direction * t, t >= 0.
 * The constructor requires finite components and guarantees a non-zero unit direction within rounding error.
 * Parameter t measures distance in the same units as the origin. Invalid components or a zero direction
 * throw IllegalArgumentException; null vectors throw NullPointerException.
 */
public record Ray(Vector3 origin, Vector3 direction) {

    public Ray {
        if (!Double.isFinite(origin.x()) || !Double.isFinite(origin.y()) || !Double.isFinite(origin.z())) {
            throw new IllegalArgumentException("Origin must be finite");
        }
        if (direction.x() == 0 && direction.y() == 0 && direction.z() == 0) {
            throw new IllegalArgumentException("Direction must be non-zero.");
        }
        direction = direction.normalized();
    }

    /**
     * Evaluates origin + direction * t without clamping or validation; negative t extends behind the origin.
     * Use finite t and representable intermediate results. Non-finite arithmetic follows Java double rules.
     * @return point at distance t along the supporting line
     */
    public Vector3 at(double t) { return origin.add(direction.mul(t)); }
}
