package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.math.Vector3;

/**
 * Immutable plane n·p + d = 0. Normal and offset are divided by the original normal's length.
 * The resulting normal is unit-length within rounding; d and distances use the point's units.
 * A zero/non-finite normal, non-finite d or unrepresentable normalized d throws IllegalArgumentException.
 */
public record Plane(Vector3 normal, double d) {
    public Plane {
        double scale = Math.max(Math.abs(normal.x()), Math.max(Math.abs(normal.y()), Math.abs(normal.z())));
        if (scale == 0.0 || !Double.isFinite(scale)) throw new IllegalArgumentException("Normal must be finite and non-zero");
        double sx = normal.x() / scale, sy = normal.y() / scale, sz = normal.z() / scale;
        double len = Math.sqrt(sx*sx + sy*sy + sz*sz);
        normal = new Vector3(sx / len, sy / len, sz / len);
        double offset = d / scale;
        d = Double.isFinite(offset) ? offset / len : (d / len) / scale;
        if (!Double.isFinite(d)) throw new IllegalArgumentException("Normalized offset must be finite");
    }
    /** Signed distance from point to plane (positive in direction of the normal). */
    public double distanceTo(Vector3 p) { return normal.dot(p) + d; }
    /** Projects point onto the plane. */
    public Vector3 project(Vector3 p) { return p.sub(normal.mul(distanceTo(p))); }
}
