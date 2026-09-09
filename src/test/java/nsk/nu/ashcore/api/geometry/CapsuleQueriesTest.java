package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.math.Quaternion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CapsuleQueriesTest {

    @Test
    void distanceSq_measuresDistanceToSolidCapsule() {
        Capsule capsule = new Capsule(Vector3.ZERO, new Vector3(0, 2, 0), 1);
        assertEquals(0.25, capsule.distanceSqTo(new Vector3(1.5, 1, 0)), 1e-12);
        assertEquals(0.25, capsule.distanceSqTo(new Vector3(0, -1.5, 0)), 1e-12);
        assertEquals(0, capsule.distanceSqTo(new Vector3(0.5, 1, 0)));
        Capsule sphere = new Capsule(Vector3.ZERO, Vector3.ZERO, 1);
        assertEquals(0.25, sphere.distanceSqTo(new Vector3(1.5, 0, 0)), 1e-12);
    }

    @Test
    void rayIntersect_handlesAxisEndCapsAndInsideStarts() {
        Capsule capsule = new Capsule(Vector3.ZERO, new Vector3(0, 2, 0), 1);
        assertEquals(1, capsule.rayIntersectT(new Ray(new Vector3(0, -2, 0), new Vector3(0, 1, 0))), 1e-12);
        assertEquals(1, capsule.rayIntersectT(new Ray(new Vector3(0, 4, 0), new Vector3(0, -1, 0))), 1e-12);
        assertEquals(0, capsule.rayIntersectT(new Ray(new Vector3(0, 1, 0), new Vector3(1, 0, 0))));
        assertEquals(Double.POSITIVE_INFINITY,
                capsule.rayIntersectT(new Ray(new Vector3(0, -2, 0), new Vector3(0, -1, 0))));
    }

    @Test
    void rayIntersect_doesNotClampHitsBehindDegenerateCapsuleToZero() {
        Capsule capsule = new Capsule(Vector3.ZERO, Vector3.ZERO, 1);
        assertEquals(Double.POSITIVE_INFINITY,
                capsule.rayIntersectT(new Ray(new Vector3(2, 0, 0), new Vector3(1, 0, 0))));
    }

    @Test
    void capsule_queriesHandleSidesTangencyScalingAndReversedEndpoints() {
        for (double scale : new double[]{1e-150, 1, 1e150}) {
            Vector3 end = new Vector3(0, 2*scale, 0);
            Capsule capsule = new Capsule(Vector3.ZERO, end, scale);
            Capsule reversed = new Capsule(end, Vector3.ZERO, scale);
            Ray ray = new Ray(new Vector3(-2*scale, scale, 0), new Vector3(1, 0, 0));
            assertEquals(1, capsule.rayIntersectT(ray) / scale, 1e-12);
            assertEquals(capsule.rayIntersectT(ray) / scale, reversed.rayIntersectT(ray) / scale, 1e-12);
        }
        Capsule capsule = new Capsule(Vector3.ZERO, new Vector3(0, 2, 0), 1);
        assertEquals(2, capsule.rayIntersectT(new Ray(new Vector3(-2, 1, 1), new Vector3(1, 0, 0))), 1e-12);
        assertEquals(Double.POSITIVE_INFINITY,
                capsule.rayIntersectT(new Ray(new Vector3(-2, 1, 1.1), new Vector3(1, 0, 0))));
        Capsule segment = new Capsule(Vector3.ZERO, new Vector3(0, 2, 0), 0);
        assertEquals(2, segment.rayIntersectT(new Ray(new Vector3(-2, 1, 0), new Vector3(1, 0, 0))), 1e-12);
    }

    @Test
    void capsule_rejectsInvalidShapesAndPoints() {
        for (double radius : new double[]{-1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new Capsule(Vector3.ZERO, Vector3.ZERO, radius));
        }
        assertThrows(IllegalArgumentException.class,
                () -> new Capsule(new Vector3(Double.NaN, 0, 0), Vector3.ZERO, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Capsule(Vector3.ZERO, Vector3.ZERO, 1).distanceSqTo(new Vector3(0, Double.POSITIVE_INFINITY, 0)));
    }

    @Test
    void capsule_queriesPreserveResultsAfterRotationAndTranslation() {
        Quaternion rotation = Quaternion.fromAxisAngle(new Vector3(1, 2, 3), 1.2);
        Vector3 translation = new Vector3(7, -3, 11);
        Capsule capsule = new Capsule(translation, rotation.rotate(new Vector3(0, 2, 0)).add(translation), 1);
        for (Ray ray : new Ray[]{
                new Ray(new Vector3(-2, 1, 0), new Vector3(1, 0, 0)),
                new Ray(new Vector3(0, -2, 0), new Vector3(0, 1, 0))}) {
            Ray transformed = new Ray(rotation.rotate(ray.origin()).add(translation), rotation.rotate(ray.direction()));
            assertEquals(1, capsule.rayIntersectT(transformed), 1e-12);
        }
        Vector3 point = rotation.rotate(new Vector3(1.5, 1, 0)).add(translation);
        assertEquals(0.25, capsule.distanceSqTo(point), 1e-12);
    }
}
