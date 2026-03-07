package nsk.nu.ashcore.api.math;

/**
 * Immutable 2D vector with integer components.
 *
 * <p>Use this type for discrete 2D coordinates (grid cells, tile indices, chunk keys).</p>
 */
public record Vector2i(int x, int y) {

    /** Constant zero vector (0, 0). */
    public static final Vector2i ZERO = new Vector2i(0, 0);

    /**
     * Factory method.
     *
     * @param x x component
     * @param y y component
     * @return new integer vector
     */
    public static Vector2i of(int x, int y) {
        return new Vector2i(x, y);
    }

    /**
     * Component-wise addition.
     *
     * @param other vector to add
     * @return this + other
     */
    public Vector2i add(Vector2i other) {
        return new Vector2i(x + other.x, y + other.y);
    }

    /**
     * Component-wise subtraction.
     *
     * @param other vector to subtract
     * @return this - other
     */
    public Vector2i sub(Vector2i other) {
        return new Vector2i(x - other.x, y - other.y);
    }

    /**
     * Scalar multiplication.
     *
     * @param k scalar
     * @return this * k
     */
    public Vector2i mul(int k) {
        return new Vector2i(x * k, y * k);
    }

    /**
     * Dot product in integer arithmetic.
     *
     * <p>Result is {@code int}; for large magnitudes this may overflow.</p>
     *
     * @param other second operand
     * @return x*other.x + y*other.y
     */
    public int dot(Vector2i other) {
        return x * other.x + y * other.y;
    }

    /**
     * 2D cross product magnitude (z-component of 3D cross with z=0).
     *
     * <p>Result is {@code int}; for large magnitudes this may overflow.</p>
     *
     * @param other second operand
     * @return x*other.y - y*other.x
     */
    public int crossZ(Vector2i other) {
        return x * other.y - y * other.x;
    }

    /**
     * Squared Euclidean length.
     *
     * <p>Computed in {@code long} to reduce overflow risk compared to {@code int}.</p>
     *
     * @return x^2 + y^2
     */
    public long lengthSq() {
        long lx = x;
        long ly = y;
        return lx * lx + ly * ly;
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
     * Returns a copy with replaced x component.
     *
     * @param nx new x value
     * @return copied vector with new x
     */
    public Vector2i withX(int nx) {
        return new Vector2i(nx, y);
    }

    /**
     * Returns a copy with replaced y component.
     *
     * @param ny new y value
     * @return copied vector with new y
     */
    public Vector2i withY(int ny) {
        return new Vector2i(x, ny);
    }

    /**
     * Converts to floating-point vector.
     *
     * @return Vector2 with the same numeric component values
     */
    public Vector2 toVector2() {
        return new Vector2((double) x, (double) y);
    }

    /**
     * Converts floating-point vector to integer vector by flooring each component.
     *
     * @param v source vector
     * @return (floor(x), floor(y))
     */
    public static Vector2i fromFloor(Vector2 v) {
        return new Vector2i(
                (int) Math.floor(v.x()),
                (int) Math.floor(v.y())
        );
    }

    /**
     * Converts floating-point vector to integer vector by rounding each component
     * to the nearest integer (ties according to {@link Math#round(double)}).
     *
     * @param v source vector
     * @return (round(x), round(y))
     */
    public static Vector2i fromRound(Vector2 v) {
        return new Vector2i(
                (int) Math.round(v.x()),
                (int) Math.round(v.y())
        );
    }

    /**
     * Converts floating-point vector to integer vector by ceiling each component.
     *
     * @param v source vector
     * @return (ceil(x), ceil(y))
     */
    public static Vector2i fromCeil(Vector2 v) {
        return new Vector2i(
                (int) Math.ceil(v.x()),
                (int) Math.ceil(v.y())
        );
    }
}