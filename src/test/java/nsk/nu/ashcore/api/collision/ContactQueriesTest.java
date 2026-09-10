package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Sphere;
import nsk.nu.ashcore.api.math.Quaternion;
import nsk.nu.ashcore.api.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContactQueriesTest {
    private final Sphere sphere = new Sphere(Vector3.ZERO, 1);
    private final AxisAlignedBox box = new AxisAlignedBox(new Vector3(-1, -2, -3), new Vector3(1, 2, 3));

    @Test
    void spheres_distinguishMissTouchAndPenetrationWithSurfaceWitnesses() {
        for (double distance : new double[]{0.5, 1.5, 2, 2.001, 3}) {
            Sphere other = new Sphere(new Vector3(distance, 0, 0), 1);
            Contact c = CollisionTests.sphereVsSphereContact(sphere, other);
            assertEquals(CollisionTests.sphereVsSphere(sphere, other), c.hit());
            if (c.hit()) {
                assertEquals(2-distance, c.depth(), 1e-12);
                assertEquals(distance == 2, c.touching());
                assertVector(new Vector3(1, 0, 0), c.normal());
                assertOnSphere(c.pointA(), sphere);
                assertOnSphere(c.pointB(), other);
                assertRelation(c);
            } else assertMiss(c);
        }
    }

    @Test
    void sphereContainment_usesSeparationDepthAndExplicitCoincidentTie() {
        Sphere outer = new Sphere(Vector3.ZERO, 5), inner = new Sphere(new Vector3(1, 0, 0), 1);
        Contact c = CollisionTests.sphereVsSphereContact(inner, outer);
        assertEquals(5, c.depth());
        assertVector(new Vector3(-1, 0, 0), c.normal());
        assertOnSphere(c.pointA(), inner);
        assertOnSphere(c.pointB(), outer);
        assertRelation(c);
        for (Sphere contained : new Sphere[]{sphere, new Sphere(Vector3.ZERO, 0)}) {
            Contact tied = CollisionTests.sphereVsSphereContact(contained, outer);
            assertEquals(contained.radius()+5, tied.depth());
            assertVector(new Vector3(1, 0, 0), tied.normal());
            assertVector(new Vector3(1, 0, 0), CollisionTests.sphereVsSphereContact(outer, contained).normal());
            assertOnSphere(tied.pointA(), contained);
            assertOnSphere(tied.pointB(), outer);
            assertRelation(tied);
        }
        Contact points = CollisionTests.sphereVsSphereContact(new Sphere(Vector3.ZERO, 0), new Sphere(Vector3.ZERO, 0));
        assertTrue(points.touching());
        assertRelation(points);
    }

    @Test
    void distinctSphereSwaps_andRigidTransformsPreserveWitnessRelations() {
        Sphere a = new Sphere(new Vector3(1, 2, 3), 2), b = new Sphere(new Vector3(2, 3, 4), 1);
        Contact c = CollisionTests.sphereVsSphereContact(a, b);
        assertReversed(c, CollisionTests.sphereVsSphereContact(b, a));
        Quaternion q = Quaternion.fromAxisAngle(new Vector3(2, -3, 1), 1.1);
        Vector3 translation = new Vector3(-7, 5, 2);
        Contact moved = CollisionTests.sphereVsSphereContact(
                new Sphere(q.rotate(a.center()).add(translation), a.radius()), new Sphere(q.rotate(b.center()).add(translation), b.radius()));
        assertEquals(c.depth(), moved.depth(), 1e-12);
        assertVector(q.rotate(c.normal()), moved.normal());
        assertVector(q.rotate(c.pointA()).add(translation), moved.pointA());
        assertVector(q.rotate(c.pointB()).add(translation), moved.pointB());
        assertRelation(moved);
    }

    @Test
    void sphereBoxOutside_coversFaceEdgeCornerAndTangency() {
        Sphere[] cases = {
                new Sphere(new Vector3(2, 0, 0), 1),
                new Sphere(new Vector3(1.5, 0, 0), 1),
                new Sphere(new Vector3(4, 6, 0), 5),
                new Sphere(new Vector3(3, 4, 4), 3),
                new Sphere(new Vector3(2.001, 0, 0), 1),
                new Sphere(new Vector3(4, 6, 0), 4.9)
        };
        for (Sphere a : cases) {
            Contact c = CollisionTests.sphereVsBoxContact(a, box);
            assertEquals(CollisionTests.sphereVsBox(a, box), c.hit());
            if (c.hit()) {
                assertOnSphere(c.pointA(), a);
                assertOnBox(c.pointB(), box);
                assertRelation(c);
                assertReversed(c, CollisionTests.boxVsSphereContact(box, a));
            } else assertMiss(c);
        }
        assertTrue(CollisionTests.sphereVsBoxContact(cases[0], box).touching());
        assertTrue(CollisionTests.sphereVsBoxContact(cases[2], box).touching());
        assertTrue(CollisionTests.sphereVsBoxContact(cases[3], box).touching());
        assertEquals(0.5, CollisionTests.sphereVsBoxContact(cases[1], box).depth());
    }

    @Test
    void sphereBoxContainment_selectsNearestFaceAndDocumentedTies() {
        for (Sphere a : new Sphere[]{sphere, new Sphere(Vector3.ZERO, 10), new Sphere(Vector3.ZERO, 0), new Sphere(new Vector3(0.8, 0, 0), 0.1)}) {
            Contact c = CollisionTests.sphereVsBoxContact(a, box);
            assertOnSphere(c.pointA(), a);
            assertOnBox(c.pointB(), box);
            assertRelation(c);
            assertReversed(c, CollisionTests.boxVsSphereContact(box, a));
        }
        Contact centered = CollisionTests.sphereVsBoxContact(sphere, box);
        assertVector(new Vector3(1, 0, 0), centered.normal());
        assertVector(new Vector3(-1, 0, 0), centered.pointB());
        assertEquals(2, centered.depth());
        Contact upper = CollisionTests.sphereVsBoxContact(new Sphere(new Vector3(0.8, 0, 0), 0.1), box);
        assertVector(new Vector3(-1, 0, 0), upper.normal());
        assertEquals(0.3, upper.depth(), 1e-12);
        AxisAlignedBox cube = new AxisAlignedBox(new Vector3(-1, -1, -1), new Vector3(1, 1, 1));
        assertVector(new Vector3(1, 0, 0), CollisionTests.sphereVsBoxContact(sphere, cube).normal());
        assertVector(new Vector3(0, -1, 0), CollisionTests.sphereVsBoxContact(new Sphere(new Vector3(0, 0.9, 0), 0.1), cube).normal());
        assertVector(new Vector3(0, 0, 1), CollisionTests.sphereVsBoxContact(new Sphere(new Vector3(0, 0, -0.9), 0.1), cube).normal());
    }

    @Test
    void sphereBoxDegeneracies_coverZeroRadiusBoundaryAndFlatBoxes() {
        Sphere point = new Sphere(new Vector3(1, 0, 0), 0);
        Contact boundary = CollisionTests.sphereVsBoxContact(point, box);
        assertTrue(boundary.touching());
        assertOnSphere(boundary.pointA(), point);
        assertOnBox(boundary.pointB(), box);
        assertRelation(boundary);
        for (AxisAlignedBox degenerate : new AxisAlignedBox[]{
                new AxisAlignedBox(Vector3.ZERO, Vector3.ZERO),
                new AxisAlignedBox(new Vector3(-1, 0, 0), new Vector3(1, 0, 0)),
                new AxisAlignedBox(new Vector3(-1, -1, 0), new Vector3(1, 1, 0))}) {
            Contact c = CollisionTests.sphereVsBoxContact(sphere, degenerate);
            assertEquals(1, c.depth());
            assertOnSphere(c.pointA(), sphere);
            assertOnBox(c.pointB(), degenerate);
            assertRelation(c);
            assertReversed(c, CollisionTests.boxVsSphereContact(degenerate, sphere));
        }
    }

    @Test
    void sphereBoxRigidTransform_preservesUniqueContactWhenBoxStaysAxisAligned() {
        // Exact half turn around Z avoids rounding a 90-degree AABB back into a general OBB.
        Quaternion q = new Quaternion(0, 0, 0, 1);
        Vector3 shift = new Vector3(5, 7, 9);
        Vector3 first = q.rotate(box.min()).add(shift), second = q.rotate(box.max()).add(shift);
        AxisAlignedBox movedBox = new AxisAlignedBox(first.min(second), first.max(second));
        for (Vector3 center : new Vector3[]{new Vector3(1.5, 0.2, 0.3), new Vector3(0.8, 0.1, 0.3)}) {
            Sphere a = new Sphere(center, 1);
            Contact c = CollisionTests.sphereVsBoxContact(a, box);
            Contact moved = CollisionTests.sphereVsBoxContact(new Sphere(q.rotate(center).add(shift), 1), movedBox);
            assertEquals(c.depth(), moved.depth(), 1e-12);
            assertVector(q.rotate(c.normal()), moved.normal());
            assertVector(q.rotate(c.pointA()).add(shift), moved.pointA());
            assertVector(q.rotate(c.pointB()).add(shift), moved.pointB());
        }
    }

    @Test
    void depthMeasuresTranslationToTangencyForSupportedShapes() {
        Sphere a = new Sphere(new Vector3(0.5, 0, 0), 0.25);
        Contact c = CollisionTests.sphereVsBoxContact(a, box);
        Sphere tangent = new Sphere(a.center().sub(c.normal().mul(c.depth())), a.radius());
        assertTrue(CollisionTests.sphereVsBoxContact(tangent, box).touching());
        Sphere separated = new Sphere(tangent.center().sub(c.normal().mul(0.01)), a.radius());
        assertFalse(CollisionTests.sphereVsBox(separated, box));
        Sphere outer = new Sphere(Vector3.ZERO, 2);
        Contact spheres = CollisionTests.sphereVsSphereContact(a, outer);
        Sphere tangentSphere = new Sphere(a.center().sub(spheres.normal().mul(spheres.depth())), a.radius());
        assertTrue(CollisionTests.sphereVsSphereContact(tangentSphere, outer).touching());
    }

    @Test
    void scaledContacts_preserveUnitsWithoutSquaredOverflow() {
        for (double scale : new double[]{1e-140, 1, 1e140, 1e300}) {
            Sphere a = new Sphere(Vector3.ZERO, scale), b = new Sphere(new Vector3(1.5*scale, 0, 0), scale);
            Contact c = CollisionTests.sphereVsSphereContact(a, b);
            assertEquals(0.5, c.depth()/scale, 1e-12);
            assertEquals(1, c.pointA().x()/scale, 1e-12);
            assertEquals(0.5, c.pointB().x()/scale, 1e-12);
            assertVector(new Vector3(1, 0, 0), c.normal());
            Contact boxContact = CollisionTests.sphereVsBoxContact(b,
                    new AxisAlignedBox(new Vector3(-scale, -scale, -scale), new Vector3(scale, scale, scale)));
            assertEquals(0.5, boxContact.depth()/scale, 1e-12);
        }
    }

    @Test
    void contactOverflowAndMalformedRecords_areRejected() {
        Sphere huge = new Sphere(Vector3.ZERO, Double.MAX_VALUE);
        assertTrue(CollisionTests.sphereVsSphere(huge, huge));
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.sphereVsSphereContact(huge, huge));
        Sphere far = new Sphere(new Vector3(Double.MAX_VALUE, 0, 0), Double.MAX_VALUE);
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.sphereVsSphereContact(far, far));
        Vector3 min = new Vector3(-Double.MAX_VALUE, -1, -1), max = new Vector3(Double.MAX_VALUE, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.sphereVsBoxContact(new Sphere(max, 1), new AxisAlignedBox(min, max)));
        assertThrows(IllegalArgumentException.class, () -> CollisionTests.sphereVsBoxContact(sphere,
                new AxisAlignedBox(new Vector3(Double.NaN, 0, 0), Vector3.ZERO)));
        assertThrows(IllegalArgumentException.class, () -> new Contact(-1, Vector3.ZERO, Vector3.ZERO, Vector3.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Contact(0, Vector3.ZERO, Vector3.ZERO, Vector3.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Contact(0, new Vector3(1, 0, 0), new Vector3(Double.NaN, 0, 0), Vector3.ZERO));
        assertThrows(NullPointerException.class, () -> new Contact(0, null, Vector3.ZERO, Vector3.ZERO));
    }

    private static void assertOnSphere(Vector3 point, Sphere sphere) { assertEquals(sphere.radius(), point.distance(sphere.center()), 1e-12); }
    private static void assertOnBox(Vector3 point, AxisAlignedBox box) {
        assertTrue(point.x() >= box.min().x()-1e-12 && point.x() <= box.max().x()+1e-12);
        assertTrue(point.y() >= box.min().y()-1e-12 && point.y() <= box.max().y()+1e-12);
        assertTrue(point.z() >= box.min().z()-1e-12 && point.z() <= box.max().z()+1e-12);
        assertTrue(Math.abs(point.x()-box.min().x()) < 1e-12 || Math.abs(point.x()-box.max().x()) < 1e-12 ||
                Math.abs(point.y()-box.min().y()) < 1e-12 || Math.abs(point.y()-box.max().y()) < 1e-12 ||
                Math.abs(point.z()-box.min().z()) < 1e-12 || Math.abs(point.z()-box.max().z()) < 1e-12);
    }
    private static void assertRelation(Contact c) {
        assertTrue(c.hit());
        assertEquals(1, c.normal().length(), 1e-12);
        assertVector(c.normal().mul(c.depth()), c.pointA().sub(c.pointB()));
    }
    private static void assertReversed(Contact a, Contact b) {
        assertEquals(a.depth(), b.depth(), 1e-12);
        assertVector(a.normal().mul(-1), b.normal());
        assertVector(a.pointA(), b.pointB());
        assertVector(a.pointB(), b.pointA());
    }
    private static void assertMiss(Contact c) {
        assertFalse(c.hit());
        assertFalse(c.touching());
        assertEquals(Double.NEGATIVE_INFINITY, c.depth());
        assertNull(c.normal()); assertNull(c.pointA()); assertNull(c.pointB());
    }
    private static void assertVector(Vector3 expected, Vector3 actual) { assertTrue(expected.distance(actual) < 1e-12, actual.toString()); }
}
