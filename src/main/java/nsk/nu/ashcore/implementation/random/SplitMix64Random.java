package nsk.nu.ashcore.implementation.random;

import nsk.nu.ashcore.api.random.DeterministicRandom;

/**
 * SplitMix64 PRNG: fast, good-quality generator commonly used to seed stronger RNGs.
 * Reference: Steele et al. "Fast Splittable Pseudorandom Number Generators."
 */
public final class SplitMix64Random implements DeterministicRandom {
    private long state;

    /**
     * @param seed initial seed; identical seeds produce identical sequences
     */
    public SplitMix64Random(long seed) { this.state = seed; }

    @Override public String id() { return "random:splitmix64"; }

    @Override public long nextLong() {
        long z = (state += 0x9E3779B97F4A7C15L);
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}