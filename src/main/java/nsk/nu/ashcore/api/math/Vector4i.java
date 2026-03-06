package nsk.nu.ashcore.api.math;

/**
 * Immutable 4D vector with integer components.
 *
 * <p>Use this type for discrete 4D coordinates or keys where integer semantics are required.</p>
 */
public record Vector4i(int x, int y, int z, int w) {

    /** Constant zero vector (0, 0, 0, 0). */
    public static final Vector4i ZERO = new Vector4i(0, 0, 0, 0);

    /**
     * Factory method.
     *
     * @param x x component
     * @param y y component
     * @param z z component
     * @param w w component
     * @return new integer vector
     */
    public static Vector4i of(int x, int y, int z, int w) {
        return new Vector4i(x, y, z, w);
    }

    /**
     * Creates a 4D integer vector from a 3D integer vector and explicit w component.
     *
     * @param v xyz source
     * @param w w component
     * @return new vector (v.x, v.y, v.z, w)
     */
    public static Vector4i fromVector3i(Vector3i v, int w) {
        return new Vector4i(v.x(), v.y(), v.z(), w);
    }

    /**
     * Returns the xyz part as Vector3i.
     *
     * @return vector (x, y, z)
     */
    public Vector3i xyz() {
        return new Vector3i(x, y, z);
    }

    /**
     * Component-wise addition.
     *
     * @param other vector to add
     * @return this + other
     */
    public Vector4i add(Vector4i other) {
        return new Vector4i(x + other.x, y + other.y, z + other.z, w + other.w);
    }

    /**
     * Component-wise subtraction.
     *
     * @param other vector to subtract
     * @return this - other
     */
    public Vector4i sub(Vector4i other) {
        return new Vector4i(x - other.x, y - other.y, z - other.z, w - other.w);
    }

    /**
     * Scalar multiplication.
     *
     * @param k scalar
     * @return this * k
     */
    public Vector4i mul(int k) {
        return new Vector4i(x * k, y * k, z * k, w * k);
    }

    /**
     * Dot product in integer arithmetic.
     *
     * <p>Result is {@code int}; for large magnitudes this may overflow.</p>
     *
     * @param other second operand
     * @return x*ox + y*oy + z*oz + w*ow
     */
    public int dot(Vector4i other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    /**
     * Squared Euclidean length.
     *
     * <p>Computed in {@code long} to reduce overflow risk compared to {@code int}.</p>
     *
     * @return x^2 + y^2 + z^2 + w^2
     */
    public long lengthSq() {
        long lx = x;
        long ly = y;
        long lz = z;
        long lw = w;
        return lx * lx + ly * ly + lz * lz + lw * lw;
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
    public Vector4i withX(int nx) {
        return new Vector4i(nx, y, z, w);
    }

    /**
     * Returns a copy with replaced y component.
     *
     * @param ny new y value
     * @return copied vector with new y
     */
    public Vector4i withY(int ny) {
        return new Vector4i(x, ny, z, w);
    }

    /**
     * Returns a copy with replaced z component.
     *
     * @param nz new z value
     * @return copied vector with new z
     */
    public Vector4i withZ(int nz) {
        return new Vector4i(x, y, nz, w);
    }

    /**
     * Returns a copy with replaced w component.
     *
     * @param nw new w value
     * @return copied vector with new w
     */
    public Vector4i withW(int nw) {
        return new Vector4i(x, y, z, nw);
    }

    /**
     * Converts to floating-point vector.
     *
     * @return Vector4 with the same numeric component values
     */
    public Vector4 toVector4() {
        return new Vector4((double) x, (double) y, (double) z, (double) w);
    }

    /**
     * Converts floating-point vector to integer vector by flooring each component.
     *
     * @param v source vector
     * @return (floor(x), floor(y), floor(z), floor(w))
     */
    public static Vector4i fromFloor(Vector4 v) {
        return new Vector4i(
                (int) Math.floor(v.x()),
                (int) Math.floor(v.y()),
                (int) Math.floor(v.z()),
                (int) Math.floor(v.w())
        );
    }

    /**
     * Converts floating-point vector to integer vector by rounding each component
     * to the nearest integer (ties according to {@link Math#round(double)}).
     *
     * @param v source vector
     * @return (round(x), round(y), round(z), round(w))
     */
    public static Vector4i fromRound(Vector4 v) {
        return new Vector4i(
                (int) Math.round(v.x()),
                (int) Math.round(v.y()),
                (int) Math.round(v.z()),
                (int) Math.round(v.w())
        );
    }

    /**
     * Converts floating-point vector to integer vector by ceiling each component.
     *
     * @param v source vector
     * @return (ceil(x), ceil(y), ceil(z), ceil(w))
     */
    public static Vector4i fromCeil(Vector4 v) {
        return new Vector4i(
                (int) Math.ceil(v.x()),
                (int) Math.ceil(v.y()),
                (int) Math.ceil(v.z()),
                (int) Math.ceil(v.w())
        );
    }
}
