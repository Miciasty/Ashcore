# Ashcore

Low-level deterministic Java foundation library for math, geometry, primitive collisions, sampling, noise, hashing, and online statistics.

> [!NOTE]
> Ashcore is the base layer of Blackframe.  
> Voxel and chunk workflows belong to Ashgrid.

## 1. Purpose

Ashcore gives you reusable low-level primitives so you can build engines and plugins without rewriting the same math, collision, random, and stats code in every project.

## 2. Problem

Most projects repeatedly rebuild the same foundations:
- vector/matrix/quaternion math with consistent numeric rules,
- primitive geometry and collision helpers,
- reproducible random sampling and noise,
- online statistics for streams of values.

This duplication causes inconsistent behavior, hidden bugs, and weak determinism. Ashcore centralizes these building blocks behind stable contracts.

## 3. When to use

Use Ashcore when:
- you need deterministic low-level utilities with explicit inputs,
- you want immutable math types and predictable helper APIs,
- you need reusable primitives for collision, noise, sampling, and stats.

Do not use Ashcore when:
- you need voxel/chunk storage and traversal (use Ashgrid),
- you need coordinate-space/world-frame abstractions,
- you need high-level gameplay, navigation, or scene systems.

## 4. Simple example (Minecraft plugin example)

You are building a plugin that:
1. generates terrain height from deterministic noise,
2. ray-tests player interaction against simple hit volumes,
3. tracks online average values without storing full history.

Ashcore lets you do all three with one consistent deterministic base:
- same seed gives same terrain every run,
- primitive collision works without bringing a full physics stack,
- running statistics update in O(1) per sample.

## 5. How it works

1. Immutable value objects (`Vector*`, `Matrix*`, `Quaternion`) provide safe math primitives.
2. Geometry and collision APIs operate on explicit inputs (`Ray`, `AxisAlignedBox`, `Plane`).
3. Random and sampling utilities use explicit seeds for reproducible results.
4. Noise and hashing build deterministic procedural pipelines from those primitives.
5. Online stats update state incrementally per sample, without full-buffer recomputation.

## 6. Big-O for operations

Definitions:
- `n`: number of items used to build a weighted sampler.
- `o`: number of octaves in fractal noise.

| Operation | Complexity | Notes |
| --- | --- | --- |
| `Vector3` arithmetic (`add/sub/dot/cross`) | `O(1)` | Fixed-size operations. |
| `Matrix4.mul` / `Matrix4.inverseAffine` | `O(1)` | Fixed-size matrix math. |
| `CollisionTests.rayVsBoxT` | `O(1)` | Slab test on one AABB. |
| `SweptAABB.test` | `O(1)` | Single moving AABB vs static AABB test. |
| `HaltonSequence.next` | amortized `O(1)` | Digit-carry update per sample. |
| `WeightedSampler.build` | `O(n)` | Alias table preprocessing. |
| `WeightedSampler.sampleIndex` | `O(1)` | Constant-time alias sampling. |
| `PerlinNoise.sample` | `O(1)` | Fixed work per 2D/3D sample. |
| `FractalNoise.fbm` | `O(o)` | One base-noise call per octave. |
| `RunningStats.add` / `ReservoirSampler.add` | `O(1)` | One-pass streaming updates. |

## 7. Core terms

- `deterministic`: same input and seed always produce the same output.
- `AABB`: axis-aligned bounding box (`AxisAlignedBox`).
- `ray parameter t`: scalar position along a ray (`origin + direction * t`).
- `low-discrepancy sequence`: sequence with more even coverage than plain pseudorandom points.
- `online statistic`: metric updated incrementally as new samples arrive.
- `seed sequence`: deterministic seed-derivation utility for reproducible streams.

## 8. Quick-start

Requires **Java 21+**.

Published to Maven Central:

Maven:

```xml
<dependency>
  <groupId>dev.nasaka.blackframe</groupId>
  <artifactId>ashcore</artifactId>
  <version>1.0.1</version>
</dependency>
```

Note: Maven coordinates use `dev.nasaka.blackframe:ashcore`, while Java package names remain `nsk.nu.ashcore.*` for compatibility.

Minimal usage example:

```java
import nsk.nu.ashcore.api.collision.CollisionTests;
import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.noise.FractalNoise;
import nsk.nu.ashcore.api.noise.PerlinNoise;
import nsk.nu.ashcore.api.random.DeterministicRandom;
import nsk.nu.ashcore.api.random.DeterministicRandoms;
import nsk.nu.ashcore.api.stats.RunningStats;

public final class AshcoreQuickStart {
    public static void main(String[] args) {
        DeterministicRandom rng = DeterministicRandoms.defaultGenerator(1337L);
        PerlinNoise perlin = new PerlinNoise(rng);

        double height = FractalNoise.fbm(perlin, 128.0 * 0.02, 64.0 * 0.02, 5, 2.0, 0.5);

        Ray ray = new Ray(new Vector3(-2, 0, 0), new Vector3(1, 0, 0));
        AxisAlignedBox box = new AxisAlignedBox(new Vector3(0, -1, -1), new Vector3(1, 1, 1));
        double t = CollisionTests.rayVsBoxT(ray, box);

        RunningStats stats = new RunningStats();
        stats.add(height);

        System.out.printf("height=%.3f, rayHitT=%.3f, mean=%.3f%n", height, t, stats.mean());
    }
}
```

## License

Apache-2.0 Copyright 2025 Mateusz Aftanas
