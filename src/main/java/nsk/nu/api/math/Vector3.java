package nsk.nu.api.math;

/**
 * Immutable 3D vector for positions and directions.
 * Provides the minimal, predictable set of operations commonly used in gameplay math.
 */
public record Vector3(double x, double y, double z) {
    public static final Vector3 ZERO = new Vector3(0, 0, 0);

    /** @return component-wise sum (this + other) */
    public Vector3 add(Vector3 other) { return new Vector3(x + other.x, y + other.y, z + other.z); }
    /** @return component-wise difference (this - other) */
    public Vector3 sub(Vector3 other) { return new Vector3(x - other.x, y - other.y, z - other.z); }
    /** @return scalar multiplication */
    public Vector3 mul(double s) { return new Vector3(x * s, y * s, z * s); }
    /** @return dot product */
    public double dot(Vector3 other) { return x * other.x + y * other.y + z * other.z; }
    /** @return cross product */
    public Vector3 cross(Vector3 other) {
        return new Vector3(
                y * other.z - z * other.y,
                z * other.x - x * other.z,
                x * other.y - y * other.x
        );
    }
    /** @return Euclidean length (magnitude) */
    public double length() { return Math.sqrt(dot(this)); }
    /**
     * @return normalized vector; returns {@code this} if the length is zero (no allocations on zero).
     */
    public Vector3 normalized() {
        double len = length();
        return len == 0 ? this : mul(1.0 / len);
    }
}