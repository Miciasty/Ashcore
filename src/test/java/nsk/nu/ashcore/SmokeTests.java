package nsk.nu.ashcore;

import nsk.nu.ashcore.api.collision.CollisionTests;
import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmokeTests {

    @Test
    void ray_vs_box_t_works() {
        var ray = new Ray(new Vector3(-2, 0, 0), new Vector3(1, 0, 0));
        var box = new AxisAlignedBox(new Vector3(0, -1, -1), new Vector3(1, 1, 1));
        double t = CollisionTests.rayVsBoxT(ray, box);
        assertEquals(2.0, t, 1e-9);
    }

}