package nsk.nu.ashcore.api.noise;

import nsk.nu.ashcore.api.random.DeterministicRandom;

/**
 * Deterministic, seedable Perlin noise (improved Perlin).
 * - Range approximately [-1, 1]
 * - 2D and 3D variants
 * - No allocations during sampling
 *
 * Use {@link FractalNoise} for FBM/turbulence/ridge combinations.
 */
public final class PerlinNoise implements Noise2D, Noise3D {

    private final int[] perm = new int[512];

    /**
     * Builds a Perlin permutation table using the provided RNG.
     * The table is duplicated to avoid masking on each access.
     */
    public PerlinNoise(DeterministicRandom rng) {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;
        for (int i = 255; i > 0; i--) {
            int j = (int) (rng.nextUnitDouble() * (i + 1));
            int t = p[i]; p[i] = p[j]; p[j] = t;
        }
        for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
    }

    @Override
    public double sample(double x, double y) {
        int X = fastFloor(x) & 255;
        int Y = fastFloor(y) & 255;
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);
        double u = fade(xf);
        double v = fade(yf);

        int aa = perm[X] + Y;
        int ab = perm[X] + Y + 1;
        int ba = perm[X + 1] + Y;
        int bb = perm[X + 1] + Y + 1;

        double x1 = lerp(grad2(perm[aa], xf,     yf    ),
                grad2(perm[ba], xf - 1, yf    ), u);
        double x2 = lerp(grad2(perm[ab], xf,     yf - 1),
                grad2(perm[bb], xf - 1, yf - 1), u);
        return lerp(x1, x2, v);
    }

    @Override
    public double sample(double x, double y, double z) {
        int X = fastFloor(x) & 255;
        int Y = fastFloor(y) & 255;
        int Z = fastFloor(z) & 255;
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);
        double zf = z - Math.floor(z);
        double u = fade(xf);
        double v = fade(yf);
        double w = fade(zf);

        int A  = perm[X] + Y;
        int AA = perm[A] + Z;
        int AB = perm[A + 1] + Z;
        int B  = perm[X + 1] + Y;
        int BA = perm[B] + Z;
        int BB = perm[B + 1] + Z;

        double x1 = lerp(grad3(perm[AA], xf,     yf,     zf    ),
                grad3(perm[BA], xf - 1, yf,     zf    ), u);
        double x2 = lerp(grad3(perm[AB], xf,     yf - 1, zf    ),
                grad3(perm[BB], xf - 1, yf - 1, zf    ), u);
        double y1 = lerp(x1, x2, v);

        double x3 = lerp(grad3(perm[AA + 1], xf,     yf,     zf - 1),
                grad3(perm[BA + 1], xf - 1, yf,     zf - 1), u);
        double x4 = lerp(grad3(perm[AB + 1], xf,     yf - 1, zf - 1),
                grad3(perm[BB + 1], xf - 1, yf - 1, zf - 1), u);
        double y2 = lerp(x3, x4, v);

        return lerp(y1, y2, w);
    }

    private static int fastFloor(double x) { int i = (int)x; return x < i ? i - 1 : i; }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double a, double b, double t) { return a + t * (b - a); }

    private static double grad2(int hash, double x, double y) {
        return switch (hash & 7) {
            case 0 -> x + y;
            case 1 -> x - y;
            case 2 -> -x + y;
            case 3 -> -x - y;
            case 4 -> x;
            case 5 -> -x;
            case 6 -> y;
            default -> -y;
        };
    }

    private static double grad3(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = (h < 8) ? x : y;
        double v = (h < 4) ? y : (h == 12 || h == 14) ? x : z;
        return (((h & 1) == 0) ?  u : -u) + (((h & 2) == 0) ?  v : -v);
    }
}