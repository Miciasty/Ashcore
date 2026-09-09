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
    /** Divides each component by s; zero s throws ArithmeticException. Other arithmetic follows double rules. */
    public Vector3 div(double s) {
        if (s == 0.0) throw new ArithmeticException("Division by zero");
        return new Vector3(x / s, y / s, z / s);
    }
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
    /** @return squared Euclidean length; may overflow or underflow even when length() does not */
    public double lengthSq() { return dot(this); }
    /** @return Euclidean distance in the components' common units; infinity if not representable */
    public double distance(Vector3 other) { return Math.hypot(Math.hypot(x - other.x, y - other.y), z - other.z); }
    /** @return squared Euclidean distance; intermediate differences and squares follow double rules */
    public double distanceSq(Vector3 other) {
        double dx = x - other.x, dy = y - other.y, dz = z - other.z;
        return dx*dx + dy*dy + dz*dz;
    }
    /** Component-wise interpolation this + (to - this) * t; t is dimensionless and is not clamped. */
    public Vector3 lerp(Vector3 to, double t) {
        return new Vector3(x + (to.x - x)*t, y + (to.y - y)*t, z + (to.z - z)*t);
    }
    /** @return component-wise minimum, propagating NaN as Math.min does */
    public Vector3 min(Vector3 other) { return new Vector3(Math.min(x, other.x), Math.min(y, other.y), Math.min(z, other.z)); }
    /** @return component-wise maximum, propagating NaN as Math.max does */
    public Vector3 max(Vector3 other) { return new Vector3(Math.max(x, other.x), Math.max(y, other.y), Math.max(z, other.z)); }
    /** @return a copy with the given x component */
    public Vector3 withX(double nx) { return new Vector3(nx, y, z); }
    /** @return a copy with the given y component */
    public Vector3 withY(double ny) { return new Vector3(x, ny, z); }
    /** @return a copy with the given z component */
    public Vector3 withZ(double nz) { return new Vector3(x, y, nz); }
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
