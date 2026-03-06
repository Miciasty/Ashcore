package nsk.nu.ashcore.api.math;

/**
 * Immutable 2D vector with double-precision components.
 *
 * <p>Use this type for continuous 2D math (positions, directions, offsets, UV-style operations).
 * This class is allocation-friendly and side-effect free.</p>
 */
public record Vector2(double x, double y) {

    /** Constant zero vector (0, 0). */
    public static final Vector2 ZERO = new Vector2(0.0, 0.0);

    /**
     * Creates a vector with both components set to the same value.
     *
     * @param value component value for x and y
     * @return vector (value, value)
     */
    public static Vector2 splat(double value) {
        return new Vector2(value, value);
    }

    /**
     * Component-wise addition.
     *
     * @param other vector to add
     * @return this + other
     */
    public Vector2 add(Vector2 other) {
        return new Vector2(x + other.x, y + other.y);
    }

    /**
     * Component-wise subtraction.
     *
     * @param other vector to subtract
     * @return this - other
     */
    public Vector2 sub(Vector2 other) {
        return new Vector2(x - other.x, y - other.y);
    }

    /**
     * Scalar multiplication.
     *
     * @param scalar multiplier
     * @return this * scalar
     */
    public Vector2 mul(double scalar) {
        return new Vector2(x * scalar, y * scalar);
    }

    /**
     * Scalar division.
     *
     * @param scalar divisor
     * @return this / scalar
     * @throws ArithmeticException if scalar is zero
     */
    public Vector2 div(double scalar) {
        if (scalar == 0.0) throw new ArithmeticException("Division by zero");
        return new Vector2(x / scalar, y / scalar);
    }

    /**
     * Dot product.
     *
     * @param other second operand
     * @return x*other.x + y*other.y
     */
    public double dot(Vector2 other) {
        return x * other.x + y * other.y;
    }

    /**
     * 2D cross product magnitude (z-component of 3D cross with z=0).
     *
     * @param other second operand
     * @return x*other.y - y*other.x
     */
    public double crossZ(Vector2 other) {
        return x * other.y - y * other.x;
    }

    /**
     * Squared Euclidean length.
     *
     * @return x^2 + y^2
     */
    public double lengthSq() {
        return x * x + y * y;
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
     * Euclidean distance to another vector.
     *
     * @param other target vector
     * @return |this - other|
     */
    public double distance(Vector2 other) {
        return Math.sqrt(distanceSq(other));
    }

    /**
     * Squared Euclidean distance to another vector.
     *
     * @param other target vector
     * @return |this - other|^2
     */
    public double distanceSq(Vector2 other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return dx * dx + dy * dy;
    }

    /**
     * Returns a unit-length vector in the same direction.
     *
     * <p>If this vector is zero-length, returns {@code this} unchanged.</p>
     *
     * @return normalized vector or this if length is zero
     */
    public Vector2 normalized() {
        double len = length();
        return len == 0.0 ? this : new Vector2(x / len, y / len);
    }

    /**
     * Returns the left-hand perpendicular vector.
     *
     * <p>For (x, y), result is (-y, x).</p>
     *
     * @return left perpendicular vector
     */
    public Vector2 perpLeft() {
        return new Vector2(-y, x);
    }

    /**
     * Returns the right-hand perpendicular vector.
     *
     * <p>For (x, y), result is (y, -x).</p>
     *
     * @return right perpendicular vector
     */
    public Vector2 perpRight() {
        return new Vector2(y, -x);
    }

    /**
     * Linearly interpolates between this vector and {@code to}.
     *
     * <p>Parameter {@code t} is not clamped.</p>
     *
     * @param to end vector
     * @param t interpolation factor
     * @return this + (to - this) * t
     */
    public Vector2 lerp(Vector2 to, double t) {
        return new Vector2(
                x + (to.x - x) * t,
                y + (to.y - y) * t
        );
    }

    /**
     * Component-wise minimum.
     *
     * @param other second operand
     * @return min(this, other) per component
     */
    public Vector2 min(Vector2 other) {
        return new Vector2(Math.min(x, other.x), Math.min(y, other.y));
    }

    /**
     * Component-wise maximum.
     *
     * @param other second operand
     * @return max(this, other) per component
     */
    public Vector2 max(Vector2 other) {
        return new Vector2(Math.max(x, other.x), Math.max(y, other.y));
    }
}
