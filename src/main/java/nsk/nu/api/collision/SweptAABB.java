package nsk.nu.api.collision;

import nsk.nu.api.geometry.AxisAlignedBox;
import nsk.nu.api.math.Vector3;

/** Continuous collision: moving AABB (vel) vs static AABB. */
public final class SweptAABB {
    public record Result(boolean hit, double t, Vector3 normal){}
    public static Result test(AxisAlignedBox moving, Vector3 vel, AxisAlignedBox box, double tMax){
        Vector3 size = new Vector3(moving.max().x()-moving.min().x(),
                moving.max().y()-moving.min().y(),
                moving.max().z()-moving.min().z());
        AxisAlignedBox expanded = new AxisAlignedBox(
                box.min().sub(size.mul(0.5)), box.max().add(size.mul(0.5)));

        Vector3 origin = moving.min().add(moving.max()).mul(0.5);
        var ray = new nsk.nu.api.geometry.Ray(origin, vel.length()==0? new Vector3(1,0,0): vel);
        double tHit = nsk.nu.api.collision.CollisionTests.rayVsBoxT(ray, expanded);
        if (!Double.isFinite(tHit) || tHit > tMax) return new Result(false, Double.POSITIVE_INFINITY, null);
        var full = nsk.nu.api.collision.CollisionTests.rayVsBoxHit(ray, expanded);
        return new Result(true, tHit, full.normal());
    }
}