package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.api.spi.Identified;

/**
 * Deterministic pseudo-random generator interface.
 * Implementations must be reproducible for the same algorithm version, initial state and ordered calls.
 * The unbounded default nextInt/nextUnitDouble each consume one nextLong; mixing calls changes subsequent values.
 * Mutable implementations require exclusive access or externally ordered synchronization.
 */
public interface DeterministicRandom extends Identified {
    /** @return next 64-bit pseudo-random value */
    long nextLong();

    /** @return next 32-bit pseudo-random value */
    default int nextInt() { return (int) nextLong(); }

    /**
     * Draws uniformly from [0,bound), assuming uniform source bits; rejects non-positive bound before drawing.
     * Powers of two use low bits. Other bounds reject excess values from 31 non-negative bits to avoid modulo bias.
     * Consumes one or more nextInt calls, including one for bound=1. Expected O(1) time and O(1) memory;
     * no finite worst-case draw count. A custom generator that repeatedly supplies rejected bits may not terminate.
     */
    default int nextInt(int bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        int r = nextInt(), m = bound - 1;
        if ((bound & m) == 0) return r & m;
        for (int u = r >>> 1; u + m - (r = u % bound) < 0; u = nextInt() >>> 1) { }
        return r;
    }

    /**
     * Draws from [origin,bound); requires origin less than bound. Supports ranges whose width overflows int.
     * Uses nextInt(width) when width fits, otherwise rejects full-width draws outside the interval.
     * State consumption and expected cost follow nextInt(bound); invalid bounds consume no state.
     */
    default int nextInt(int origin, int bound) {
        if (origin >= bound) throw new IllegalArgumentException("origin must be less than bound");
        int width = bound - origin;
        if (width > 0) return origin + nextInt(width);
        int r;
        do { r = nextInt(); } while (r < origin || r >= bound);
        return r;
    }

    /**
     * Draws uniformly from [0,bound), assuming uniform source bits; rejects non-positive bound before drawing.
     * Powers of two use low bits. Other bounds reject excess values from 63 non-negative bits to avoid modulo bias.
     * Consumes one or more nextLong calls, including one for bound=1. Expected O(1) time and O(1) memory;
     * no finite worst-case draw count. A custom generator that repeatedly supplies rejected bits may not terminate.
     */
    default long nextLong(long bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        long r = nextLong(), m = bound - 1;
        if ((bound & m) == 0) return r & m;
        for (long u = r >>> 1; u + m - (r = u % bound) < 0; u = nextLong() >>> 1) { }
        return r;
    }

    /**
     * Draws from [origin,bound); requires origin less than bound. Supports ranges whose width overflows long.
     * Uses nextLong(width) when width fits, otherwise rejects full-width draws outside the interval.
     * State consumption and expected cost follow nextLong(bound); invalid bounds consume no state.
     */
    default long nextLong(long origin, long bound) {
        if (origin >= bound) throw new IllegalArgumentException("origin must be less than bound");
        long width = bound - origin;
        if (width > 0) return origin + nextLong(width);
        long r;
        do { r = nextLong(); } while (r < origin || r >= bound);
        return r;
    }

    /** @return double in [0,1) generated from the next 53 random bits */
    default double nextUnitDouble() { return (nextLong() >>> 11) * 0x1.0p-53; }
}
