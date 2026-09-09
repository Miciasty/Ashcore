package nsk.nu.ashcore.api.random;

import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;

class BoundedRandomTest {

    @Test
    void boundedDraws_matchFixedSplitMix64Sequence() {
        DeterministicRandom rng = DeterministicRandoms.splitMix64(0);
        assertEquals(3, rng.nextInt(10));
        assertEquals(850, rng.nextLong(1000));
        assertEquals(-43, rng.nextInt(-50, 50));
        assertEquals(-537132696929009172L, rng.nextLong(Long.MIN_VALUE, Long.MAX_VALUE));
        assertEquals(684997197, rng.nextInt(Integer.MAX_VALUE));
        assertEquals(3019047300631581045L, rng.nextLong(Long.MAX_VALUE));
        assertEquals(3207296026000306913L, rng.nextLong());
    }

    @Test
    void invalidBounds_consumeNoState() {
        ScriptedRandom rng = new ScriptedRandom();
        assertThrows(IllegalArgumentException.class, () -> rng.nextInt(0));
        assertThrows(IllegalArgumentException.class, () -> rng.nextInt(-1));
        assertThrows(IllegalArgumentException.class, () -> rng.nextInt(2, 2));
        assertThrows(IllegalArgumentException.class, () -> rng.nextInt(2, -2));
        assertThrows(IllegalArgumentException.class, () -> rng.nextLong(0));
        assertThrows(IllegalArgumentException.class, () -> rng.nextLong(Long.MIN_VALUE));
        assertThrows(IllegalArgumentException.class, () -> rng.nextLong(2, 2));
        assertThrows(IllegalArgumentException.class, () -> rng.nextLong(2, -2));
        assertEquals(0, rng.draws);
    }

    @Test
    void powersOfTwoAndSingletons_consumeOneDrawEach() {
        ScriptedRandom rng = new ScriptedRandom(-1, -1, -1, -1, 0, 0);
        assertEquals(7, rng.nextInt(8));
        assertEquals(7, rng.nextLong(8));
        assertEquals(0, rng.nextInt(1));
        assertEquals(0, rng.nextLong(1));
        assertEquals(Integer.MAX_VALUE - 1, rng.nextInt(Integer.MAX_VALUE - 1, Integer.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, rng.nextLong(Long.MIN_VALUE, Long.MIN_VALUE + 1));
        assertEquals(6, rng.draws);
    }

    @Test
    void rejection_discardsIncompleteModuloBucket() {
        ScriptedRandom ints = new ScriptedRandom(-1, 10);
        assertEquals(2, ints.nextInt(3));
        assertEquals(2, ints.draws);
        ScriptedRandom longs = new ScriptedRandom(-1, 10);
        assertEquals(2, longs.nextLong(3));
        assertEquals(2, longs.draws);
        assertEquals(2, new ScriptedRandom(-1, 4).nextInt(Integer.MAX_VALUE));
        assertEquals(2, new ScriptedRandom(-1, 4).nextLong(Long.MAX_VALUE));
    }

    @Test
    void overflowingWidths_useFullSignedDrawsAndKeepUpperBoundExclusive() {
        ScriptedRandom ints = new ScriptedRandom(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, ints.nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertEquals(2, ints.draws);
        ScriptedRandom longs = new ScriptedRandom(Long.MAX_VALUE, Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, longs.nextLong(Long.MIN_VALUE, Long.MAX_VALUE));
        assertEquals(2, longs.draws);
        assertEquals(-1, new ScriptedRandom(0, -1).nextInt(Integer.MIN_VALUE, 0));
        assertEquals(-1, new ScriptedRandom(0, -1).nextLong(Long.MIN_VALUE, 0));
    }

    @Test
    void boundedDraws_matchJdkReferenceUsingTheSameSourceBits() {
        DeterministicRandom actual = DeterministicRandoms.splitMix64(42);
        RandomGenerator reference = new RandomGenerator() {
            private final SplittableRandom source = new SplittableRandom(42);
            @Override public long nextLong() { return source.nextLong(); }
            @Override public int nextInt() { return (int) nextLong(); }
        };
        for (int i = 0; i < 1000; i++) {
            for (int bound : new int[]{1, 2, 3, 17, 1 << 30, Integer.MAX_VALUE}) {
                assertEquals(reference.nextInt(bound), actual.nextInt(bound));
            }
            for (long bound : new long[]{1, 2, 3, 17, 1L << 62, Long.MAX_VALUE}) {
                assertEquals(reference.nextLong(bound), actual.nextLong(bound));
            }
            assertEquals(reference.nextInt(-10, 21), actual.nextInt(-10, 21));
            assertEquals(reference.nextLong(-10, 21), actual.nextLong(-10, 21));
            assertEquals(reference.nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE), actual.nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE));
            assertEquals(reference.nextLong(Long.MIN_VALUE, Long.MAX_VALUE), actual.nextLong(Long.MIN_VALUE, Long.MAX_VALUE));
        }
        assertEquals(reference.nextLong(), actual.nextLong());
    }

    @Test
    void boundedDraws_coverSmallRangesAndRepeatForSameCallOrder() {
        DeterministicRandom a = DeterministicRandoms.splitMix64(7), b = DeterministicRandoms.splitMix64(7);
        int[] counts = new int[3];
        for (int i = 0; i < 30000; i++) {
            int value = a.nextInt(-1, 2);
            assertEquals(b.nextInt(-1, 2), value);
            assertTrue(value >= -1 && value < 2);
            counts[value + 1]++;
        }
        for (int count : counts) assertEquals(10000, count, 400);
    }

    private static final class ScriptedRandom implements DeterministicRandom {
        private final long[] values;
        private int draws;

        private ScriptedRandom(long... values) { this.values = values; }
        @Override public String id() { return "test:scripted"; }
        @Override public long nextLong() {
            assertTrue(draws < values.length, "Unexpected extra draw");
            return values[draws++];
        }
    }
}
