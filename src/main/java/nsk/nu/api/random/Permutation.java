package nsk.nu.api.random;

/**
 * Deterministic permutations and shuffles based on {@link DeterministicRandom}.
 */
public final class Permutation {
    private Permutation() {}

    /** In-place Fisher–Yates shuffle of an int array. */
    public static void shuffle(int[] a, DeterministicRandom rng) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = (int) (rng.nextUnitDouble() * (i + 1));
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }
}