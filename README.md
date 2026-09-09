# Ashcore

Ashcore provides Java math, primitive geometry, sampling, noise, hashing and streaming statistics for engines and plugins.
It is the lowest Blackframe layer and has no production dependencies outside the Java standard library.

Use it to generate terrain samples, test a player's ray against a box, or track an average without retaining every measurement.
A box hit measures contact with that box. If the box encloses a more detailed object, the result is only a candidate for that object's hit.
Ashcore does not compute forces, bouncing or collision response. Voxel storage belongs to Ashgrid, coordinate-frame graphs to Ashspace,
and navigation and movement rules belong to higher layers or the caller.

## Requirements and quick start

Requires **JDK 21+** and **Maven 3.9+** to build. This checkout is **1.1.0-SNAPSHOT**, a development version.
Publication of these coordinates is **not verified**. To use this checkout locally, run `mvn -B clean verify` and then `mvn -B install`.
These commands do not upload artifacts. See [release and verification evidence](docs/RELEASE.md) before choosing a released dependency.

```xml
<dependency>
  <groupId>dev.nasaka.blackframe</groupId>
  <artifactId>ashcore</artifactId>
  <version>1.1.0-SNAPSHOT</version>
</dependency>
```

Java packages remain `nsk.nu.ashcore.*`. The example below is compiled and run against the main JAR during `verify`.
A ray starting two units before the box hits at distance `2`. The terrain value sums five noise layers and is not a block height
until the caller chooses a height scale and rounding rule.
The sphere is hit one unit ahead; the segment reaches the box halfway through its length.
The rotation turns the ray direction by 90 degrees around Y, and the bounded integer draw models a six-sided die.

```java
import nsk.nu.ashcore.api.collision.CollisionTests;
import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.geometry.Segment3;
import nsk.nu.ashcore.api.geometry.Sphere;
import nsk.nu.ashcore.api.math.Quaternion;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.noise.FractalNoise;
import nsk.nu.ashcore.api.noise.PerlinNoise;
import nsk.nu.ashcore.api.random.DeterministicRandom;
import nsk.nu.ashcore.api.random.DeterministicRandoms;
import nsk.nu.ashcore.api.stats.RunningStats;

public final class AshcoreQuickStart {
    public static void main(String[] args) {
        DeterministicRandom rng = DeterministicRandoms.splitMix64(1337L);
        PerlinNoise perlin = new PerlinNoise(rng);
        double height = FractalNoise.fbm(perlin, 128.0 * 0.02, 64.0 * 0.02, 5, 2.0, 0.5);

        Ray ray = new Ray(new Vector3(-2, 0, 0), new Vector3(1, 0, 0));
        AxisAlignedBox box = new AxisAlignedBox(new Vector3(0, -1, -1), new Vector3(1, 1, 1));
        double t = CollisionTests.rayVsBoxT(ray, box);

        double sphereT = CollisionTests.rayVsSphereT(ray, new Sphere(Vector3.ZERO, 1));
        Segment3 segment = new Segment3(new Vector3(-2, 0, 0), new Vector3(2, 0, 0));
        double fraction = CollisionTests.segmentVsBoxT(segment, box);
        Quaternion rotation = Quaternion.fromAxisAngle(new Vector3(0, 1, 0), Math.PI / 2);
        Vector3 turned = rotation.toMatrix3().mul(ray.direction());
        int roll = rng.nextInt(1, 7);

        RunningStats stats = new RunningStats();
        stats.add(height);
        System.out.printf("height=%.3f, rayHitT=%.3f, mean=%.3f%n", height, t, stats.mean());
        System.out.printf("sphereHitT=%.3f, segmentFraction=%.3f, roll=%d, turned=%s%n", sphereT, fraction, roll, turned);
    }
}
```

## Math and geometric results

Vectors contain numbers without a built-in physical unit. Combine positions, bounds and translations in the same unit.
`Matrix4` acts on column vectors: `a.mul(b)` applies `b` first. Use homogeneous `w=1` for a point and `w=0` for a direction;
`mul(x,y,z,w)` returns a new array without perspective division. Quaternion angles are radians and use the right-hand rule.
`Quaternion.rotate` and `slerp` require unit quaternions; `slerp` expects `t` in `[0,1]`.
`Quaternion.conjugate()` negates the three imaginary components and reverses a unit rotation.
`inverse()` also handles non-unit magnitudes; zero has no inverse and throws `ArithmeticException`.
`toMatrix3/toMatrix4` normalize the quaternion and produce a rotation matrix (zero maps to identity).
`fromMatrix3/fromMatrix4` accept proper rotations with absolute `1e-9` checks on column squared lengths,
dot products and determinant; scale, shear and reflections outside that tolerance are rejected.
The 4x4 conversion requires an affine last row within `1e-9` and ignores finite translation.
Round trips preserve the rotation within rounding, not necessarily the quaternion's sign.

