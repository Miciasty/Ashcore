package nsk.nu.ashcore.api.noise;

/**
 * Fractal combinations of base noise (FBM, turbulence, ridge).
 * All methods are pure O(octaves) and stateless.
 */
public final class FractalNoise {
    private FractalNoise(){}

    /** Classic fractional Brownian motion (FBM). */
    public static double fbm(Noise2D n, double x, double y, int octaves, double lacunarity, double gain){
        double sum = 0, amp = 1, fx = x, fy = y;
        for (int i = 0; i < octaves; i++) {
            sum += n.sample(fx, fy) * amp;
            fx *= lacunarity; fy *= lacunarity; amp *= gain;
        }
        return sum;
    }

    /** 3D FBM. */
    public static double fbm(Noise3D n, double x, double y, double z, int octaves, double lacunarity, double gain){
        double sum = 0, amp = 1, fx = x, fy = y, fz = z;
        for (int i = 0; i < octaves; i++) {
            sum += n.sample(fx, fy, fz) * amp;
            fx *= lacunarity; fy *= lacunarity; fz *= lacunarity; amp *= gain;
        }
        return sum;
    }

    /** Turbulence: sum of absolute values of noise octaves. */
    public static double turbulence(Noise2D n, double x, double y, int octaves, double lacunarity, double gain){
        double sum = 0, amp = 1, fx = x, fy = y;
        for (int i = 0; i < octaves; i++) {
            sum += Math.abs(n.sample(fx, fy)) * amp;
            fx *= lacunarity; fy *= lacunarity; amp *= gain;
        }
        return sum;
    }

    /** Ridge (ridged multifractal): emphasizes valleys as ridges. */
    public static double ridge(Noise2D n, double x, double y, int octaves, double lacunarity, double gain){
        double sum = 0, amp = 0.5, fx = x, fy = y;
        for (int i = 0; i < octaves; i++) {
            double v = 1.0 - Math.abs(n.sample(fx, fy)); // invert valleys
            sum += v * v * amp;
            fx *= lacunarity; fy *= lacunarity; amp *= gain;
        }
        return sum;
    }
}