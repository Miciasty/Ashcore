# Ashcore

Foundation math & utilities for game/engine plugins: vectors, geometry, collisions on primitives, RNG, low-discrepancy sequences, noise, online stats.

> [!NOTE]
> **Ashcore** provides platform-agnostic math and primitives.
>- Grid and voxel see **Ashgrid**.
>- Ray tracing and acceleration see **Ashtrace**.
>- Pathfinding and graphs see **Ashnav**.
>- Coordinate systems and transforms see **Ashspace**.

---

## Table of contents

- [What is it?](#what-is-it)
- [Why use it](#why-use-it)
- [Features](#features)
- [Installation](#installation)
- [Quick start](#quick-start)
- [API highlights](#api-highlights)
- [Performance notes](#performance-notes)
- [Project status](#project-status)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## What is it?

**Ashcore** is a small, pragmatic Java library with the building blocks most engines end up rewriting: vector math, geometric primitives, primitive-level collisions, deterministic random sampling, low-discrepancy sequences, continuous/discrete noise, and online statistics. It stays low-level and fast so higher-level libraries can compose it cleanly.

---

## Why use it

- **Consistent primitives.** `Vector3`, `Quaternion`, `Ray`, `AxisAlignedBox`, `Plane`, etc.
- **Deterministic random.** Seeded RNG, alias sampling, Halton sequences for stable runs.
- **No platform baggage.** Pure Java 21; drop it into plugins or libraries.
- **Good defaults.** Branch-light helpers, zero allocations in hot loops where practical.

---

## Features

- **Math**
    - `Vector3`, `Quaternion`, `Angles`/`AngleUtil`, `MathUtil`, `DivMod`
    - Integer coords: `Int2`, `Int3`; ranges: `IntRange`, `DoubleRange`
    - Stable sums: `KahanSummation`
- **Geometry**
    - Primitives: `Ray`, `AxisAlignedBox`, `Plane`, `Segment3`, `Sphere`, `Capsule`
    - Helpers: `GeometryUtils` (project/reflect/closest point), `Boxes` (union/expand/overlaps)
    - `OrthonormalBasis` (TBN) from a single normal
- **Collisions on primitives**
    - Slab `rayVsBox` (+ `Hit`), `CollisionUtils` (ray-plane, closest point), `SweptAABB` (continuous)
- **Interpolation**
    - `Easing`, `CatmullRom` (scalar & `Vector3`)
- **RNG & sampling**
    - `DeterministicRandom` (SplitMix64), `Permutation` (Fisher–Yates)
    - Weighted choices: `WeightedPicker` (O(n)), `WeightedSampler` (alias, build O(n), sample O(1))
    - Low-discrepancy: `HaltonSequence`, `Halton2DSequence`, `Halton3DSequence`
    - Mappings: unit square/cube → disk/sphere/hemisphere/cone
    - `SeedSequence` (derive stable seeds by tag), `Hash64`, `Morton` (2D/3D Z-order)
- **Noise**
    - `PerlinNoise` (2D/3D), `FractalNoise` (fbm/turbulence/ridge), `HashGridNoise` (value noise on int grid)
- **Online stats**
    - `RunningStats`, `ExponentialMovingAverage`, `WindowedMean`
    - `SlidingWindowMinMax` (O(1) amort.), `ReservoirSampler` (Alg. R), `P2Quantile` (online percentile)
- **Timing & small utilities**
    - `RateLimiter`, `Debouncer`, `CooldownMap`
- **Color**
    - `ColorUtil` (RGB↔HSV)

---

## Installation

Maven:

```xml
<dependency>
  <groupId>nsk.nu</groupId>
  <artifactId>Ashcore</artifactId>
  <version>1.0</version>
</dependency>
```

Gradle (Kotlin):

```kts
implementation("nsk.nu:Ashcore:1.0")
```

Requires **Java 21+**.

---

## Quick start

```java
import nsk.nu.ashcore.implementation.random.SplitMix64Random;
import nsk.nu.ashcore.api.random.DeterministicRandom;
import nsk.nu.ashcore.api.noise.PerlinNoise;
import nsk.nu.ashcore.api.noise.FractalNoise;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.collision.CollisionTests;
import nsk.nu.ashcore.api.random.Halton2DSequence;
import nsk.nu.ashcore.api.math.Double2;
import nsk.nu.ashcore.api.random.LowDiscrepancy;
import nsk.nu.ashcore.api.stats.ExponentialMovingAverage;

// RNG + noise
DeterministicRandom rng = new SplitMix64Random(1337L);
PerlinNoise perlin = new PerlinNoise(rng);
double h = FractalNoise.fbm(perlin, x * 0.02, z * 0.02, 5, 2.0, 0.5);

// Ray vs box (slab)
Ray ray = new Ray(new Vector3(0, 1, 0), new Vector3(1, 0, 0));
AxisAlignedBox box = new AxisAlignedBox(new Vector3(2, 0, -1), new Vector3(3, 2, 1));
double t = CollisionTests.rayVsBoxT(ray, box);

// Low-discrepancy hemisphere direction (Y-up)
Halton2DSequence seq = new Halton2DSequence(); // bases 2 & 3
Double2 uv = seq.nextUnitSquare();
Vector3 dir = LowDiscrepancy.mapToUniformHemisphere(uv.x(), uv.y());

// Online stats
ExponentialMovingAverage ema = new ExponentialMovingAverage(0.2);
double smooth = ema.add(sample);
```

---

## API highlights

- Math: `Vector3`, `Quaternion.slerp(...)`, `Angles.deltaRadians(...)`, `DivMod.floorMod(...)`
- Geometry: `OrthonormalBasis.fromNormal(...)`, `GeometryUtils.reflect(...)`, `Boxes.overlaps(...)`
- Collisions: `CollisionTests.rayVsBoxT(...)`, `SweptAABB.test(...)`
- RNG & sampling: `WeightedSampler.build(weights).sampleIndex(rng)`, `Permutation.shuffle(int[])`
- Low-discrepancy: `Halton2DSequence.nextConcentricDisk()`, `Halton3DSequence.nextHemisphereYUp()`
- Noise: `PerlinNoise.sample(...)`, `FractalNoise.fbm(...)`
- Stats: `SlidingWindowMinMax.add(...)`, `P2Quantile.add(...).estimate()`
- Utils: `RateLimiter.tryAcquire(System::nanoTime)`, `SeedSequence.derive("feature-x")`

---

## Performance notes

- Most operations are **O(1)**; helpers avoid allocations in inner loops.
- **WeightedSampler**: build **O(n)**, each sample **O(1)**. Prefer it when weights are stable and you draw many times.
- **HaltonSequence**: `next()` is **amortized O(1)**; random access `halton(n, base)` is **O(log n)** by definition.
- **FractalNoise**: **O(octaves)** per sample; pick octaves accordingly.

---

## Project status

- ✅ Stable math/geometry primitives
- ✅ Primitive-level collisions (slab, swept AABB)
- ✅ Deterministic RNG & samplers (alias, Halton 2D/3D)
- ✅ Noise (Perlin/FBM) and online stats (EMA, P², reservoir)
- ✅ Hashing (64-bit), Morton 2D/3D, seed derivation

---

## Roadmap

- JMH micro-benchmarks for hot paths
- Optional cosine-weighted hemisphere mapping helper
- Package-level JavaDoc summaries

---

## Contributing

- Java 21+, JUnit 5.
- Keep `api/*` small and clear; implementation classes are `final` and allocation-aware.
- Add tests (GIVEN–WHEN–THEN) where non-obvious.
- Run `mvn -DskipTests=false clean verify` before pushing.

---

## License

Apache-2.0 Copyright 2025 Mateusz Aftanas
