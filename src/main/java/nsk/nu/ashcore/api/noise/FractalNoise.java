package nsk.nu.ashcore.api.noise;

/**
 * Fractal combinations of base noise (FBM, turbulence, ridge).
 * Stateless; repeatability and thread safety also require a repeatable, safe caller-supplied noise function.
 * With o=max(0,octaves) and callback cost C, time is O(o*C) and own extra memory O(1).
 * Calls the callback once per octave in increasing octave order; non-positive octaves return zero.
 * Coordinates are in the base noise's units; lacunarity and gain are dimensionless frequency/amplitude
 * multipliers. Callers must keep each sampled coordinate within the base noise's domain and all arithmetic
 * finite. Results are unnormalized sums, not necessarily in [-1,1]; no generic error bound is supplied.
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
