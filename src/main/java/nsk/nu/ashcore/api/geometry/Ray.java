package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.math.Vector3;

/**
 * Semi-infinite ray defined by origin and normalized direction: P(t) = origin + direction * t, t >= 0.
 * The constructor guarantees a non-zero, normalized direction.
 */
public record Ray(Vector3 origin, Vector3 direction) {

    public Ray {
        if (direction.length() == 0) {
            throw new IllegalArgumentException("Direction must be non-zero.");
        }
        direction = direction.normalized();
    }

    /** @return point at parameter t along the ray */
    public Vector3 at(double t) { return origin.add(direction.mul(t)); }
}