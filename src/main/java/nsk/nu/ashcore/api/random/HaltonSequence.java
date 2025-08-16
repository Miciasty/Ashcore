package nsk.nu.ashcore.api.random;

import java.util.ArrayList;
import java.util.List;

/**
 * Incremental Halton sequence generator for a single prime base.
 * Amortized O(1) per sample by updating only the changed base-b digits.
 *
 * <p>State:
 * <ul>
 *   <li>digits[i] is the i-th (0-based) base-b digit of the integer counter</li>
 *   <li>invPow[i] = b^{-(i+1)} so the radical inverse value is sum(digits[i] * invPow[i])</li>
 * </ul>
 *
 * <p>Use this when iterating many consecutive samples. For random access to the i-th index,
 * {@link LowDiscrepancy#halton(int, int)} stays O(log i) and by design jest do tego lepsze.</p>
 */
public final class HaltonSequence {
    private final int base;
    private final List<Integer> digits = new ArrayList<>(8);
    private final List<Double> invPow = new ArrayList<>(8);
    private double value = 0.0;
    private int index = 0;

    /**
     * @param base prime base > 1 (typowo 2, 3, 5, 7, ...)
     */
    public HaltonSequence(int base) {
        if (base <= 1) throw new IllegalArgumentException("base > 1 required");
        this.base = base;
    }
    /** @return current index (number of values already produced) */
    public int index() { return index; }
    /** Resets to the beginning (value for index 0 is 0). */
    public void reset() {
        digits.clear();
        invPow.clear();
        value = 0.0;
        index = 0;
    }
    /**
     * Advances to the next value and returns it.
     * Amortized O(1): expected number of updated digits ≈ 1 + 1/(base-1).
     */
    public double next() {
        incrementDigits();
        index++;
        return value;
    }

    private void incrementDigits() {
        int i = 0;
        while (true) {
            if (i == digits.size()) {
                digits.add(0);
                invPow.add(i == 0 ? 1.0 / base : invPow.get(i - 1) / base);
            }
            int d = digits.get(i);
            if (d + 1 < base) {
                digits.set(i, d + 1);
                value += invPow.get(i);
                return;
            } else {
                digits.set(i, 0);
                value -= (base - 1) * invPow.get(i);
                i++;
            }
        }
    }
}