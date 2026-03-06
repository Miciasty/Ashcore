package nsk.nu.ashcore.api.math;

/**
 * Immutable 4D vector with double-precision components.
 *
 * <p>Useful for matrix-4 transforms, homogeneous coordinates, and generic 4D math.</p>
 */
public record Vector4(double x, double y, double z, double w) {

    /** Constant zero vector (0, 0, 0, 0). */
    public static final Vector4 ZERO = new Vector4(0.0, 0.0, 0.0, 0.0);

    /**
     * Factory method.
     *
     * @param x x component
     * @param y y component
     * @param z z component
     * @param w w component
     * @return new vector
     */
    public static Vector4 of(double x, double y, double z, double w) {
        return new Vector4(x, y, z, w);
    }

    /**
     * Creates a 4D vector from a 3D vector and explicit w component.
     *
     * @param v xyz source
     * @param w w component
     * @return new vector (v.x, v.y, v.z, w)
     */
    public static Vector4 fromVector3(Vector3 v, double w) {
        return new Vector4(v.x(), v.y(), v.z(), w);
    }

    /**
     * Returns the xyz part as Vector3.
     *
     * @return vector (x, y, z)
     */
    public Vector3 xyz() {
        return new Vector3(x, y, z);
    }

    /**
     * Component-wise addition.
     *
     * @param other vector to add
     * @return this + other
     */
    public Vector4 add(Vector4 other) {
        return new Vector4(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    /**
     * Component-wise subtraction.
     *
     * @param other vector to subtract
     * @return this - other
     */
    public Vector4 sub(Vector4 other) {
        return new Vector4(x - other.x, y - other.y, z - other.z, w - other.w);
    }

    /**
     * Scalar multiplication.
     *
     * @param scalar multiplier
     * @return this * scalar
     */
    public Vector4 mul(double scalar) {
        return new Vector4(x * scalar, y * scalar, z * scalar, w * scalar);
    }

    /**
     * Scalar division.
     *
     * @param scalar divisor
     * @return this / scalar
     * @throws ArithmeticException if scalar is zero
     */
    public Vector4 div(double scalar) {
        if (scalar == 0.0) throw new ArithmeticException("Division by zero");
        return new Vector4(x / scalar, y / scalar, z / scalar, w / scalar);
    }

    /**
     * Dot product.
     *
     * @param other second operand
     * @return x*ox + y*oy + z*oz + w*ow
     */
    public double dot(Vector4 other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    /**
     * Squared Euclidean length.
     *
     * @return x^2 + y^2 + z^2 + w^2
     */
    public double lengthSq() {
        return dot(this);
    }

    /**
     * Euclidean length.
     *
     * @return sqrt(lengthSq())
     */
    public double length() {
        return Math.sqrt(lengthSq());
    }

    /**
     * Returns a unit-length vector in the same direction.
     *
     * <p>If this vector is zero-length, returns {@code this} unchanged.</p>
     *
     * @return normalized vector or this if length is zero
     */
    public Vector4 normalized() {
        double len = length();
        return len == 0.0 ? this : div(len);
    }

    /**
     * Linear interpolation between this vector and {@code to}.
     *
     * <p>Parameter {@code t} is not clamped.</p>
     *
     * @param to end vector
     * @param t interpolation factor
     * @return this + (to - this) * t
     */
    public Vector4 lerp(Vector4 to, double t) {
        return new Vector4(
                x + (to.x - x) * t,
                y + (to.y - y) * t,
                z + (to.z - z) * t,
                w + (to.w - w) * t
        );
    }

    /**
     * Component-wise minimum.
     *
     * @param other second operand
     * @return min(this, other) per component
     */
    public Vector4 min(Vector4 other) {
        return new Vector4(
                Math.min(x, other.x),
                Math.min(y, other.y),
                Math.min(z, other.z),
                Math.min(w, other.w)
        );
    }

    /**
     * Component-wise maximum.
     *
     * @param other second operand
     * @return max(this, other) per component
     */
    public Vector4 max(Vector4 other) {
        return new Vector4(
                Math.max(x, other.x),
                Math.max(y, other.y),
                Math.max(z, other.z),
                Math.max(w, other.w)
        );
    }

    /**
     * Returns a copy with replaced x component.
     *
     * @param nx new x value
     * @return copied vector with new x
     */
    public Vector4 withX(double nx) {
        return new Vector4(nx, y, z, w);
    }

    /**
     * Returns a copy with replaced y component.
     *
     * @param ny new y value
     * @return copied vector with new y
     */
    public Vector4 withY(double ny) {
        return new Vector4(x, ny, z, w);
    }

    /**
     * Returns a copy with replaced z component.
     *
     * @param nz new z value
     * @return copied vector with new z
     */
    public Vector4 withZ(double nz) {
        return new Vector4(x, y, nz, w);
    }

    /**
     * Returns a copy with replaced w component.
     *
     * @param nw new w value
     * @return copied vector with new w
     */
    public Vector4 withW(double nw) {
        return new Vector4(x, y, z, nw);
    }
}