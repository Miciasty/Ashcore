package nsk.nu.ashcore.api.math;

/** Immutable integer 3D vector. */
public record Vector3i(int x, int y, int z) {

    public static Vector3i zero() { return new Vector3i(0,0,0); }
    public static Vector3i of(int x, int y, int z) { return new Vector3i(x,y,z); }

    public Vector3i add(Vector3i o) { return new Vector3i(x + o.x, y + o.y, z + o.z); }
    public Vector3i sub(Vector3i o) { return new Vector3i(x - o.x, y - o.y, z - o.z); }
    public Vector3i mul(int k)      { return new Vector3i(x * k,   y * k,   z * k);    }

    public int dot(Vector3i o) {
        return x * o.x + y * o.y + z * o.z;
    }
    public Vector3i cross(Vector3i o) {
        int nx = y * o.z - z * o.y;
        int ny = z * o.x - x * o.z;
        int nz = x * o.y - y * o.x;
        return new Vector3i(nx, ny, nz);
    }

    public long lengthSq() {
        long lx = x, ly = y, lz = z;
        return lx*lx + ly*ly + lz*lz;
    }
    public double length() {
        return Math.sqrt(lengthSq());
    }

    public Vector3i withX(int nx) { return new Vector3i(nx, y,  z); }
    public Vector3i withY(int ny) { return new Vector3i(x,  ny, z); }
    public Vector3i withZ(int nz) { return new Vector3i(x,  y,  nz); }

    public Vector3 toVector3() { return new Vector3((double)x, (double)y, (double)z); }

    public static Vector3i fromFloor(Vector3 v) {
        return new Vector3i((int)Math.floor(v.x()),
                (int)Math.floor(v.y()),
                (int)Math.floor(v.z()));
    }
    public static Vector3i fromRound(Vector3 v) {
        return new Vector3i((int)Math.rint(v.x()),
                (int)Math.rint(v.y()),
                (int)Math.rint(v.z()));
    }
    public static Vector3i fromCeil(Vector3 v) {
        return new Vector3i((int)Math.ceil(v.x()),
                (int)Math.ceil(v.y()),
                (int)Math.ceil(v.z()));
    }
}