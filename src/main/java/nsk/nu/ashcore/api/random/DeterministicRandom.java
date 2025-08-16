package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.api.spi.Identified;

/**
 * Deterministic pseudo-random generator interface.
 * Implementations must be reproducible for the same seed.
 */
public interface DeterministicRandom extends Identified {
    /** @return next 64-bit pseudo-random value */
    long nextLong();

    /** @return next 32-bit pseudo-random value */
    default int nextInt() { return (int) nextLong(); }

    /** @return double in [0,1) generated from the next 53 random bits */
    default double nextUnitDouble() { return (nextLong() >>> 11) * 0x1.0p-53; }
}