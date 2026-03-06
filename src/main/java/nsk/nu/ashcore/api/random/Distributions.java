package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.api.math.NumericTolerance;

/** Common continuous/discrete distributions built on DeterministicRandom. */
public final class Distributions {
    private Distributions(){}

    /** Standard normal N(0,1) via Box-Muller; consumes two uniforms, returns one. */
    public static double gaussian01(DeterministicRandom rng){
        double u1 = Math.max(rng.nextUnitDouble(), NumericTolerance.EPS); // avoid log(0)
        double u2 = rng.nextUnitDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2*Math.PI*u2);
    }
    /** Exponential(lambda) with lambda > 0. */
    public static double exponential(DeterministicRandom rng, double lambda){
        if (lambda <= 0) throw new IllegalArgumentException("lambda > 0");
        double u = Math.max(rng.nextUnitDouble(), NumericTolerance.EPS);
        return -Math.log(u) / lambda;
    }
    /** Poisson(k; lambda) using Knuth's algorithm (ok for small lambda). */
    public static int poisson(DeterministicRandom rng, double lambda){
        if (lambda <= 0) return 0;
        double L = Math.exp(-lambda), p = 1.0;
        int k = 0;
        do { k++; p *= rng.nextUnitDouble(); } while (p > L);
        return k - 1;
    }
}