`Vector2/3/4.normalized()` support every finite non-zero vector, including subnormal and very large components, within double rounding.
A zero vector stays zero; a zero quaternion still normalizes to identity. A ray direction and an axis passed to `fromAxisAngle`
must be non-zero. These normalization operations reject non-finite inputs. A `Ray` also requires a finite origin.
A `Plane` scales both its normal and offset; an unrepresentable normalized offset is rejected.
All three vector dimensions provide length, squared length, distance, squared distance and interpolation.
`Vector3` also provides division, component-wise min/max and `withX/withY/withZ` copies.
Squared quantities can underflow/overflow even when their unsquared counterparts remain representable;
interpolation uses `this + (to - this) * t` without clamping and requires representable intermediates.

| Operation | Meaning and boundaries |
| --- | --- |
| `Ray.at(t)` | `origin + unitDirection * t`; t is distance. No clamping: negative t extends behind the origin. |
| `AxisAlignedBox` / `contains` | Ordered corners, inclusive faces/edges/corners. Zero extent is valid. Legacy non-finite corners remain constructible for adapters; `contains` returns false for a non-finite box or point, and ray/box collision rejects non-finite bounds. |
| `CollisionTests.rayVsBoxT` | First box contact distance; zero for an origin inside/on the box, positive infinity for a miss. Only exactly zero direction components are parallel. |
| `CollisionTests.rayVsBoxHit` | Same distance plus point/normal. Inside starts return the origin and an exit-face normal, even though that point is not on the exit face. Ties use X, then Y, then Z. Miss point/normal are null. |
| `CollisionTests.rayVsSphereT` | First sphere contact distance, including tangency; zero inside/on the sphere, positive infinity for a miss or a sphere behind the ray. |
| `CollisionTests.sphereVsSphere/sphereVsBox` | Whether the supplied closed shapes overlap or touch; zero-radius spheres are points. |
| `CollisionTests.segmentVsBoxT` | First contact as a fraction in `[0,1]`, not distance. Both endpoints count; a zero-length segment is a point query. Zero inside/on the box, positive infinity on a miss. |
| `Capsule.distanceSqTo/rayIntersectT` | Squared distance to the solid capsule, zero inside; first ray contact distance including spherical ends. Equal endpoints give a sphere and zero radius gives a segment. |
| `CollisionUtils.rayVsPlaneT` | Forward plane distance; positive infinity for a behind/parallel/coplanar query. Absolute normal/direction dot product below `1e-12` is treated as parallel and can omit distant shallow-angle hits. |
| `SweptAABB.test` | First contact during a straight translation against a stationary box, including `tMax`. `vel` is position units per time unit and returned t is time. With displacement as vel and `tMax=1`, t is a motion fraction. |

Sweep requires finite velocity and finite non-negative `tMax`, with representable bound differences. Resting overlap returns `t=0`
and a null normal; moving overlap uses an exit-face normal. No rotation, deformation or acceleration is modeled.
The tests describe primitive shapes, not mesh intersections or physical simulation.
`Sphere` and `Capsule` require finite centers/endpoints and finite non-negative radii.
New primitive queries add no contact epsilon. Relevant coordinate differences, capsule axis lengths/projections,
slab ratios and hit times must be representable; unsupported non-finite inputs/differences are rejected where documented.
Scaling prevents squared-coordinate overflow in sphere queries, but cannot preserve tiny features at arbitrary relative scales.

Double arithmetic can overflow or lose precision in intermediate differences, dot products, squared lengths and repeated transforms.
Robust normalization does not make all other vector operations robust over the entire double range.
Ray/box distances must be representable; extreme coordinates need application-level scaling.
Matrix inversion rejects non-finite entries, and non-finite determinants or absolute determinants below `1e-12`.
This legacy determinant cutoff is scale-dependent, not a relative error bound or a guarantee of good conditioning.
`inverseAffine` accepts last-row deviations up to `1e-12` and discards them.

`NumericTolerance.isZero/near` use a caller-selected absolute epsilon in the tested quantity's units;
negative, NaN and infinite epsilons are rejected. Non-finite compared values return false.
The constants serve specific existing tests: determinant units for inversion, dimensionless dot products for ray/plane parallelism,
and radians for quaternion interpolation. They are not a universal geometry tolerance and do not define grid-cell membership.

## Repeatability, state and approximation

