package nsk.nu.ashcore.api.math;

/**
 * Immutable 3D vector with integer components.
 *
 * <p>Use this type for discrete coordinates (e.g. grid/voxel indices, chunk/block keys),
 * where integer semantics are required.</p>
 */
public record Vector3i(int x, int y, int z) {

    /** Constant zero vector (0, 0, 0). */
    public static final Vector3i ZERO = new Vector3i(0, 0, 0);

    /**
     * Factory method.
     *
     * @param x x component
     * @param y y component
     * @param z z component
     * @return new integer vector
     */
    public static Vector3i of(int x, int y, int z) {
        return new Vector3i(x, y, z);
    }

    /**
     * Component-wise addition.
     *
     * @param other vector to add
     * @return this + other
     */
    public Vector3i add(Vector3i other) {
        return new Vector3i(x + other.x, y + other.y, z + other.z);
    }

    /**
     * Component-wise subtraction.
     *
     * @param other vector to subtract
     * @return this - other
     */
    public Vector3i sub(Vector3i other) {
        return new Vector3i(x - other.x, y - other.y, z - other.z);
    }

    /**
     * Scalar multiplication.
     *
     * @param k scalar
     * @return this * k
     */
    public Vector3i mul(int k) {
        return new Vector3i(x * k, y * k, z * k);
    }

    /**
     * Dot product in integer arithmetic.
     *
     * <p>Result is {@code int}; for large magnitudes this may overflow.</p>
     *
     * @param other second operand
     * @return x*ox + y*oy + z*oz
     */
    public int dot(Vector3i other) {
        return x * other.x + y * other.y + z * other.z;
    }

    /**
     * Cross product in integer arithmetic.
     *
     * @param other second operand
     * @return this x other
     */
    public Vector3i cross(Vector3i other) {
        int nx = y * other.z - z * other.y;
        int ny = z * other.x - x * other.z;
        int nz = x * other.y - y * other.x;
        return new Vector3i(nx, ny, nz);
    }

    /**
     * Squared Euclidean length.
     *
     * <p>Computed in {@code long} to reduce overflow risk compared to {@code int}.</p>
     *
     * @return x^2 + y^2 + z^2
     */
    public long lengthSq() {
        long lx = x;
        long ly = y;
        long lz = z;
        return lx * lx + ly * ly + lz * lz;
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
    public Vector3i withX(int nx) {
        return new Vector3i(nx, y, z);
    }

    /**
     * Returns a copy with replaced y component.
     *
     * @param ny new y value
     * @return copied vector with new y
     */
    public Vector3i withY(int ny) {
        return new Vector3i(x, ny, z);
    }

    /**
     * Returns a copy with replaced z component.
     *
     * @param nz new z value
     * @return copied vector with new z
     */
    public Vector3i withZ(int nz) {
        return new Vector3i(x, y, nz);
    }

    /**
     * Converts to floating-point vector.
     *
     * @return Vector3 with the same numeric component values
     */
    public Vector3 toVector3() {
        return new Vector3((double) x, (double) y, (double) z);
    }

    /**
     * Converts floating-point vector to integer vector by flooring each component.
     *
     * @param v source vector
     * @return (floor(x), floor(y), floor(z))
     */
    public static Vector3i fromFloor(Vector3 v) {
        return new Vector3i(
                (int) Math.floor(v.x()),
                (int) Math.floor(v.y()),
                (int) Math.floor(v.z())
        );
    }

    /**
     * Converts floating-point vector to integer vector by rounding each component
     * to the nearest integer (ties according to {@link Math#round(double)}).
     *
     * @param v source vector
     * @return (round(x), round(y), round(z))
     */
    public static Vector3i fromRound(Vector3 v) {
        return new Vector3i(
                (int) Math.round(v.x()),
                (int) Math.round(v.y()),
                (int) Math.round(v.z())
        );
    }

    /**
     * Converts floating-point vector to integer vector by ceiling each component.
     *
     * @param v source vector
     * @return (ceil(x), ceil(y), ceil(z))
     */
    public static Vector3i fromCeil(Vector3 v) {
        return new Vector3i(
                (int) Math.ceil(v.x()),
                (int) Math.ceil(v.y()),
                (int) Math.ceil(v.z())
        );
    }
}