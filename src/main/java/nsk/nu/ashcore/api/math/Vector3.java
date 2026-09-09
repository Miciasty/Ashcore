package nsk.nu.ashcore.api.math;

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
    /** @return Euclidean length; infinity if the magnitude exceeds the double range */
    public double length() { return Math.hypot(Math.hypot(x, y), z); }
    /**
     * @return unit vector within floating-point rounding; returns {@code this} for the zero vector
     * @throws IllegalArgumentException if any component is not finite
     */
    public Vector3 normalized() {
        double scale = Math.max(Math.abs(x), Math.max(Math.abs(y), Math.abs(z)));
        if (!Double.isFinite(scale)) throw new IllegalArgumentException("Vector must be finite");
        if (scale == 0) return this;
        double sx = x / scale, sy = y / scale, sz = z / scale;
        double len = Math.sqrt(sx*sx + sy*sy + sz*sz);
        return new Vector3(sx / len, sy / len, sz / len);
    }
}
