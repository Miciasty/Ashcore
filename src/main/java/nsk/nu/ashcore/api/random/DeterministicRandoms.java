package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.implementation.random.SplitMix64Random;

/**
 * Factory methods for deterministic random generators.
 *
 * <p>This class hides concrete RNG implementations behind the
 * {@link DeterministicRandom} interface.</p>
 */
public final class DeterministicRandoms {
    private DeterministicRandoms() { }

    /**
     * Creates SplitMix64, the default throughout Ashcore 1.x. Use splitMix64 when persisting algorithm identity.
     *
     * @param seed initial seed
     * @return deterministic random generator
     */
    public static DeterministicRandom defaultGenerator(long seed) {
        return splitMix64(seed);
    }

    /**
     * Creates a mutable SplitMix64 generator. Its nextLong stream and default int/double conversions
     * are stable in 1.x for the same seed and ordered calls; no shared concurrent mutation is supported.
     *
     * @param seed initial seed
     * @return SplitMix64-based deterministic generator
     */
    public static DeterministicRandom splitMix64(long seed) {
        return new SplitMix64Random(seed);
    }

    /**
     * Creates the default generator from a derived seed.
     *
     * @param sequence seed sequence
     * @param tag derivation tag
     * @return deterministic random generator initialized from derived seed
     * @throws NullPointerException if sequence or tag is null
     */
    public static DeterministicRandom fromDerivedSeed(SeedSequence sequence, String tag) {
        if (sequence == null) throw new NullPointerException("sequence");
        if (tag == null) throw new NullPointerException("tag");
        return defaultGenerator(sequence.derive(tag));
    }
}
