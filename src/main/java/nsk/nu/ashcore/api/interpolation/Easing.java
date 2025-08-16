package nsk.nu.ashcore.api.interpolation;

import java.util.function.DoubleUnaryOperator;

/**
 * Common easing functions mapping [0,1] to [0,1].
 * Useful for smoothing abrupt changes without reinventing animation libraries.
 */
public final class Easing {
    private Easing() {}

    /** Linear interpolation: f(t) = t */
    public static DoubleUnaryOperator linear() { return t -> t; }

    /** Smoothstep: cubic S-curve, C1 continuous. */
    public static DoubleUnaryOperator smoothstep() { return t -> t * t * (3 - 2 * t); }

    /** Quadratic ease-in-out. */
    public static DoubleUnaryOperator quadInOut() {
        return t -> t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    }
}