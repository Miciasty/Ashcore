package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.geometry.Ray;

/** Continuous collision: moving AABB (vel) vs static AABB. */
public final class SweptAABB {
    public record Result(boolean hit, double t, Vector3 normal){}
    public static Result test(AxisAlignedBox moving, Vector3 vel, AxisAlignedBox box, double tMax){
        Vector3 size = new Vector3(
                moving.max().x() - moving.min().x(),
                moving.max().y() - moving.min().y(),
                moving.max().z() - moving.min().z()
        );

        AxisAlignedBox expanded = new AxisAlignedBox(
                box.min().sub(size.mul(0.5)),
                box.max().add(size.mul(0.5))
        );

        double speed = vel.length();
        if (speed == 0.0) {
            boolean overlapNow =
                    moving.max().x() >= box.min().x() && box.max().x() >= moving.min().x() &&
                            moving.max().y() >= box.min().y() && box.max().y() >= moving.min().y() &&
                            moving.max().z() >= box.min().z() && box.max().z() >= moving.min().z();
            return overlapNow
                    ? new Result(true, 0.0, null)
                    : new Result(false, Double.POSITIVE_INFINITY, null);
        }

        Vector3 origin = moving.min().add(moving.max()).mul(0.5);
        Ray ray = new Ray(origin, vel);
        double distHit = CollisionTests.rayVsBoxT(ray, expanded);
        if (!Double.isFinite(distHit)) return new Result(false, Double.POSITIVE_INFINITY, null);

        double tHit = distHit / speed;
        if (tHit < 0.0 || tHit > tMax) return new Result(false, Double.POSITIVE_INFINITY, null);

        Hit full = CollisionTests.rayVsBoxHit(ray, expanded);
        return new Result(true, tHit, full.normal());
    }
}