Repeatability requires the same algorithm/version, inputs, initial state and ordered sequence of calls. It does not establish accuracy.
Do not concurrently mutate RNGs, incremental sequences, samplers or statistics; external synchronization must also establish repeatable call order.
Immutable values and a fully constructed `PerlinNoise` can be shared. Caller-owned arrays must stay stable during reads/builds;
callbacks used by noise combinations must themselves be repeatable.

| Family | Guarantee and limitations |
| --- | --- |
| SplitMix64 | `splitMix64` and the default generator use `random:splitmix64` throughout 1.x. The seeded `nextLong` stream and default `nextInt/nextUnitDouble` conversions are stable in 1.x. Each consumes one 64-bit draw. Integer arithmetic gives the same bits on conforming Java 21+ runtimes; fixed vectors are tested. This is not a security RNG. |
| `Hash64`, `SeedSequence` | FNV-1a hashes bytes or UTF-8 text; `mix64` uses intentional long overflow. These outputs and `mix64(root XOR fnv1a(tag))` derivation are stable in 1.x. Tags are not text-normalized, collision-free or guaranteed statistically independent. |
| Perlin / hash-grid noise | Same table/seed, coordinates, calls, version and environment repeat results. Perlin construction consumes 255 uniform-double draws; sampling does not use or retain the RNG. Perlin's lattice repeats every 256 units. No bitwise cross-release/platform promise for noise. |
| Math, geometry, distributions | Repeatability is scoped to one library version/environment and valid ordered inputs. Some `Math` functions allow implementation variation. Floating-point results and approximations may change in bug-fix releases. |
| Statistics and sampling | Sample order, RNG state, weight/item order and resets are part of the input. Floating-point rounding is order-sensitive. Fixed-seed weighted/reservoir/distribution output is not a promised cross-release stream. |
| SPI | Select providers by ID. Registry enumeration and provider initialization order are unspecified. |

`DeterministicRandom` adds `nextInt(bound)`, `nextLong(bound)` and `(origin,bound)` overloads, with an inclusive
lower bound and exclusive upper bound. Bounds must define a non-empty interval; invalid bounds consume no state.
The overloads support the signed int/long domains, including intervals whose width overflows that type.
Under uniform source bits, rejection of incomplete buckets avoids modulo bias. The methods take expected O(1) time
and O(1) extra memory, but may consume more than one draw; even a singleton interval consumes one.
A pathological custom generator that repeatedly produces rejected values may not terminate.
The default reduction and fixed-seed results are stable from 1.1 through the rest of 1.x; existing unbounded streams are unchanged.

Perlin accepts dimensionless lattice coordinates in `[-2^31,2^31)`; hash-grid 3D noise requires
`[Integer.MIN_VALUE,Integer.MAX_VALUE)` to leave room for upper corners. Out-of-range/non-finite coordinates are rejected.
Perlin values are approximately in `[-1,1]`. Fractal noise sums layers without normalization; all octave coordinates must stay in the
base noise's domain. Non-positive octave counts produce zero. A seed alone does not preserve terrain after an algorithm or call-order change.

`WeightedSampler` builds its own tables from finite non-negative weights with at least one positive entry; later array edits have no effect.
Probabilities approximate weight ratios in double precision; tiny relative weights can underflow.
For one-off draws, `WeightedPicker` uses O(n) time/O(1) extra memory and requires a finite positive weight sum.
It uses half-open selection intervals and never selects a zero-weight entry, including boundary/rounding cases.
Halton sequences offer more even coverage than independent random points, but incremental rounding can drift from direct evaluation.
A single base need only exceed one; use distinct prime bases for multiple dimensions. The incremental counter stops at `Integer.MAX_VALUE` until reset.

`RunningStats` computes mean and population/sample variance with constant state; no-data results are NaN and sample variance needs two samples.
Samples must be finite and intermediate arithmetic representable. Count overflow is rejected at `Long.MAX_VALUE`; double count arithmetic loses
integer precision above `2^53`. Variance has squared sample units.
`P2Quantile` keeps five markers to estimate one percentile. It returns NaN before five samples, depends on order, has no general error bound,
and stops at `Integer.MAX_VALUE` samples until reset. It is not an exact sorted-history percentile.
`ReservoirSampler` keeps at most k items; scaling a finite-resolution random double makes inclusion probabilities approximate.
It rejects long counter overflow. Its snapshots copy slot references; items themselves are not copied. Reset does not reset its RNG.

## Service discovery

