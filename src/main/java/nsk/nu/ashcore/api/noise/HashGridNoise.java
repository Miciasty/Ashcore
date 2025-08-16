package nsk.nu.ashcore.api.noise;

import nsk.nu.ashcore.api.hash.Hash64;

public final class HashGridNoise {
    private HashGridNoise(){}

    /** Returns deterministic in [0,1) for integer cell with seed. */
    public static double value2D(int x, int y, long seed){
        long h = Hash64.mix64(seed ^ Hash64.mix64((x * 0x9E3779B97F4A7C15L) ^ (y * 0xBF58476D1CE4E5B9L)));
        return ((h >>> 11) & ((1L<<53)-1)) * 0x1.0p-53;
    }
    /** Trilinear-smooth 3D noise from integer grid corners. */
    public static double value3D(double x, double y, double z, long seed){
        int X=(int)Math.floor(x), Y=(int)Math.floor(y), Z=(int)Math.floor(z);
        double fx=x-X, fy=y-Y, fz=z-Z;
        double sx = fx*fx*(3-2*fx), sy = fy*fy*(3-2*fy), sz = fz*fz*(3-2*fz);
        double c000 = value2D(X, Y, seed ^ Z);
        double c100 = value2D(X+1, Y, seed ^ Z);
        double c010 = value2D(X, Y+1, seed ^ Z);
        double c110 = value2D(X+1, Y+1, seed ^ Z);
        double c001 = value2D(X, Y, seed ^ (Z+1));
        double c101 = value2D(X+1, Y, seed ^ (Z+1));
        double c011 = value2D(X, Y+1, seed ^ (Z+1));
        double c111 = value2D(X+1, Y+1, seed ^ (Z+1));
        double ix00 = lerp(c000, c100, sx), ix10 = lerp(c010, c110, sx);
        double ix01 = lerp(c001, c101, sx), ix11 = lerp(c011, c111, sx);
        double iy0 = lerp(ix00, ix10, sy), iy1 = lerp(ix01, ix11, sy);
        return lerp(iy0, iy1, sz);
    }
    private static double lerp(double a,double b,double t){ return a + (b-a)*t; }
}