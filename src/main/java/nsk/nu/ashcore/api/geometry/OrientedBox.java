package nsk.nu.ashcore.api.geometry;

import nsk.nu.ashcore.api.math.Quaternion;
import nsk.nu.ashcore.api.math.Vector3;

/**
 * Closed oriented box: center + orientation.rotate(local), where each local component lies between
 * minus and plus its half extent. Center and half extents share position units; orientation maps local
 * axes to the query coordinate system. Zero extents represent a rectangle, segment or point.
 * Finite non-zero quaternions are normalized; zero is rejected here because it does not specify an orientation.
 * Non-finite components or negative extents throw IllegalArgumentException; null values throw NullPointerException.
 * Immutable, with O(1) construction time and storage. Queries are provided by CollisionTests.
 */
public record OrientedBox(Vector3 center, Vector3 halfExtents, Quaternion orientation) {
    public OrientedBox {
        if (!Double.isFinite(center.x()) || !Double.isFinite(center.y()) || !Double.isFinite(center.z())) {
            throw new IllegalArgumentException("Center must be finite");
        }
        if (!Double.isFinite(halfExtents.x()) || !Double.isFinite(halfExtents.y()) || !Double.isFinite(halfExtents.z()) ||
                halfExtents.x() < 0 || halfExtents.y() < 0 || halfExtents.z() < 0) {
            throw new IllegalArgumentException("Half extents must be finite and non-negative");
        }
        if (orientation.w() == 0 && orientation.x() == 0 && orientation.y() == 0 && orientation.z() == 0) {
            throw new IllegalArgumentException("Orientation must be non-zero");
        }
        orientation = orientation.normalized();
    }
}