`ServiceRegistry.of` eagerly instantiates providers through `ServiceLoader` and calls their `id()` methods.
This may execute constructors and static initializers; a failure does not undo their effects.
It uses the thread context loader, falling back to the service type's loader and then the system loader; an explicit-loader overload is available.
IDs must be valid and unique. Duplicates fail with `IllegalStateException`; invalid IDs fail with `IllegalArgumentException`.
`get(id)` returns an empty Optional for absence; `require(id)` fails. ServiceLoader failures propagate.
Collections are immutable, but provider objects may be mutable. Ashcore ships the registry utility, with no built-in SPI provider registrations.

## Operation costs

Here n is the number of weights, i the Halton index, b its fixed base, o the non-negative octave count,
C the cost of one noise callback, k the reservoir capacity and p the number of discovered providers.
Memory below is additional state or working storage, excluding inputs and callback/provider internals.

| Operation | Time | Memory and practical meaning |
| --- | --- | --- |
| Fixed-size vector/matrix/quaternion math | O(1) | O(1); immutable results/arrays may allocate. |
| Ray/box and box sweep | O(1) | O(1); result records and vectors may allocate. |
| Ray/sphere, sphere overlap, segment/box, capsule queries | O(1) | O(1); temporary vectors/shapes may allocate. |
| Quaternion inverse and matrix conversions | O(1) | O(1); fixed-size results may allocate. |
| Bounded integer RNG | Expected O(1) | O(1); rejection has no finite worst-case draw count. |
| `HaltonSequence.next` | O(1) amortized over consecutive calls; O(log_b(i+1)) worst case | O(log_b(i+1)) retained digits. Reset releases logical state; list capacity can remain. |
| `WeightedSampler.build` / `sampleIndex` | O(n) build / O(1) plus RNG cost per draw | O(n) tables and temporary queues. Twice as many weights need roughly twice the table storage. |
| Perlin construction / sample | O(1) | Fixed permutation table and 255 construction draws / fixed sample work. |
| Fractal noise | O(o*C) | O(1) own state; doubling octaves doubles callback count. |
| `RunningStats.add`, `P2Quantile.add` | O(1) | O(1); P² bootstrap sorts only five values. |
| `ReservoirSampler.offer` / `snapshot` | O(1) plus RNG cost / O(k) worst case | O(k) retained slots / O(k) snapshot copy. |
| `Hash64.fnv1a` | O(n) for n input bytes | O(1) for byte input; O(n) temporary UTF-8 bytes for strings. |
| `ServiceRegistry.of` / `get` | Expected O(p) map work plus discovery/loading/provider cost / expected O(1) lookup | O(p) retained entries; no bound on provider initialization cost. IDs must be fixed-length for the lookup bound. |

Big-O describes growth, not latency. No benchmark-based speed or allocation-free claim is made here.
See the Javadoc and tests for operation-specific requirements beyond these common paths.

## Supported API and compatibility

All public types/members under `nsk.nu.ashcore.api` and the public `implementation.random.SplitMix64Random` class/constructor
are supported. The package name `implementation` does not remove existing supported usage. Prefer factories for new RNG callers.
Private implementation details and test fixtures are unsupported. There is no public serialization/wire format guarantee.

Releases follow Semantic Versioning. Source and binary signatures are retained by the 1.0.2 corrections; zero vector/quaternion behavior remains.
The 1.1.0 extension adds methods to existing classes and default methods to the RNG interface, requiring no new methods
in existing RNG implementations. It also fixes Vector2/4 normalization, capsule distances/intersections and zero-weight selection.
Sphere/Capsule constructors now reject invalid shape data; the previously constructible invalid values have no geometric interpretation.
Corrections change extreme normalization, shallow-angle box hits, sweep arithmetic, P² estimates and weighted tables, and reject specified invalid inputs.
Clients must not rely on old erroneous results. The explicitly stable SplitMix64/hash/seed contracts require a major version or a separately named
algorithm to change. Other floating-point and approximate results can change with documented fixes; pin the artifact version for stored procedural output.
See [migration and consumer checks](docs/RELEASE.md) and the [maintenance evidence](ISSUES.md).

## Terms

- **AABB:** a box whose sides follow the coordinate axes; a cheap hit volume.
- **Unit vector:** a direction with length one, allowing ray t to measure distance.
- **Quaternion:** four numbers used to compose rotations without storing angles around each axis.
- **Affine transform:** a matrix combining a linear transform with translation.
- **Deterministic:** repeatable under the specified algorithm, state, input order and environment.
- **Seed:** initial generator state; later draws advance that state.
- **Octave:** one scaled layer of noise added to a sum.
- **Online statistic:** a value updated as samples arrive without keeping the full history.
- **Quantile:** a cutoff below which a chosen fraction of samples lies; the median is the 50th percentile.
- **SPI provider:** an implementation discovered from service registration resources and chosen by ID.

## License

Apache-2.0 Copyright 2025 Mateusz Aftanas
