package nsk.nu.api.math;

/** Immutable integer 3D vector for indices/keys. */
public record Int3(int x, int y, int z) {
    public Int3 add(Int3 o){ return new Int3(x+o.x, y+o.y, z+o.z); }
    public Int3 sub(Int3 o){ return new Int3(x-o.x, y-o.y, z-o.z); }
    public Int3 mul(int s){ return new Int3(x*s, y*s, z*s); }
}