package nsk.nu.ashcore.api.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathScalarApiTest {

    @Test
    void angles_wrapAndDelta_followShortestArcConventions() {
        // We test normalization and shortest-path delta conventions in radians and degrees.
        assertEquals(-Math.PI, Angles.wrapRadians(-Math.PI), 1e-12);
        assertEquals(350.0, Angles.wrapDegrees360(-10.0), 1e-12);
        assertEquals(180.0, Angles.wrapDegrees180(-180.0), 1e-12);
        assertEquals(20.0, Angles.deltaDegrees(350.0, 10.0), 1e-12);
        assertEquals(Math.PI / 2, Angles.deltaRadians(0.0, Math.PI / 2), 1e-12);
    }

    @Test
    void angles_directionConversion_isConsistentForYawAndPitch() {
        // We test that yaw/pitch conversion produces a unit direction with expected orientation.
        Vector3 dir = Angles.dirFromYawPitch(0.0, 0.0);

        assertEquals(0.0, dir.x(), 1e-12);
        assertEquals(0.0, dir.y(), 1e-12);
        assertEquals(1.0, dir.z(), 1e-12);
        assertEquals(0.0, Angles.yawFromXZ(0.0, 1.0), 1e-12);
        assertEquals(-Math.PI / 2, Angles.pitchFromVector(new Vector3(0, 1, 0)), 1e-12);
    }

    @Test
    void divMod_floorOperations_handleNegativeValues() {
        // We test mathematical floor division/modulo semantics for negative dividends/divisors.
        assertEquals(-1, DivMod.floorDiv(-1, 2));
        assertEquals(1, DivMod.floorMod(-1, 2));
        assertEquals(-2L, DivMod.floorDiv(-3L, 2L));
        assertEquals(1L, DivMod.floorMod(-3L, 2L));
        assertEquals(-1, DivMod.floorDiv(1, -2));
        assertEquals(-1, DivMod.floorMod(1, -2));
        assertEquals(0, DivMod.floorMod(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    @Test
    void ranges_validateClampIntersectAndExpand() {
        // We test invariants and utility methods for integer and double ranges.
        IntRange i = IntRange.of(10, 3);
        DoubleRange d = DoubleRange.of(5.0, 1.0);

        assertEquals(new IntRange(3, 10), i);
        assertEquals(new DoubleRange(1.0, 5.0), d);
        assertTrue(i.contains(5));
        assertTrue(d.contains(3.0));
        assertEquals(10, i.clamp(100));
        assertEquals(1.0, d.clamp(-5.0), 1e-12);
        assertTrue(i.intersects(new IntRange(10, 12)));
        assertTrue(d.intersects(new DoubleRange(5.0, 8.0)));
        assertEquals(new IntRange(3, 20), i.expandToInclude(20));
        assertEquals(new DoubleRange(-2.0, 5.0), d.expandToInclude(-2.0));
        assertEquals(8L, i.sizeInclusive());
        assertEquals(4.0, d.width(), 1e-12);
    }

    @Test
    void ranges_constructors_rejectInvertedBounds() {
        // We test constructor guards that enforce normalized ranges.
        assertThrows(IllegalArgumentException.class, () -> new IntRange(2, 1));
        assertThrows(IllegalArgumentException.class, () -> new DoubleRange(2.0, 1.0));
    }

    @Test
    void kahanSummation_accumulatesAndResets() {
        // We test running sum accumulation and reset behavior.
        KahanSummation ks = new KahanSummation();
        ks.add(1.0);
        ks.add(2.0);
        ks.add(3.0);
        assertEquals(6.0, ks.value(), 1e-12);

        ks.reset();
        assertEquals(0.0, ks.value(), 1e-12);
    }

    @Test
    void mathUtil_helpers_coverClampingMappingAndHypot() {
        // We test utility functions used by other API layers.
        assertEquals(2.0, MathUtil.clamp(3.0, 0.0, 2.0), 1e-12);
        assertEquals(50.0, MathUtil.mapRange(0.5, 0.0, 1.0, 0.0, 100.0), 1e-12);
        assertEquals(0.25, MathUtil.inverseLerp(0.0, 8.0, 2.0), 1e-12);
        assertEquals(2.5, MathUtil.lerp(0.0, 10.0, 0.25), 1e-12);
        assertTrue(MathUtil.near(1.0, 1.0 + 1e-10, 1e-9));
        assertEquals(13.0, MathUtil.hypot3(3.0, 4.0, 12.0), 1e-12);
    }

    @Test
    void numericTolerance_helpers_behaveAsAbsoluteChecks() {
        // We test centralized tolerance predicates directly.
        assertTrue(NumericTolerance.isZero(1e-13, NumericTolerance.EPS));
        assertFalse(NumericTolerance.isZero(1e-6, NumericTolerance.EPS));
        assertTrue(NumericTolerance.near(10.0, 10.0 + 1e-10, 1e-9));
        assertFalse(NumericTolerance.near(10.0, 10.1, 1e-9));
    }
}
