package nsk.nu.ashcore.api.random;

import nsk.nu.ashcore.api.math.Vector2;
import nsk.nu.ashcore.api.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LowDiscrepancyApiTest {

    @Test
    void halton_validatesArguments() {
        // We test explicit guard clauses for invalid index/base.
        assertThrows(IllegalArgumentException.class, () -> LowDiscrepancy.halton(-1, 2));
        assertThrows(IllegalArgumentException.class, () -> LowDiscrepancy.halton(1, 1));
    }

    @Test
    void mapToConcentricDisk_mapsCenterToZero() {
        // We test center point contract of Shirley-Chiu mapping.
        assertEquals(Vector2.ZERO, LowDiscrepancy.mapToConcentricDisk(0.5, 0.5));
    }

    @Test
    void mapToUniformHemisphere_returnsUnitVectorWithNonNegativeY() {
        // We test hemisphere domain constraints and normalization.
        Vector3 d = LowDiscrepancy.mapToUniformHemisphere(0.25, 0.8);
        assertEquals(1.0, d.length(), 1e-9);
        assertTrue(d.y() >= 0.0);
    }

    @Test
    void mapToUniformSphere_returnsUnitVector() {
        // We test unit-sphere surface mapping normalization.
        Vector3 d = LowDiscrepancy.mapToUniformSphere(0.7, 0.1);
        assertEquals(1.0, d.length(), 1e-9);
    }

    @Test
    void mapToUniformSphereVolume_staysInsideUnitSphere() {
        // We test that volume mapping radius never exceeds 1.
        Vector3 p = LowDiscrepancy.mapToUniformSphereVolume(0.1, 0.2, 0.3);
        assertTrue(p.length() <= 1.0 + 1e-9);
    }

    @Test
    void mapToUniformCone_respectsCosThetaMaxRange() {
        // We test cone mapping y component bounds [cosThetaMax, 1].
        double cosThetaMax = 0.4;
        Vector3 d = LowDiscrepancy.mapToUniformCone(0.3, 0.8, cosThetaMax);

        assertTrue(d.y() >= cosThetaMax - 1e-12);
        assertTrue(d.y() <= 1.0 + 1e-12);
        assertEquals(1.0, d.length(), 1e-9);
    }

    @Test
    void orientYUpToNormal_mapsLocalUpOntoTargetNormal() {
        // We test orientation utility: local up should align with provided normal.
        Vector3 n = new Vector3(0, 1, 0);
        Vector3 localUp = new Vector3(0, 1, 0);
        Vector3 world = LowDiscrepancy.orientYUpToNormal(localUp, n);

        assertEquals(n.x(), world.x(), 1e-12);
        assertEquals(n.y(), world.y(), 1e-12);
        assertEquals(n.z(), world.z(), 1e-12);
    }

    @Test
    void orientYUpToNormal_normalizesInputNormalAndRejectsZero() {
        // We test non-unit normal handling and explicit zero-normal guard.
        Vector3 world = LowDiscrepancy.orientYUpToNormal(new Vector3(0, 1, 0), new Vector3(0, 10, 0));
        assertEquals(1.0, world.length(), 1e-12);
        assertEquals(0.0, world.x(), 1e-12);
        assertEquals(1.0, world.y(), 1e-12);
        assertEquals(0.0, world.z(), 1e-12);
        assertThrows(IllegalArgumentException.class,
                () -> LowDiscrepancy.orientYUpToNormal(new Vector3(0, 1, 0), Vector3.ZERO));
    }
}
