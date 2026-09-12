(function () {
  'use strict';

  const { code, table, note } = window.WIKI_HTML;
  const sourceRoot = 'https://github.com/Miciasty/Ashcore/blob/v1.2.0/src/main/java/nsk/nu/ashcore/';
  const typeLink = (packagePath, name) => {
    const file = name.split('.')[0];
    return `<a href="${sourceRoot}${packagePath}/${file}.java"><code>${name}</code></a>`;
  };
  const directory = (packagePath, entries) => table(
    ['Type', 'Purpose', 'Guide'],
    entries.map(([name, purpose, guide, label]) => [typeLink(packagePath, name), purpose, `<a href="#/${guide}">${label}</a>`])
  );

  window.WIKI_PAGES.push(
    {
      id: 'api-index',
      category: 'Reference',
      title: 'API index',
      description: 'Find every supported public type in Ashcore 1.2.0 and the guide for its behavior.',
      kind: 'reference',
      readingTime: 9,
      intro: '<p>Java imports begin with <code>nsk.nu.ashcore</code>. The Maven coordinates are <code>dev.nasaka.blackframe:ashcore:1.2.0</code>. Each type link opens its source and Javadoc comments at the <code>v1.2.0</code> release tag.</p>',
      sections: [
        {
          id: 'supported-api',
          title: 'Supported API',
          html: '<p>All public types and members beneath <code>nsk.nu.ashcore.api</code> are supported. The public <code>implementation.random.SplitMix64Random</code> class and constructor are supported too. Use <code>DeterministicRandoms.splitMix64(seed)</code> for new callers.</p><p>Private members and test fixtures are outside this contract. Ashcore does not promise a public serialization or wire format. Source and binary compatibility do not guarantee identical floating-point results after a documented correction; see <a href="#/migration">Migration</a>.</p>'
        },
        {
          id: 'math-package',
          title: 'Math',
          html: '<p><code>nsk.nu.ashcore.api.math</code> provides scalar operations, immutable vectors, matrices, rotations, and a mutable compensated sum.</p>' + directory('api/math', [
            ['Vector2', 'Immutable two-component double vector for continuous positions, directions, and offsets.', 'math', 'Vectors'],
            ['Vector3', 'Immutable three-component double vector with dot/cross products, normalization, and component operations.', 'math', 'Vectors'],
            ['Vector4', 'Immutable four-component double vector, including homogeneous coordinates.', 'math', 'Vectors'],
            ['Vector2i', 'Immutable two-component integer coordinates and arithmetic.', 'math', 'Vectors'],
            ['Vector3i', 'Immutable three-component integer coordinates and arithmetic.', 'math', 'Vectors'],
            ['Vector4i', 'Immutable four-component integer coordinates and arithmetic.', 'math', 'Vectors'],
            ['Matrix2', 'Immutable row-major 2 × 2 matrix with multiplication, determinant, and inverse.', 'transforms', 'Transforms'],
            ['Matrix3', 'Immutable row-major 3 × 3 matrix with multiplication, determinant, and inverse.', 'transforms', 'Transforms'],
            ['Matrix4', 'Immutable row-major 4 × 4 matrix acting on column vectors; supports affine transforms and inversion.', 'transforms', 'Transforms'],
            ['Quaternion', 'Immutable rotation representation, composition, interpolation, inverse, and matrix conversions. Rotation angles use radians.', 'transforms', 'Transforms'],
            ['Angles', 'Angle wrapping, shortest-arc interpolation, angular deltas, and yaw/pitch conversions.', 'math', 'Angles'],
            ['MathUtil', 'Scalar clamping, range mapping, interpolation, near-equality, and three-component hypotenuse.', 'math', 'Scalar math'],
            ['NumericTolerance', 'Absolute tolerance constants and validated near-equality/near-zero checks.', 'math', 'Numeric limits'],
            ['DivMod', 'Mathematical floor division and floor modulo for integers and longs.', 'math', 'Scalar math'],
            ['IntRange', 'Immutable inclusive integer range with ordered endpoints.', 'math', 'Ranges'],
            ['DoubleRange', 'Inclusive double-range operations; validate finite endpoints and query values in the caller.', 'math', 'Ranges'],
            ['KahanSummation', 'Mutable compensated running sum with a reset operation.', 'math', 'Scalar math']
          ])
        },
        {
          id: 'geometry-package',
          title: 'Geometry',
          html: '<p><code>nsk.nu.ashcore.api.geometry</code> defines shapes and coordinate helpers. Choose the position unit in your application and use it consistently.</p>' + directory('api/geometry', [
            ['AxisAlignedBox', 'Closed 3D box defined by inclusive minimum and maximum corners.', 'geometry', 'Shapes'],
            ['AxisAlignedRect', 'Closed 2D rectangle defined by inclusive minimum and maximum corners.', 'geometry', 'Shapes'],
            ['OrientedBox', 'Closed box with a center, non-negative half extents, and a normalized local-to-query rotation.', 'geometry', 'Shapes'],
            ['Sphere', 'Closed sphere with a finite center and finite non-negative radius.', 'geometry', 'Shapes'],
            ['Capsule', 'Closed segment expanded by a radius, including spherical ends; also supplies distance and ray queries.', 'geometry', 'Shapes'],
            ['Segment3', 'Closed segment between two endpoints; its interpolation parameter is a fraction.', 'geometry', 'Shapes'],
            ['Ray', 'Finite origin and normalized non-zero direction; its parameter measures distance.', 'raycasting', 'Raycasting'],
            ['Plane', 'Plane equation <code>normal · point + d = 0</code>, with the normal and offset normalized together.', 'geometry', 'Shapes'],
            ['Boxes', 'AABB union, expansion, and inclusive overlap helpers.', 'geometry', 'Shapes'],
            ['GeometryUtils', 'Vector reflection, projection/rejection, and the closest point on a segment.', 'geometry', 'Geometry helpers'],
            ['OrthonormalBasis', 'Tangent, normal, and bitangent basis for mapping local Y-up samples to a supplied normal.', 'geometry', 'Geometry helpers']
          ])
        },
        {
          id: 'collision-package',
          title: 'Collision queries',
          html: '<p><code>nsk.nu.ashcore.api.collision</code> tests the supplied primitive shapes. A result contains geometric information; it does not move objects or calculate a physical response.</p>' + directory('api/collision', [
            ['CollisionTests', 'Primitive ray/segment queries, static overlaps, oriented-box intervals, and sphere contact witnesses.', 'collisions', 'Collision queries'],
            ['CollisionUtils', 'Closest point and squared point distance for an AABB, plus forward ray/plane distance.', 'raycasting', 'Raycasting'],
            ['Hit', 'Ray query result with <code>t</code>, point, normal, and a finite-<code>t</code> hit check.', 'raycasting', 'Hit results'],
            ['IntersectionInterval', 'Closed entry/exit interval; query methods define distance or fraction units and clipping.', 'raycasting', 'Entry and exit'],
            ['Contact', 'Ordered shape contact with depth, unit normal, and a surface witness on each shape.', 'collisions', 'Contact witnesses'],
            ['SweptAABB', 'First contact of a translating AABB against a stationary AABB over a closed time interval.', 'collisions', 'Swept boxes'],
            ['SweptAABB.Result', 'Sweep hit flag, time parameter, and available face normal.', 'collisions', 'Swept boxes']
          ])
        },
        {
          id: 'random-package',
          title: 'Random generation and sampling',
          html: '<p><code>nsk.nu.ashcore.api.random</code> provides generators, seed derivation, distributions, weighted choices, and low-discrepancy samples.</p>' + directory('api/random', [
            ['DeterministicRandom', 'Identified generator interface with unbounded, bounded integer, and unit-double draws.', 'random', 'Random generation'],
            ['DeterministicRandoms', 'Factory for a SplitMix64 generator initialized from a long seed.', 'random', 'Random generation'],
            ['SeedSequence', 'Stateless tagged seed derivation from one root seed.', 'random', 'Seed streams'],
            ['Distributions', 'Gaussian, exponential, and Poisson draws using a supplied generator.', 'random', 'Distributions'],
            ['WeightedPicker', 'One-off weighted index or list-item selection with a linear scan.', 'random', 'Weighted choices'],
            ['WeightedSampler', 'Reusable alias tables: linear construction and constant-time index sampling, plus generator cost.', 'random', 'Weighted choices'],
            ['Permutation', 'In-place shuffle of an integer array using a supplied generator.', 'random', 'Sampling'],
            ['LowDiscrepancy', 'Direct Halton evaluation and mappings to disks, hemispheres, spheres, sphere volumes, and cones.', 'random', 'Low-discrepancy samples'],
            ['HaltonSequence', 'Mutable incremental one-dimensional Halton sequence with a configurable base.', 'random', 'Low-discrepancy samples'],
            ['Halton2DSequence', 'Mutable incremental two-dimensional Halton sequence; default bases are 2 and 3.', 'random', 'Low-discrepancy samples'],
            ['Halton3DSequence', 'Mutable incremental three-dimensional Halton sequence; default bases are 2, 3, and 5.', 'random', 'Low-discrepancy samples']
          ]) + '<p>The supported concrete implementation lives in <code>nsk.nu.ashcore.implementation.random</code>:</p>' + directory('implementation/random', [
            ['SplitMix64Random', 'Mutable SplitMix64 generator implementing <code>DeterministicRandom</code>; its identifier is <code>random:splitmix64</code>.', 'random', 'Random generation']
          ])
        },
        {
          id: 'noise-package',
          title: 'Noise',
          html: '<p><code>nsk.nu.ashcore.api.noise</code> supplies sample functions and octave combinations. Noise values become terrain heights only after your application applies a scale and rounding rule.</p>' + directory('api/noise', [
            ['Noise2D', 'Functional interface for a scalar sample at two double coordinates.', 'noise', 'Noise'],
            ['Noise3D', 'Functional interface for a scalar sample at three double coordinates.', 'noise', 'Noise'],
            ['PerlinNoise', 'Seedable 2D/3D improved Perlin noise with an immutable permutation table after construction.', 'noise', 'Perlin noise'],
            ['HashGridNoise', 'Stateless hash-based values for 2D integer cells and interpolated 3D value noise.', 'noise', 'Hash-grid noise'],
            ['FractalNoise', 'Unnormalized octave sums: fractional Brownian motion, turbulence, and ridge noise.', 'noise', 'Fractal noise']
          ])
        },
        {
          id: 'statistics-package',
          title: 'Statistics',
          html: '<p><code>nsk.nu.ashcore.api.stats</code> updates statistics or a bounded sample as values arrive. These objects retain mutable state.</p>' + directory('api/stats', [
            ['RunningStats', 'Sample count, running mean, population variance, and sample variance with constant state.', 'statistics', 'Running statistics'],
            ['WindowedMean', 'Mean over the most recent fixed number of samples.', 'statistics', 'Windowed statistics'],
            ['SlidingWindowMinMax', 'Minimum and maximum over the most recent fixed number of samples.', 'statistics', 'Windowed statistics'],
            ['ExponentialMovingAverage', 'Exponentially weighted mean with direct alpha or half-life configuration.', 'statistics', 'Moving average'],
            ['P2Quantile', 'Approximate, order-dependent estimate of one quantile using five markers.', 'statistics', 'Quantiles'],
            ['ReservoirSampler', 'Mutable bounded sample from a stream, with shallow snapshot copies.', 'statistics', 'Reservoir sampling']
          ])
        },
        {
          id: 'hash-spi-packages',
          title: 'Hashing and service discovery',
          html: '<p><code>nsk.nu.ashcore.api.hash</code> provides numeric key helpers:</p>' + directory('api/hash', [
            ['Hash64', 'Non-cryptographic FNV-1a hashing and 64-bit mixing for identifiers and seeds.', 'utilities', 'Hashing'],
            ['Morton', 'Morton Z-order encoding/decoding: full signed 32-bit coordinates in 2D and signed 21-bit coordinates in 3D.', 'utilities', 'Morton keys']
          ]) + '<p><code>nsk.nu.ashcore.api.spi</code> provides explicit provider selection:</p>' + directory('api/spi', [
            ['Identified', 'Stable non-blank provider identifier contract and identifier validation.', 'utilities', 'Service discovery'],
            ['ServiceRegistry', 'Eager <code>ServiceLoader</code> discovery and immutable membership indexed by provider ID.', 'utilities', 'Service discovery']
          ])
        }
      ]
    },
    {
      id: 'migration',
      category: 'Reference',
      title: 'Migration',
      description: 'Upgrade to 1.2.0 while accounting for numeric corrections, validation, and procedural output.',
      kind: 'guide',
      readingTime: 5,
      intro: '<p>Ashcore 1.2.0 retains the supported public types, methods, and constructors from 1.0.x and 1.1.x. It adds oriented boxes, intersection intervals, contact witnesses, and optional queries. Existing boolean queries, <code>Hit</code>, and <code>SweptAABB.Result</code> retain their contracts.</p>',
      sections: [
        {
          id: 'upgrade-dependency',
          title: 'Update the dependency',
          html: '<ol><li>Set the Ashcore dependency version to <code>1.2.0</code>.</li><li>Build and run your consumer tests with JDK 21 or newer.</li><li>Compare saved procedural outputs and boundary cases before replacing the deployed artifact.</li></ol><p>The <a href="#/installation">Installation guide</a> contains Maven and Gradle declarations. Bounded methods added in 1.1 are default methods on <code>DeterministicRandom</code>; existing implementations need no additional methods.</p>'
        },
        {
          id: 'new-in-1-2',
          title: 'Use the 1.2 additions when needed',
          html: table(['Addition', 'When to use it'], [
            ['<code>OrientedBox</code>', 'Keep a box’s local rotation when an enclosing AABB is too coarse.'],
            ['<code>IntersectionInterval</code>', 'Read both entry and exit parameters for a ray or segment against an oriented box.'],
            ['<code>Contact</code>', 'Read sphere/sphere or sphere/AABB surface witnesses, a normal, and geometric penetration depth.']
          ]) + '<p>Read <a href="#/geometry">Geometry</a>, <a href="#/raycasting">Raycasting</a>, <a href="#/collisions">Collision queries</a>, and <a href="#/transforms">Transforms</a> before changing result types. A contact depth, ray distance, segment fraction, and sweep time measure different quantities.</p>'
        },
        {
          id: 'behavior-changes',
          title: 'Check corrected behavior',
          html: '<p>The following corrections can affect callers upgrading from earlier versions. An unchanged signature does not preserve an erroneous result.</p>' + table(['Area', 'Current behavior and upgrade check'], [
            ['Normalization', 'Finite non-zero vectors and quaternion/plane normalization use scaling. Extreme inputs now normalize correctly; ordinary results can differ in their last bits. Zero vectors remain zero, and a zero quaternion normalizes to identity. Reject non-finite inputs and zero rotation axes before constructing a rotation.'],
            ['Rays and boxes', 'Rays require a finite origin and finite non-zero direction. Ray/box queries treat only exactly zero direction components as parallel, so shallow angles can produce distant hits. Non-finite AABBs remain constructible for legacy adapters, but containment is false and ray collision rejects those bounds.'],
            ['Swept boxes', 'The parameter is time. Moving initial overlap uses an exit-face normal. Supply finite velocity, finite non-negative <code>tMax</code>, and representable bound differences.'],
            ['Spheres and capsules', 'Constructors reject non-finite shape data and negative radii. Capsule queries include spherical ends and degenerate sphere/segment cases.'],
            ['Weighted choices', 'Alias tables preserve relative probabilities for large finite weights. <code>WeightedPicker</code> never selects a zero-weight entry. Draws may differ from older tables; tiny relative weights can still underflow.'],
            ['Statistics', 'P² estimates change, especially for descending samples, and remain approximate. Non-finite samples and exhausted counters are rejected where specified. Compare estimates with a tolerance appropriate to your application.'],
            ['Other numeric limits', '<code>NumericTolerance</code> rejects invalid tolerances. <code>Matrix4</code> inverse methods reject non-finite inputs/determinants; noise methods reject coordinates outside their documented domains. These checks are method-specific. Robust normalization does not make every later product or difference representable.'],
            ['Service discovery', 'Choose a provider by ID. Enumeration and provider initialization order are unspecified. Discovery eagerly executes provider code.']
          ])
        },
        {
          id: 'repeatable-output',
          title: 'Preserve repeatable output',
          html: table(['Operation', 'Repeatability contract'], [
            ['SplitMix64 unbounded stream', 'Stable in Ashcore 1.x for the same seed and ordered calls.'],
            ['Default bounded integer reduction', 'Stable from 1.1 through the rest of 1.x. Existing unbounded streams are unchanged.'],
            ['<code>Hash64</code> and <code>SeedSequence</code>', 'Documented hash and seed outputs are stable in 1.x.'],
            ['Noise, weighted tables, distributions, floating-point math, approximate statistics', 'Do not assume cross-release bitwise identity. Keep the same version, inputs, initial state, call/sample order, and environment when reproducing output.']
          ]) + note('Stored terrain needs a version', '<p>Save the library version and generation parameters alongside your world-generation recipe. A seed alone cannot reproduce output after a noise algorithm, table construction, or call-order change.</p>') + '<p>Changes to explicitly stable algorithms require a major version or a separately named algorithm. Other numeric fixes can change results within the documented compatibility contract.</p>'
        }
      ]
    },
    {
      id: 'troubleshooting',
      category: 'Reference',
      title: 'Troubleshooting',
      description: 'Diagnose dependency loading, invalid geometry, unexpected query results, and changing random output.',
      kind: 'guide',
      readingTime: 8,
      intro: '<p>Start with the observed error or result. Keep the Ashcore version, exact inputs, and expected unit with a reproducible example.</p>',
      sections: [
        {
          id: 'java-version',
          title: 'UnsupportedClassVersionError or release 21 is not supported',
          html: '<p>Ashcore 1.2.0 targets Java 21 bytecode. The JDK used to build your project and the runtime used to launch it can differ. Check both in your terminal:</p>' + code('powershell', 'Terminal — Java and Maven versions', 'java -version\nmvn -version') + '<p>Both commands must report Java 21 or newer. If Maven reports an older JDK, correct the JDK selected by your build environment. If only the server fails, check the Java executable in its launch command.</p>'
        },
        {
          id: 'classpath',
          title: 'ClassNotFoundException, NoClassDefFoundError, or NoSuchMethodError',
          html: '<p>Resolve the library at build time and make it available to the application’s runtime class loader. For a Minecraft plugin, use your platform’s supported library-loading or packaging mechanism. Ashcore supplies no server plugin entry point.</p><p>Inspect the selected dependency in your Maven consumer project:</p>' + code('powershell', 'Terminal — selected Ashcore dependency', 'mvn dependency:tree "-Dincludes=dev.nasaka.blackframe:ashcore"') + '<p>Expect <code>dev.nasaka.blackframe:ashcore:jar:1.2.0</code>. An absent entry indicates that the dependency is missing from that project. A different version can explain a missing newer method.</p><p>If loading succeeds, run this diagnostic in the affected application to see the runtime and the actual class source:</p>' + code('java', 'AshcoreClasspathCheck.java', 'import nsk.nu.ashcore.api.math.Vector3;\n\npublic final class AshcoreClasspathCheck {\n    public static void main(String[] args) {\n        System.out.println("Java: " + Runtime.version());\n        System.out.println("Class source: "\n                + Vector3.class.getProtectionDomain().getCodeSource());\n        System.out.println("Class loader: " + Vector3.class.getClassLoader());\n    }\n}') + '<p>Compare the reported source with the artifact you intended to deploy. A shaded copy can point to the containing plugin JAR. Keep the original import when Shade packages your source. Use the relocated import only when compiling a separate diagnostic against an already relocated artifact. Review the first underlying exception in the stack trace as well: a <code>NoClassDefFoundError</code> can follow a failed class initialization.</p>'
        },
        {
          id: 'normalization',
          title: 'A ray or rotation constructor rejects its direction',
          html: '<p>A zero vector can normalize to zero, but it cannot define a ray direction or rotation axis. Two identical points produce a zero direction when subtracted. Skip that query or choose a direction explicitly.</p>' + table(['Input or operation', 'Requirement'], [
            ['<code>new Ray(origin, direction)</code>', 'Finite origin; finite non-zero direction. The constructor normalizes the direction.'],
            ['<code>Quaternion.fromAxisAngle(axis, angle)</code>', 'Finite non-zero axis and finite angle in radians.'],
            ['<code>new OrientedBox(center, halfExtents, orientation)</code>', 'Finite center, finite non-negative half extents, and a finite non-zero quaternion.'],
            ['<code>Vector2/3/4.normalized()</code>', 'Finite components. Zero stays zero; finite non-zero input is normalized with scaling.'],
            ['<code>Quaternion.normalized()</code> / <code>inverse()</code>', 'Zero normalizes to identity, but zero has no inverse and <code>inverse()</code> throws <code>ArithmeticException</code>.']
          ]) + '<p>Log the original components before the failing call. Check for division by zero, an overflowed subtraction, or a non-finite upstream value. Replacing an invalid vector with zero still does not supply a valid direction.</p>'
        },
        {
          id: 'ray-miss',
          title: 'A query returns infinity or its point is null',
          html: '<p>A first-contact query returns <code>Double.POSITIVE_INFINITY</code> when it misses. A returned <code>Hit</code> has null point/normal fields on a miss. Check the result before calling <code>Ray.at(t)</code> or dereferencing vectors.</p>' + code('java', 'AshcoreRayCheck.java', 'import nsk.nu.ashcore.api.collision.CollisionTests;\nimport nsk.nu.ashcore.api.collision.Hit;\nimport nsk.nu.ashcore.api.geometry.AxisAlignedBox;\nimport nsk.nu.ashcore.api.geometry.Ray;\nimport nsk.nu.ashcore.api.math.Vector3;\n\npublic final class AshcoreRayCheck {\n    public static void main(String[] args) {\n        Ray ray = new Ray(new Vector3(-2, 0, 0), new Vector3(1, 0, 0));\n        AxisAlignedBox box = new AxisAlignedBox(\n                new Vector3(0, -1, -1), new Vector3(1, 1, 1));\n        Hit result = CollisionTests.rayVsBoxHit(ray, box);\n        double maxDistance = 5.0;\n        if (result.hit() && result.t() <= maxDistance) {\n            System.out.println("Distance: " + result.t()); // 2.0\n            System.out.println("Point: " + result.point());\n        } else {\n            System.out.println("No hit within range");\n        }\n    }\n}') + '<p>Check that the ray and shape use the same coordinate system and position unit. A ray points forward and has no built-in maximum distance. A box behind the origin is a miss. For an oriented-box interval, use <code>hit()</code>; its miss endpoints are <code>(+infinity, -infinity)</code>.</p><p><code>CollisionUtils.rayVsPlaneT</code> also returns infinity for parallel, coplanar, or behind-origin queries. It treats an absolute normal/direction dot product below <code>1e-12</code> as parallel, so it can omit distant shallow-angle plane hits.</p>'
        },
        {
          id: 'parameter-units',
          title: 'The hit distance or normal looks wrong',
          html: table(['Result', 'Meaning'], [
            ['Ray first-contact <code>t</code>', 'Distance in position units, because <code>Ray</code> normalizes its direction.'],
            ['Segment first-contact <code>t</code>', 'Dimensionless fraction in <code>[0,1]</code>; multiply by the segment length for distance.'],
            ['<code>SweptAABB.Result.t()</code>', 'Time in the velocity’s time unit. With a displacement as velocity and <code>tMax=1</code>, it is a motion fraction.'],
            ['Ray/OBB interval entry', 'Can be negative when the origin is inside. The first-forward-contact helper returns zero in that case.'],
            ['<code>Contact.depth()</code>', 'Geometric penetration depth in position units. Zero is touching; a miss has negative infinity.']
          ]) + '<p>When a ray starts inside an AABB, <code>rayVsBoxHit</code> returns the origin at <code>t=0</code> with an exit-face normal. The point is therefore not on that exit face. Face ties use X, then Y, then Z. A resting overlapping sweep returns <code>t=0</code> and a null normal.</p><p>A hit against a bounding box describes that box. If it encloses a more detailed object, test the candidate against the object’s geometry separately. See <a href="#/raycasting">Raycasting</a> and <a href="#/collisions">Collision queries</a>.</p>'
        },
        {
          id: 'random-state',
          title: 'The same seed produces different output',
          html: '<p>A seed initializes generator state. Each draw advances it. Compare the algorithm, Ashcore version, initial seed, and complete ordered call sequence. A new draw inserted earlier changes later results.</p><p><code>PerlinNoise</code> construction consumes 255 generator draws. Bounded integer methods can consume multiple draws because of rejection sampling; even a singleton interval consumes one. Use separately derived seeds for tasks whose call counts change independently:</p>' + code('java', 'AshcoreSeedCheck.java', 'import nsk.nu.ashcore.api.random.DeterministicRandom;\nimport nsk.nu.ashcore.api.random.DeterministicRandoms;\nimport nsk.nu.ashcore.api.random.SeedSequence;\n\npublic final class AshcoreSeedCheck {\n    public static void main(String[] args) {\n        SeedSequence seeds = new SeedSequence(1337L);\n        DeterministicRandom terrain = DeterministicRandoms.splitMix64(\n                seeds.derive("terrain"));\n        DeterministicRandom loot = DeterministicRandoms.splitMix64(\n                seeds.derive("loot"));\n        long terrainValue = terrain.nextLong();\n        loot.nextLong();\n        DeterministicRandom terrainAgain = DeterministicRandoms.splitMix64(\n                seeds.derive("terrain"));\n        System.out.println(terrainValue == terrainAgain.nextLong()); // true\n    }\n}') + '<p>Tag text is exact and is not normalized. Reusing a tag repeats its seed; distinct tags do not guarantee collision-free or statistically independent streams. Mutable generators require exclusive access or externally ordered synchronization. Scheduling-dependent call order remains scheduling-dependent output.</p><p><code>ReservoirSampler.reset()</code> clears its contents and counters, but does not reset its generator. Recreate both from the same initial state to repeat a complete sampling run. See <a href="#/random">Random generation</a> and <a href="#/migration">Migration</a> for stability boundaries.</p>'
        },
        {
          id: 'weights-noise-statistics',
          title: 'Weights, noise, or statistics reject input',
          html: table(['Symptom', 'Check'], [
            ['Weighted choice throws', 'Weights must be finite, non-negative, and include a positive value. <code>WeightedPicker</code> also requires a finite positive sum; adding individually finite weights can overflow that sum. <code>WeightedSampler</code> builds scaled tables for repeated draws.'],
            ['Editing weights has no effect', '<code>WeightedSampler.build</code> builds its own tables. Rebuild after changing the distribution.'],
            ['Noise rejects a coordinate', 'Perlin coordinates must lie in <code>[-2³¹, 2³¹)</code>. Hash-grid noise requires <code>[Integer.MIN_VALUE, Integer.MAX_VALUE)</code>. Every octave must remain in the base noise’s domain.'],
            ['Fractal noise exceeds ±1', 'The result is an unnormalized sum of octave values. Choose the output scale in the caller.'],
            ['Statistic is NaN', '<code>RunningStats</code> needs a sample for mean/population variance and two for sample variance. <code>P2Quantile</code> needs five samples.'],
            ['A counter is exhausted', 'Incremental Halton and P² stop at <code>Integer.MAX_VALUE</code> samples until reset. Running statistics and reservoir sampling reject <code>Long.MAX_VALUE</code> count overflow. Start a new accumulator or reset only where the API supports it.'],
            ['A snapshot item changes', 'Reservoir snapshots copy slot references, not item objects. Later mutation of an item remains visible through that reference.']
          ]) + '<p><code>RunningStats</code> and <code>P2Quantile</code> reject non-finite samples. <code>WindowedMean</code>, <code>SlidingWindowMinMax</code>, and <code>ExponentialMovingAverage</code> do not provide the same guard; validate their samples in the caller. Require a finite alpha in <code>(0,1]</code> for an EMA and a finite positive half-life when deriving alpha.</p><p><code>SlidingWindowMinMax</code> has an unguarded integer index in 1.2.0. Replace it before that index overflows during a long-running stream; it has no reset method. Other counters’ rejection behavior does not apply to this class.</p><p>Finite samples alone do not guarantee representable differences, squared deviations, or octave arithmetic. Log values near the first failure. Choose a common scale that keeps the required intermediate calculations representable.</p>'
        },
        {
          id: 'legacy-numeric-behavior',
          title: 'An angle or range result contradicts its expected bounds',
          html: '<p>Several legacy helpers in 1.2.0 have narrower behavior than their comments suggest. These are known limitations of the current code:</p>' + table(['Operation', 'Observed behavior', 'Caller action'], [
            ['<code>Angles.wrapDegrees180(-720)</code>', 'Returns <code>-360</code>, outside the documented interval. <code>deltaDegrees</code> uses the same helper.', 'For finite degrees, reduce the value with <code>wrapDegrees360</code> before passing it to <code>wrapDegrees180</code>. For deltas, keep the subtraction representable.'],
            ['<code>new DoubleRange(0, 1).contains(Double.NaN)</code>', 'Returns <code>true</code>. The constructor also does not reject NaN endpoints.', 'Require finite endpoints and query values before using a range as a validity check.'],
            ['<code>MathUtil.near(a, b, eps)</code>', 'Directly compares <code>abs(a-b) &lt;= eps</code> without validating the tolerance.', 'Use <code>NumericTolerance.near</code> when you need rejection of a negative or non-finite tolerance.'],
            ['<code>Matrix2.inverse()</code> and <code>Matrix3.inverse()</code>', 'Their absolute determinant guard does not reject every non-finite input or determinant.', 'Validate finite inputs and results. The extra checks on <code>Matrix4</code> do not apply automatically to these types.']
          ]) + '<p>The <a href="#/math">Math</a> and <a href="#/transforms">Transforms</a> pages describe these boundaries beside the affected operations. Do not use a NaN result or a tolerated absolute determinant as proof of a valid, accurate calculation.</p>'
        },
        {
          id: 'service-discovery',
          title: 'No service is registered or an ID is duplicated',
          html: '<p>Ashcore ships the registry utility without built-in <code>META-INF/services</code> provider registrations. To create its supplied generator, call <code>DeterministicRandoms.splitMix64(seed)</code> directly.</p><p>For your own SPI, confirm that the provider registration resource is packaged and visible to the chosen class loader. Select the intended provider with its exact ID. <code>get(id)</code> returns an empty <code>Optional</code> for absence; <code>require(id)</code> throws <code>IllegalStateException</code>.</p><p>Duplicate IDs and null/blank IDs returned by providers throw <code>IllegalStateException</code>. Null/blank IDs passed to lookup methods throw <code>IllegalArgumentException</code>. The provider-ID behavior follows <code>Identified.requireValidId</code>; the broader exception description in the registry Javadoc does not match this path in 1.2.0.</p><p>Inspect all registrations and ensure each provider ID is non-blank and unique within that service type. Discovery eagerly runs constructors, static initialization, and <code>id()</code>; their failures propagate without undoing earlier effects.</p><p>Use the explicit-loader <code>ServiceRegistry.of(type, loader)</code> overload when the context loader cannot see the provider. See <a href="#/utilities">Utilities</a> for the complete discovery contract.</p>'
        },
        {
          id: 'report-a-problem',
          title: 'Prepare a reproducible example',
          html: '<p>Include the dependency version, Java version, full underlying exception, exact inputs, units, and expected result. For random output, include the seed and ordered calls. Reduce the example to a small Java program when the problem does not require your platform.</p><p>For a source-checkout verification, use JDK 21 or newer and Maven 3.9 or newer:</p>' + code('powershell', 'Terminal — Ashcore checkout', 'mvn -B clean verify') + '<p>This runs the project’s tests and packaged-artifact integration checks. A successful library build helps distinguish a consumer integration problem from a reproducible library failure. Keep the observed result with your report in the <a href="https://github.com/Miciasty/Ashcore/issues">Ashcore issue tracker</a>.</p>'
        }
      ]
    }
  );
})();
