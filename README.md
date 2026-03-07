# Ashcore

Low-level, deterministic Java foundation library for math, geometry, primitive collisions, sampling, noise, hashing, and online statistics.

> [!NOTE]
> **Ashcore** is the core layer of Blackframe.
> For grid/voxel operations use **Ashgrid**.
> For acceleration structures and advanced tracing use **Ashtrace**.
> For pathfinding use **Ashnav**.
> For coordinate-space systems use **Ashspace**.

---

## What is Ashcore

**Ashcore** provides the reusable base that most engines and plugins keep rewriting:
- immutable vector/matrix math,
- geometric primitives and primitive-level collision tests,
- deterministic random and low-discrepancy sampling,
- deterministic hash/noise helpers,
- online statistical estimators,
- minimal SPI utilities for pluggable implementations.

The scope is intentionally low-level and engine-agnostic.

---

## Design goals

- Deterministic behavior from explicit seeds.
- Small, composable APIs with stable contracts.
- Immutable value types for safe reuse.
- No platform/runtime lock-in.
- Foundation-first: extend by adding new fundamentals, not by rewriting old ones.

---

## Installation

Maven:

```xml
<dependency>
  <groupId>nsk.nu</groupId>
  <artifactId>ashcore</artifactId>
  <version>1.0.0</version>
</dependency>
```

Gradle (Kotlin DSL):

```kts
implementation("nsk.nu:ashcore:1.0.0")
```

Requires **Java 21+**.

---

## Feature map

### Math (`api.math`)
- `Vector2`, `Vector3`, `Vector4`
- `Vector2i`, `Vector3i`, `Vector4i`
- `Matrix2`, `Matrix3`, `Matrix4`
- `Quaternion`
- `Angles`, `DivMod`, `MathUtil`
- `IntRange`, `DoubleRange`
- `KahanSummation`
- `NumericTolerance`

### Geometry (`api.geometry`)
- `AxisAlignedBox` (3D AABB), `AxisAlignedRect` (2D AABB)
- `Ray`, `Plane`, `Segment3`, `Sphere`, `Capsule`
- `GeometryUtils`, `Boxes`, `OrthonormalBasis`

### Collision (`api.collision`)
- `CollisionTests` (e.g. ray vs AABB slab)
- `CollisionUtils` (primitive helpers)
- `SweptAABB` (continuous moving-AABB test)
- `Hit`

### Random & sampling (`api.random`)
- `DeterministicRandom` + `DeterministicRandoms`
- `HaltonSequence`, `Halton2DSequence`, `Halton3DSequence`
- `LowDiscrepancy` mappings (disk/sphere/hemisphere/cone)
- `Distributions`
- `WeightedPicker`, `WeightedSampler`
- `Permutation`
- `SeedSequence`

### Noise (`api.noise`)
- `Noise2D`, `Noise3D`
- `PerlinNoise`
- `FractalNoise` (fbm/turbulence/ridge)
- `HashGridNoise`

### Hashing (`api.hash`)
- `Hash64`
- `Morton` (2D/3D Z-order encode/decode)

### Statistics (`api.stats`)
- `RunningStats`
- `ExponentialMovingAverage`
- `WindowedMean`
- `SlidingWindowMinMax`
- `ReservoirSampler`
- `P2Quantile`

### SPI (`api.spi`)
- `Identified`
- `ServiceRegistry`

---

## Quick start

```java
import nsk.nu.ashcore.api.collision.CollisionTests;
import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector2;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.noise.FractalNoise;
import nsk.nu.ashcore.api.noise.PerlinNoise;
import nsk.nu.ashcore.api.random.DeterministicRandom;
import nsk.nu.ashcore.api.random.DeterministicRandoms;
import nsk.nu.ashcore.api.random.Halton2DSequence;
import nsk.nu.ashcore.api.random.LowDiscrepancy;
import nsk.nu.ashcore.api.stats.ExponentialMovingAverage;

// Deterministic RNG + noise
DeterministicRandom rng = DeterministicRandoms.defaultGenerator(1337L);
PerlinNoise perlin = new PerlinNoise(rng);

double x = 128.0;
double z = 64.0;
double height = FractalNoise.fbm(perlin, x * 0.02, z * 0.02, 5, 2.0, 0.5);

// Primitive collision
Ray ray = new Ray(new Vector3(-2, 0, 0), new Vector3(1, 0, 0));
AxisAlignedBox box = new AxisAlignedBox(new Vector3(0, -1, -1), new Vector3(1, 1, 1));
double t = CollisionTests.rayVsBoxT(ray, box);

// Low-discrepancy sampling
Halton2DSequence seq = new Halton2DSequence(); // bases 2 and 3
Vector2 uv = seq.nextUnitSquare();
Vector3 hemisphereDir = LowDiscrepancy.mapToUniformHemisphere(uv.x(), uv.y());

// Online smoothing
ExponentialMovingAverage ema = new ExponentialMovingAverage(0.2);
double smoothed = ema.add(42.0);
```

---

## API highlights

- Math: `Quaternion.slerp(...)`, `Angles.deltaRadians(...)`, `Matrix4.inverseAffine(...)`
- Geometry: `OrthonormalBasis.fromNormal(...)`, `GeometryUtils.closestPointOnSegment(...)`
- Collision: `CollisionTests.rayVsBoxT(...)`, `SweptAABB.test(...)`
- Sampling: `Halton2DSequence.nextConcentricDisk()`, `LowDiscrepancy.mapToUniformCone(...)`
- Random: `DeterministicRandoms.defaultGenerator(...)`, `WeightedSampler.build(...).sampleIndex(...)`
- Noise: `PerlinNoise.sample(...)`, `FractalNoise.fbm(...)`
- Stats: `P2Quantile.add(...)/estimate()`, `SlidingWindowMinMax.add(...)`
- SPI: `ServiceRegistry.of(...)`, `ServiceRegistry.require(...)`

---

## Performance notes

- Most methods are O(1) and allocation-light.
- `WeightedSampler`: build O(n), sample O(1).
- `HaltonSequence.next()`: amortized O(1).
- `FractalNoise`: O(octaves) per sample.

---

## Stability policy

- Public API under `nsk.nu.ashcore.api.*` is the stability surface.
- Behavioral fixes are allowed.
- Breaking API changes should be introduced only in major versions.

---

## Contributing

- Java 21+, JUnit 5.
- Keep contracts explicit and deterministic.
- Keep comments precise and synchronized with behavior.
- Prefer small, focused additions to core primitives.
- Run full verification before release.

---

## License

Apache-2.0  
Copyright 2025 Mateusz Aftanas
