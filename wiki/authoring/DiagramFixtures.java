import nsk.nu.ashcore.api.collision.CollisionTests;
import nsk.nu.ashcore.api.geometry.Sphere;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.noise.FractalNoise;
import nsk.nu.ashcore.api.noise.PerlinNoise;
import nsk.nu.ashcore.api.random.DeterministicRandoms;
import nsk.nu.ashcore.api.random.LowDiscrepancy;

/** Independent numerical references: call the real library, never a copied formula. */
public class DiagramFixtures {
    private static void row(String type, double... values) {
        StringBuilder line = new StringBuilder(type);
        for (double value : values) line.append('\t').append(value);
        System.out.println(line);
    }

    private static void contact(double x, double y) {
        var a = new Sphere(Vector3.ZERO, 1);
        var b = new Sphere(new Vector3(x, y, 0), 1);
        var c = CollisionTests.sphereVsSphereContact(a, b);
        if (!c.hit()) {
            row("C", x, y, 0, c.depth(), a.center().distance(b.center()));
        } else {
            row("C", x, y, 1, c.depth(), a.center().distance(b.center()),
                c.normal().x(), c.normal().y(), c.pointA().x(), c.pointA().y(),
                c.pointB().x(), c.pointB().y(), c.touching() ? 1 : 0);
        }
    }

    private static void noise(PerlinNoise noise, int seed, double frequency,
                              int octaves, double gain, int x, int z) {
        double value = FractalNoise.fbm(noise, x * frequency, z * frequency, octaves, 2, gain);
        row("N", seed, frequency, octaves, gain, x, z, value, Math.floor(64 + value * 12));
    }

    public static void main(String[] args) {
        // Every selectable sphere position, including near-tangent diagonal cases.
        for (int x = 0; x <= 60; x++) for (int y = -30; y <= 30; y++) contact(x / 20.0, y / 20.0);
        contact(Math.nextDown(2.0), 0);
        contact(Math.nextUp(2.0), 0);
        for (int seed : new int[]{0, 1, 1337, 1338, 999999}) {
            var random = DeterministicRandoms.splitMix64(seed);
            for (int i = 1; i <= 256; i++) {
                double u = random.nextUnitDouble(), v = random.nextUnitDouble();
                var disk = LowDiscrepancy.mapToConcentricDisk(u, v);
                row("R", seed, i, u, v, disk.x(), disk.y());
            }
            var perlin = new PerlinNoise(DeterministicRandoms.splitMix64(seed));
            for (int f = 1; f <= 8; f++) for (int o = 0; o <= 5; o++) for (int g = 0; g <= 20; g++) {
                noise(perlin, seed, f / 200.0, o, g / 20.0, 128, 64);
            }
            // Full mesh at default parameters and the highest frequency/gain.
            if (seed == 1337 || seed == 999999) for (int x = 96; x <= 160; x++) for (int z = 32; z <= 96; z++) {
                noise(perlin, seed, .02, 5, .5, x, z);
                noise(perlin, seed, .04, 5, 1, x, z);
            }
        }
        for (int i = 1; i <= 256; i++) {
            double u = LowDiscrepancy.halton(i, 2), v = LowDiscrepancy.halton(i, 3);
            var disk = LowDiscrepancy.mapToConcentricDisk(u, v);
            row("H", i, u, v, disk.x(), disk.y());
        }
        for (double u : new double[]{0, .25, .5, .75, 1}) for (double v : new double[]{0, .25, .5, .75, 1}) {
            var disk = LowDiscrepancy.mapToConcentricDisk(u, v);
            row("D", u, v, disk.x(), disk.y());
        }
    }
}
