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
     * Creates the default deterministic generator for this release.
     *
     * @param seed initial seed
     * @return deterministic random generator
     */
    public static DeterministicRandom defaultGenerator(long seed) {
        return splitMix64(seed);
    }

    /**
     * Creates a SplitMix64 generator.
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
