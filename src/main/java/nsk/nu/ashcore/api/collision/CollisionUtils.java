package nsk.nu.ashcore.api.collision;

import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Plane;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;

/** Small collision helpers on primitives (no meshes, no grids). */
public final class CollisionUtils {
    private CollisionUtils(){}

    /** Closest point on AABB to p (clamped). */
    public static Vector3 closestPointOnBox(Vector3 p, AxisAlignedBox b){
        double x = Math.min(Math.max(p.x(), b.min().x()), b.max().x());
        double y = Math.min(Math.max(p.y(), b.min().y()), b.max().y());
        double z = Math.min(Math.max(p.z(), b.min().z()), b.max().z());
        return new Vector3(x,y,z);
    }
    /** Squared distance from point to AABB. */
    public static double distanceSqPointBox(Vector3 p, AxisAlignedBox b){
        double dx = outDelta(p.x(), b.min().x(), b.max().x());
        double dy = outDelta(p.y(), b.min().y(), b.max().y());
        double dz = outDelta(p.z(), b.min().z(), b.max().z());
        return dx*dx + dy*dy + dz*dz;
    }
    private static double outDelta(double v, double min, double max){
        if (v < min) return min - v;
        if (v > max) return v - max;
        return 0.0;
    }

    /** Ray vs Plane: returns t, or +INF if parallel or behind. */
    public static double rayVsPlaneT(Ray ray, Plane plane){
        double denom = plane.normal().dot(ray.direction());
        if (Math.abs(denom) < 1e-12) return Double.POSITIVE_INFINITY;
        double t = -(plane.normal().dot(ray.origin()) + plane.d()) / denom;
        return t >= 0 ? t : Double.POSITIVE_INFINITY;
    }
}