package nsk.nu.api.stats;

import nsk.nu.api.random.DeterministicRandom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Reservoir sampling (Algorithm R) for selecting {@code k} items uniformly at random
 * from a stream of unknown (or very large) size using O(k) memory and O(1) time per item.
 *
 * <p>Algorithm:
 * <ol>
 *   <li>Fill the reservoir with the first {@code k} items.</li>
 *   <li>For the i-th item (1-based), draw {@code j ~ U[0, i)}.
 *       If {@code j < k}, replace {@code reservoir[j]} with the new item.</li>
 * </ol>
 * After processing {@code n} items, each has probability exactly {@code k/n} to be present.
 *
 * <p>Determinism: all randomness comes from {@link DeterministicRandom}. With the same seed
 * and the same stream order you will get identical results across runs.
 *
 * <h3>Complexity</h3>
 * <ul>
 *   <li>{@link #offer(Object)}: O(1)</li>
 *   <li>{@link #snapshot()}: O(k)</li>
 *   <li>Memory: O(k)</li>
 * </ul>
 *
 * <h3>Thread-safety</h3>
 * Not thread-safe. Wrap externally if multiple producers call {@code offer} concurrently.
 *
 * @param <T> item type (nulls are allowed and will be sampled like any other value)
 */
public final class ReservoirSampler<T> {

    private final Object[] reservoir;           // fixed-size sample buffer
    private final DeterministicRandom rng;

    private long seen = 0L;                     // how many items were offered
    private int size = 0;                       // how many slots are currently filled (<= capacity)

    /**
     * @param capacity reservoir size k (> 0)
     * @param rng deterministic RNG used for sampling
     */
    public ReservoirSampler(int capacity, DeterministicRandom rng) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity > 0");
        if (rng == null) throw new NullPointerException("rng");
        this.reservoir = new Object[capacity];
        this.rng = rng;
    }

    /** @return maximum number of items kept in the reservoir (k). */
    public int capacity() { return reservoir.length; }

    /** @return current number of filled slots (≤ capacity). */
    public int size() { return size; }

    /** @return total number of items that have been offered to this sampler. */
    public long seenCount() { return seen; }

    /** @return true if the reservoir is fully filled. */
    public boolean isFull() { return size == reservoir.length; }

    /**
     * Offers a new item from the stream to the reservoir (Algorithm R step).
     * O(1) time; replaces an existing item with decreasing probability as the stream grows.
     */
    public void offer(T item) {
        seen++;
        final int cap = reservoir.length;

        // Fast path: still filling
        if (size < cap) {
            reservoir[size++] = item;
            return; // if-gate: no further work needed
        }

        // Sampling path
        final long j = (long) (rng.nextUnitDouble() * seen); // j in [0, seen)
        if (j < cap) {
            reservoir[(int) j] = item;
        }
    }

    /**
     * Clears all state and empties the reservoir.
     */
    public void reset() {
        Arrays.fill(reservoir, null);
        seen = 0L;
        size = 0;
    }

    /**
     * @return an unmodifiable view of the currently filled items (size ≤ k).
     *         Copies out the filled prefix only; never exposes null tail.
     */
    @SuppressWarnings("unchecked")
    public List<T> snapshot() {
        ArrayList<T> out = new ArrayList<>(size);
        for (int i = 0; i < size; i++) out.add((T) reservoir[i]);
        return Collections.unmodifiableList(out);
    }

    /**
     * @return a raw copy of the underlying reservoir (length == k), including any nulls in the unfilled tail.
     *         Useful for diagnostics or fixed-size consumers.
     */
    public Object[] snapshotRaw() {
        return Arrays.copyOf(reservoir, reservoir.length);
    }
}
