(function () {
  'use strict';
  const { code, table, note } = window.WIKI_HTML;

  window.WIKI_PAGES.push(
    {
      id: 'random', category: 'Data & generation', title: 'Random and sampling',
      description: 'Repeat random draws, select weighted items, and generate samples on geometric domains.',
      kind: 'guide', readingTime: 12,
      intro: '<p>Ashcore supplies a mutable random generator, stateless seed derivation, weighted selection, and Halton sequences. These APIs return numbers and vectors for your application to use. Add the library through <a href="#/installation">Installation</a> before running the examples.</p>',
      sections: [
        {
          id: 'repeat-a-sequence', title: 'Repeat a sequence',
          html: `<p>Create a generator with an explicit seed. Repeat the same ordered calls to reproduce its output. <code>splitMix64</code> has identifier <code>random:splitmix64</code>; <code>defaultGenerator</code> selects SplitMix64 throughout Ashcore 1.x.</p>
          ${code('java', 'RepeatRandom.java', `import nsk.nu.ashcore.api.random.DeterministicRandom;
import nsk.nu.ashcore.api.random.DeterministicRandoms;

public final class RepeatRandom {
    public static void main(String[] args) {
        DeterministicRandom rng = DeterministicRandoms.splitMix64(0L);
        int first = rng.nextInt(10);
        long second = rng.nextLong(1000);
        int third = rng.nextInt(-50, 50);
        if (first != 3 || second != 850 || third != -43) {
            throw new AssertionError("Unexpected sequence");
        }
        System.out.println(first + ", " + second + ", " + third);
    }
}`)}
          <p>The output is <code>3, 850, -43</code> in version 1.2.0. Changing the seed, the bounds, or a preceding draw can change later values.</p>
          ${table(['Method', 'Result and state consumption'], [
            ['<code>nextLong()</code>', 'Any signed 64-bit value; advances the generator once.'],
            ['<code>nextInt()</code>', 'The low 32 bits of one <code>nextLong()</code> result.'],
            ['<code>nextUnitDouble()</code>', 'A double in <code>[0, 1)</code>, using 53 bits from one <code>nextLong()</code>.'],
            ['<code>nextInt(bound)</code>, <code>nextLong(bound)</code>', 'An integer in <code>[0, bound)</code>; requires a positive bound. May consume multiple source draws.'],
            ['<code>nextInt(origin, bound)</code>, <code>nextLong(origin, bound)</code>', 'An integer in <code>[origin, bound)</code>; requires <code>origin &lt; bound</code>. Supports widths that overflow the result type.']
          ])}
          <p>The lower bound is included; the upper bound is excluded. Invalid bounds throw <code>IllegalArgumentException</code> before consuming state. Even a one-value interval consumes a draw. Bounded methods use rejection sampling, so expected work is constant but the draw count has no fixed upper limit.</p>`
        },
        {
          id: 'seed-and-state', title: 'Keep seed and state separate',
          html: `<p>The seed starts a sequence; the current state also depends on every draw since creation. The public generator API has no reset, state export, or state restore method. Recreate a generator from its seed and replay the ordered operations when you need the same position.</p>
          <p>The SplitMix64 long stream and default int/double conversions are stable throughout 1.x. Record the explicit algorithm, library version, seed, and call order when results must survive an application update. See <a href="#/migration">Migration and compatibility</a> for the scope of these guarantees.</p>
          <p>Use separate generators for independently scheduled work. A shared mutable generator requires exclusive access or synchronization with a reproducible call order. A lock alone does not decide which caller receives each draw.</p>
          ${code('java', 'DerivedSeeds.java', `import nsk.nu.ashcore.api.random.DeterministicRandom;
import nsk.nu.ashcore.api.random.DeterministicRandoms;
import nsk.nu.ashcore.api.random.SeedSequence;

public final class DerivedSeeds {
    public static void main(String[] args) {
        SeedSequence seeds = new SeedSequence(1337L);
        DeterministicRandom terrain =
                DeterministicRandoms.fromDerivedSeed(seeds, "terrain");
        DeterministicRandom repeated =
                DeterministicRandoms.fromDerivedSeed(seeds, "terrain");
        boolean equal = terrain.nextLong() == repeated.nextLong();
        if (!equal) throw new AssertionError("Seed derivation changed");
        System.out.println(equal);
    }
}`)}
          <p>This prints <code>true</code>. <code>SeedSequence</code> derives <code>mix64(root XOR fnv1a(tag))</code> from the tag's UTF-8 bytes. Derivation has no mutable cursor. The same root and exact tag repeat the seed; text is not normalized. Different tags can collide and do not promise statistically independent streams.</p>`
        },
        {
          id: 'weighted-selection', title: 'Select weighted items',
          html: `<p>A weight expresses a relative share, in any common unit. Weights <code>1, 3, 0</code> give the first two entries relative shares of one quarter and three quarters. A zero weight excludes the entry. Each draw selects one entry with replacement.</p>
          ${table(['API', 'Use and constraints'], [
            ['<code>WeightedPicker.pickIndex(weights, rng)</code>', 'Scans the supplied array for each draw: O(n) time and O(1) extra memory. Weights and their positive sum must be finite.'],
            ['<code>WeightedPicker.pick(items, weights, rng)</code>', 'Returns the item at the selected index. The list and weight array must have equal sizes.'],
            ['<code>WeightedSampler.build(weights)</code>', 'Builds owned alias tables in O(n) time and memory. Reuse the sampler for O(1) draws with <code>sampleIndex(rng)</code>.']
          ])}
          ${code('java', 'WeightedChoices.java', `import java.util.List;
import nsk.nu.ashcore.api.random.DeterministicRandom;
import nsk.nu.ashcore.api.random.DeterministicRandoms;
import nsk.nu.ashcore.api.random.WeightedPicker;
import nsk.nu.ashcore.api.random.WeightedSampler;

public final class WeightedChoices {
    public static void main(String[] args) {
        DeterministicRandom rng = DeterministicRandoms.splitMix64(7L);
        List<String> items = List.of("stone", "copper", "gold");
        String selected = WeightedPicker.pick(
                items, new double[]{0, 5, 0}, rng);
        if (!selected.equals("copper")) throw new AssertionError(selected);

        double[] weights = {0, 5, 0};
        WeightedSampler sampler = WeightedSampler.build(weights);
        weights[0] = 100; // The built sampler owns its tables.
        int index = sampler.sampleIndex(rng);
        if (index != 1) throw new AssertionError(index);
        System.out.println(selected + ", " + index);
    }
}`)}
          <p>This prints <code>copper, 1</code>. Both APIs require a nonempty array of finite, nonnegative weights with at least one positive value. The alias builder scales weights before summation, so it can handle finite weights whose unscaled sum would overflow. Rebuild it when the intended weights change.</p>
          <p>Both selection methods consume one uniform-double draw per selection. They use different selection algorithms: equal seeds and weights do not imply identical picked indices across the two APIs. Relative probabilities use double precision and finite random resolution; extremely small relative weights can become unselectable.</p>`
        },
        {
          id: 'halton-sequences', title: 'Generate Halton samples',
          html: `<p>A Halton sequence spreads consecutive samples through a unit interval, square, or cube without an RNG. It is useful when you want repeatable coverage. Its samples are correlated by construction; they are not independent random draws.</p>
          ${code('java', 'HaltonSamples.java', `import nsk.nu.ashcore.api.math.Vector2;
import nsk.nu.ashcore.api.random.Halton2DSequence;
import nsk.nu.ashcore.api.random.HaltonSequence;
import nsk.nu.ashcore.api.random.LowDiscrepancy;

public final class HaltonSamples {
    public static void main(String[] args) {
        HaltonSequence sequence = new HaltonSequence(2);
        double a = sequence.next();
        double b = sequence.next();
        double c = sequence.next();
        if (a != 0.5 || b != 0.25 || c != 0.75) {
            throw new AssertionError("Unexpected Halton values");
        }
        sequence.reset();
        if (sequence.index() != 0 || sequence.next() != 0.5) {
            throw new AssertionError("Unexpected reset");
        }
        Vector2 point = new Halton2DSequence().nextUnitSquare();
        if (point.x() != 0.5 || Math.abs(point.y() - 1.0 / 3) > 1e-12) {
            throw new AssertionError(point);
        }
        if (LowDiscrepancy.halton(0, 2) != 0.0) {
            throw new AssertionError("Index zero must be zero");
        }
        System.out.println(a + ", " + b + ", " + c);
    }
}`)}
          <p>The incremental sequence advances before returning: its first sample is index 1. Direct <code>LowDiscrepancy.halton(index, base)</code> also accepts index 0, which returns zero. A base must exceed 1. Use distinct prime bases across dimensions; the defaults are 2 and 3 for 2D, and 2, 3, and 5 for 3D.</p>
          <p>Incremental generation has amortized O(1) work over consecutive calls, with O(log<sub>base</sub> index) worst-case work and state. Rounding can drift from direct evaluation. The mutable sequences require exclusive access and throw after <code>Integer.MAX_VALUE</code> samples until reset.</p>`
        },
        {
          id: 'compare-samples', title: 'Compare random and Halton coverage',
          html: '<p>Both panels contain the same number of points. The SplitMix64 panel starts with seed <code>1337</code> and draws X and Y in order. The Halton panel evaluates <code>LowDiscrepancy.halton(i, 2)</code> and <code>halton(i, 3)</code> for indices 1 through N. Its first point is <code>(0.5, 1/3)</code>.</p><div data-diagram="sampling"></div><p>Increase the sample count to reveal more of each sequence. The highlighted ring marks the last point. Changing the seed changes only the random panel; Halton samples have no seed. Clusters and gaps can occur in a finite random sample, while Halton aims for repeatable coverage. It does not guarantee a minimum distance between points.</p><p>Switch to <strong>Concentric disk</strong> to apply the same <code>mapToConcentricDisk(u, v)</code> mapping to both panels. The source square is <code>[0, 1)²</code>; the target disk has radius one. These 2D samples have no block unit until the caller scales them.</p>'
        },
        {
          id: 'geometric-sampling', title: 'Map samples to geometric domains',
          html: `<p><code>LowDiscrepancy</code> accepts unit-interval inputs and returns <a href="#/math">vectors</a>. Keep each input in <code>[0, 1)</code>; these mapping methods do not validate every domain condition. All radii below are one, and the direction mappings use +Y as their axis.</p>
          ${table(['Mapping', 'Result'], [
            ['<code>mapToConcentricDisk(u, v)</code>', 'A 2D point on or inside the unit disk. <code>Halton2DSequence.nextConcentricDisk()</code> combines sampling and mapping.'],
            ['<code>mapToUniformHemisphere(u, v)</code>', 'A unit direction with nonnegative Y. Also available as <code>nextUniformHemisphereYUp()</code> on the 2D sequence.'],
            ['<code>mapToUniformSphere(u, v)</code>', 'A direction on the unit sphere surface. Use <code>Halton3DSequence.nextUnitSphereDirection()</code> for a sequence.'],
            ['<code>mapToUniformSphereVolume(u, v, w)</code>', 'A point inside the unit sphere, using radius <code>cbrt(w)</code>. Available as <code>nextUnitSpherePoint()</code>.'],
            ['<code>mapToUniformCone(u, v, cosThetaMax)</code>', 'A direction around +Y. Supply the cosine of the half-angle; keep it in <code>[-1, 1]</code>. The sequence method <code>nextConeYUp(halfAngleRad)</code> takes radians directly.'],
            ['<code>orientYUpToNormal(local, normal)</code>', 'Rotates the local Y-up frame toward a nonzero world-space normal. The method normalizes that normal.']
          ])}
          <p>Each 3D convenience method advances all three underlying Halton dimensions, even when its mapping uses only two. Choose the same sequence type and method order to repeat samples. Scale points and add your center to place them inside application geometry; rotate directions without adding translation. See <a href="#/transforms">Transforms</a> and <a href="#/geometry">Geometry</a>.</p>`
        },
        {
          id: 'distributions-and-shuffle', title: 'Use distributions and shuffles',
          html: `${table(['Method', 'Behavior'], [
            ['<code>Distributions.gaussian01(rng)</code>', 'A standard-normal sample from Box–Muller. Consumes two uniform draws and returns one value; no second value is cached.'],
            ['<code>Distributions.exponential(rng, lambda)</code>', 'Returns <code>-log(u) / lambda</code>. Supply finite positive <code>lambda</code>, in reciprocal units of the result. Nonpositive lambda throws.'],
            ['<code>Distributions.poisson(rng, lambda)</code>', 'Returns a nonnegative event count using Knuth’s algorithm. Use finite, small positive lambda. Nonpositive lambda returns zero without drawing.'],
            ['<code>Permutation.shuffle(array, rng)</code>', 'Shuffles an <code>int[]</code> in place with Fisher–Yates. For n entries it uses max(0, n − 1) uniform draws and O(n) time.']
          ])}
          <p>The Gaussian and exponential methods clamp their logarithm input to <code>1e-12</code>. Their tails are therefore bounded by that implementation choice. Distribution methods do not consistently reject NaN or infinity; validate parameters before calling them. Large Poisson lambda also increases work and eventually underflows <code>exp(-lambda)</code>, so this routine is not a general large-lambda sampler.</p>
          <p>For a fixed-size sample from a stream, use <a href="#/statistics?section=reservoir-sampling">ReservoirSampler</a>. For values tied to coordinates, use <a href="#/noise">Noise</a>. The <a href="#/api-index">API index</a> lists the related types.</p>`
        }
      ]
    },
    {
      id: 'noise', category: 'Data & generation', title: 'Noise',
      description: 'Sample Perlin and hash-grid noise, combine octaves, and choose coordinate and amplitude scales.',
      kind: 'guide', readingTime: 8,
      intro: '<p>Noise maps coordinates to repeatable scalar values. Your application chooses what those coordinates and values mean: a terrain height, density, or visual parameter. Ashcore does not read a Minecraft world or place blocks.</p>',
      sections: [
        {
          id: 'perlin-noise', title: 'Sample Perlin noise',
          html: `<p>Create <code>PerlinNoise</code> once, then sample it at 2D or 3D coordinates. Construction consumes 255 ordered uniform-double draws from the supplied generator. Later changes to that generator do not change the built noise table.</p>
          ${code('java', 'TerrainNoise.java', `import nsk.nu.ashcore.api.noise.FractalNoise;
import nsk.nu.ashcore.api.noise.PerlinNoise;
import nsk.nu.ashcore.api.random.DeterministicRandoms;

public final class TerrainNoise {
    public static void main(String[] args) {
        PerlinNoise noise = new PerlinNoise(
                DeterministicRandoms.splitMix64(1337L));
        double blockX = 128.0;
        double blockZ = 64.0;
        double value = FractalNoise.fbm(
                noise, blockX * 0.02, blockZ * 0.02, 5, 2.0, 0.5);
        int height = (int) Math.floor(64.0 + value * 12.0);
        if (!Double.isFinite(value)) throw new AssertionError(value);
        if (value != FractalNoise.fbm(
                noise, blockX * 0.02, blockZ * 0.02, 5, 2.0, 0.5)) {
            throw new AssertionError("Sampling changed the noise");
        }
        System.out.println("height=" + height);
    }
}`)}
          <p>This application example interprets X and Z as block coordinates. The factor <code>0.02</code> converts them to dimensionless noise coordinates: one lattice unit spans 50 blocks. The caller chooses base height 64, vertical scale 12, and floor rounding. These values are example settings, not Ashcore defaults.</p>
          <p>Sampling the same instance at the same coordinates repeats the value without changing state. Perlin sampling allocates no objects. Its approximate output range is <code>[-1, 1]</code>, and its lattice repeats every 256 units. The 2D overload is a separate gradient calculation; do not assume it equals a 3D slice at Z = 0.</p>`
        },
        {
          id: 'explore-noise', title: 'Explore frequency and octaves',
          html: '<p>The preview uses the <code>TerrainNoise</code> example: seed <code>1337</code>, frequency <code>0.02</code>, five octaves, lacunarity <code>2</code>, and gain <code>0.5</code>. The marker samples block coordinates <code>(128, 64)</code>. X and Z locate a point on the patch; Y is its displayed height.</p><div data-diagram="noise"></div><p>Higher frequency fits more base-noise features into the same 64 × 64 block patch. Each additional octave samples at twice the previous frequency; gain multiplies its amplitude. One octave shows the base Perlin sample. Zero octaves return zero everywhere, so this height example becomes a flat surface at Y = 64.</p><p>The 2D map and 3D surface use the same values. Fractal sums are not normalized, and the color scale stays fixed when parameters change. A finite sample grid cannot show detail between its points. See <a href="#/noise?section=fractal-octaves">Combine octaves</a> for the sum and amplitude rules.</p>'
        },
        {
          id: 'coordinate-limits', title: 'Keep coordinates within the lattice',
          html: `${table(['API', 'Accepted coordinates'], [
            ['<code>PerlinNoise.sample</code>', 'Each coordinate must be finite and in <code>[-2147483648, 2147483648)</code>.'],
            ['<code>HashGridNoise.value2D</code>', 'Two integer cell coordinates; accepts the full Java <code>int</code> range.'],
            ['<code>HashGridNoise.value3D</code>', 'Each coordinate must be finite and in <code>[-2147483648, 2147483647)</code>, leaving room for the upper corner.']
          ])}
          <p>Perlin and 3D hash-grid sampling reject out-of-range or non-finite coordinates with <code>IllegalArgumentException</code>. Check every octave after frequency scaling. A valid initial coordinate can leave the domain after repeated multiplication by lacunarity.</p>
          <p>Noise repeatability is scoped to the same library version and environment. Keep the same algorithm, table construction, and inputs. The noise API does not promise identical floating-point bits across releases or platforms. See <a href="#/random?section=seed-and-state">seed and state</a> for separate generator guarantees.</p>`
        },
        {
          id: 'fractal-octaves', title: 'Combine octaves',
          html: `<p>An octave samples the same base function at a different frequency and amplitude. <code>lacunarity</code> multiplies coordinates after each octave; <code>gain</code> multiplies amplitude. Both are dimensionless. An octave count of zero or less returns zero without calling the base function.</p>
          ${table(['Combination', 'Per-octave contribution'], [
            ['<code>fbm</code> — 2D and 3D', '<code>sample × amplitude</code>, starting at amplitude 1.'],
            ['<code>turbulence</code> — 2D', '<code>abs(sample) × amplitude</code>, starting at amplitude 1.'],
            ['<code>ridge</code> — 2D', '<code>(1 − abs(sample))² × amplitude</code>, starting at amplitude 0.5.']
          ])}
          ${code('java', 'OctaveSum.java', `import nsk.nu.ashcore.api.noise.FractalNoise;
import nsk.nu.ashcore.api.noise.Noise2D;

public final class OctaveSum {
    public static void main(String[] args) {
        Noise2D constant = (x, y) -> 1.0;
        double sum = FractalNoise.fbm(constant, 0, 0, 3, 2.0, 0.5);
        if (sum != 1.75) throw new AssertionError(sum);
        System.out.println(sum);
    }
}`)}
          <p>The output is <code>1.75</code>: the method adds amplitudes 1, 0.5, and 0.25. Fractal results are unnormalized sums and can exceed the base noise range. If your application requires normalization, compute and apply an amplitude divisor yourself.</p>
          <p>The base function is called once per positive octave, in increasing octave order. Runtime grows with octave count and callback cost; the combination itself uses O(1) extra memory. No generic error bound or automatic parameter validation is provided. Keep coordinates and intermediate arithmetic finite. A custom callback determines its own repeatability and thread safety.</p>`
        },
        {
          id: 'hash-grid', title: 'Use hash-grid values',
          html: `<p><code>HashGridNoise.value2D(cellX, cellY, seed)</code> assigns a value in <code>[0, 1)</code> to an integer cell. It is a direct cell lookup without interpolation. The 3D method accepts doubles and interpolates eight corner values using cubic smoothstep; its result is in <code>[0, 1)</code> within rounding.</p>
          <p>Both methods are stateless. They depend on coordinates and seed, so querying one location does not advance a sequence for another. <code>Noise2D</code> and <code>Noise3D</code> are single-method interfaces; adapt a hash-grid call with a lambda when passing it to <code>FractalNoise</code>.</p>
          <p>For independent random choices, use <a href="#/random">Random and sampling</a>. For coordinate keys, use <a href="#/utilities?section=morton-encoding">Morton encoding</a>. Return to the <a href="#/quick-start">Quick start</a> to combine noise with other library features.</p>`
        }
      ]
    },
    {
      id: 'statistics', category: 'Data & generation', title: 'Streaming statistics',
      description: 'Track means, variance, recent extrema, approximate percentiles, and stream samples with bounded memory.',
      kind: 'guide', readingTime: 8,
      intro: '<p>Add one measurement at a time instead of retaining an entire history. Choose a statistic based on whether you need all samples, the last N samples, or a weighted history. A sample is one call; these classes do not measure elapsed time.</p>',
      sections: [
        {
          id: 'choose-a-statistic', title: 'Choose what the result represents',
          html: `${table(['Type', 'History and result', 'Memory'], [
            ['<code>RunningStats</code>', 'All samples: count, mean, population variance, and sample variance.', 'O(1)'],
            ['<code>WindowedMean</code>', 'Last N samples: arithmetic mean over the available window.', 'O(N)'],
            ['<code>SlidingWindowMinMax</code>', 'Last N samples: minimum and maximum.', 'O(N)'],
            ['<code>ExponentialMovingAverage</code>', 'All samples with exponentially decreasing weights.', 'O(1)'],
            ['<code>P2Quantile</code>', 'All samples: an approximate estimate of one chosen quantile.', 'O(1)'],
            ['<code>ReservoirSampler&lt;T&gt;</code>', 'Up to k sampled entries from a stream.', 'O(k)']
          ])}
          <p>These objects are mutable and require exclusive access. Floating-point statistics depend on sample order and rounding. They contain no scheduler or clock; a window of 100 means 100 calls, whether those calls span one second or one hour.</p>`
        },
        {
          id: 'mean-and-variance', title: 'Track mean and variance',
          html: `<p><code>RunningStats</code> uses Welford's one-pass algorithm. Adding a sample takes O(1) time. <code>variance()</code> divides the squared deviations by the sample count; <code>sampleVariance()</code> divides by count minus one.</p>
          ${code('java', 'MeasurementStats.java', `import nsk.nu.ashcore.api.stats.RunningStats;

public final class MeasurementStats {
    public static void main(String[] args) {
        RunningStats stats = new RunningStats();
        for (double milliseconds : new double[]{1, 2, 3}) {
            stats.add(milliseconds);
        }
        if (stats.count() != 3 || stats.mean() != 2.0
                || stats.sampleVariance() != 1.0) {
            throw new AssertionError("Unexpected statistics");
        }
        System.out.println("mean=" + stats.mean());
        System.out.println("sampleVariance=" + stats.sampleVariance());
    }
}`)}
          <p>The output is <code>mean=2.0</code> and <code>sampleVariance=1.0</code>. The mean has units of milliseconds; variance has units of milliseconds squared. Population variance for these samples is <code>2 / 3</code>.</p>
          <p>An empty instance returns NaN for both mean and population variance. Sample variance remains NaN until two samples arrive. Non-finite samples and an exhausted <code>long</code> counter are rejected before mutation. Finite inputs can still overflow intermediate differences or squared deviations. Counts above 2<sup>53</sup> lose precision when converted to double. Create a new instance to restart; there is no reset or merge method.</p>`
        },
        {
          id: 'windows-and-smoothing', title: 'Track recent measurements',
          html: `${code('java', 'RecentMeasurements.java', `import nsk.nu.ashcore.api.stats.ExponentialMovingAverage;
import nsk.nu.ashcore.api.stats.SlidingWindowMinMax;
import nsk.nu.ashcore.api.stats.WindowedMean;

public final class RecentMeasurements {
    public static void main(String[] args) {
        WindowedMean mean = new WindowedMean(3);
        SlidingWindowMinMax extrema = new SlidingWindowMinMax(3);
        for (double sample : new double[]{1, 2, 3, 4}) {
            mean.add(sample);
            extrema.add(sample);
        }
        if (mean.mean() != 3 || extrema.min() != 2 || extrema.max() != 4) {
            throw new AssertionError("Unexpected window");
        }
        ExponentialMovingAverage ema = new ExponentialMovingAverage(0.5);
        ema.add(10);
        if (ema.add(20) != 15) throw new AssertionError("Unexpected EMA");
        System.out.println("windowMean=" + mean.mean() + ", ema=" + ema.value());
    }
}`)}
          <p>This prints <code>windowMean=3.0, ema=15.0</code>. The three-entry window contains 2, 3, and 4 after the fourth call. Before a window fills, both window classes use the samples available so far. Their empty queries return NaN. Constructors reject window sizes of zero or less.</p>
          <p><code>WindowedMean.add</code> returns the updated mean in O(1) time. <code>SlidingWindowMinMax.add</code> has amortized O(1) work. Its internal position is an unguarded signed int; recreate it before <code>Integer.MAX_VALUE</code> additions to avoid overflow. Neither class has reset.</p>
          <p>An EMA initializes to the first sample. Later updates use <code>alpha × sample + (1 − alpha) × previous</code>. Use finite <code>alpha</code> in <code>(0, 1]</code>; 1 follows the newest sample directly. <code>withHalfLife(h)</code> derives alpha as <code>1 − 0.5^(1/h)</code>, where h is a positive finite number of samples, not seconds. Very large h can round alpha to zero and be rejected.</p>
          ${note('Validate samples before adding them', '<p>The window classes and EMA do not reject NaN or infinity. Such values can invalidate later results. Check <code>Double.isFinite(sample)</code> before updating any of them. EMA also accepts a NaN alpha in 1.2.0; validate it yourself.</p>', true)}`
        },
        {
          id: 'percentiles', title: 'Estimate a percentile',
          html: `<p><code>P2Quantile</code> estimates one quantile from all samples using five markers. Pass a fraction strictly between zero and one: <code>0.95</code> requests the 95th percentile. The estimator uses O(1) work and memory per sample and does not retain a sliding window.</p>
          ${code('java', 'EstimatePercentile.java', `import nsk.nu.ashcore.api.stats.P2Quantile;

public final class EstimatePercentile {
    public static void main(String[] args) {
        P2Quantile p95 = new P2Quantile(0.95);
        for (int i = 0; i < 4; i++) p95.add(7.0);
        if (!Double.isNaN(p95.estimate())) {
            throw new AssertionError("Estimator needs five samples");
        }
        for (int i = 0; i < 100; i++) p95.add(7.0);
        if (p95.estimate() != 7.0) throw new AssertionError(p95.estimate());
        System.out.println("p95=" + p95.estimate());
        p95.reset();
    }
}`)}
          <p>The example prints <code>p95=7.0</code>. Before five samples, <code>estimate()</code> returns NaN. At the fifth sample, the initial estimate is the middle of the sorted five values for every requested quantile; it adapts as later samples arrive. Do not treat early estimates as an exact percentile.</p>
          <p>The estimate is approximate, depends on input order, and has no general error bound. Non-finite samples throw before mutation. Intermediate differences and products must remain representable. The estimator accepts at most <code>Integer.MAX_VALUE</code> samples per reset; <code>reset()</code> restarts the five-sample bootstrap.</p>`
        },
        {
          id: 'reservoir-sampling', title: 'Keep a sample from a stream',
          html: `<p><code>ReservoirSampler&lt;T&gt;</code> keeps up to k entries while observing a stream of unknown length. The first k offers fill the reservoir without drawing randomness. Each subsequent offer consumes one uniform-double draw and can replace an existing entry.</p>
          ${code('java', 'StreamSample.java', `import java.util.List;
import nsk.nu.ashcore.api.random.DeterministicRandoms;
import nsk.nu.ashcore.api.stats.ReservoirSampler;

public final class StreamSample {
    public static void main(String[] args) {
        ReservoirSampler<String> sampler = new ReservoirSampler<>(
                2, DeterministicRandoms.splitMix64(123L));
        sampler.offer("north");
        sampler.offer("south");
        List<String> before = sampler.snapshot();
        sampler.offer("west");
        if (sampler.size() != 2 || sampler.seenCount() != 3) {
            throw new AssertionError("Unexpected reservoir counters");
        }
        sampler.reset();
        if (!before.equals(List.of("north", "south")) || sampler.size() != 0) {
            throw new AssertionError("Snapshot changed");
        }
        System.out.println(before);
    }
}`)}
          <p>The output is <code>[north, south]</code>. <code>snapshot()</code> returns an unmodifiable copy of filled slots; later offers and resets do not change its membership. The copy is shallow, so mutable elements remain shared references. Null values are allowed. <code>snapshotRaw()</code> copies the entire capacity, including unfilled null slots.</p>
          <p>Capacity must be positive. Offering an entry takes O(1) work; snapshots and reset take O(k). Each stream position is eligible, so repeated equal values remain separate entries. Ideal Algorithm R gives each entry probability <code>min(1, k / n)</code>. This implementation scales a finite-resolution double, making probabilities approximate, particularly beyond 2<sup>53</sup> offers.</p>
          <p><code>reset()</code> clears entries and counters but leaves the supplied RNG advanced. To replay the same sample, create a fresh generator and sampler and repeat the same stream order. An offer at <code>Long.MAX_VALUE</code> seen entries throws before mutation. See <a href="#/random">Random and sampling</a> for the generator contract.</p>`
        },
        {
          id: 'interpret-results', title: 'Interpret missing or invalid results',
          html: `<p>NaN can mean that a statistic is not ready, or that unchecked input or arithmetic invalidated its state. Track initialization and validate measurements before adding them. See <a href="#/troubleshooting">Troubleshooting</a> for numerical symptoms and <a href="#/api-index">API index</a> for class names.</p>`
        }
      ]
    },
    {
      id: 'utilities', category: 'Reference', title: 'Hashes, ranges, and SPI',
      navTitle: 'Utilities', description: 'Hash stable input, encode signed coordinates, work with inclusive ranges, and load identified providers.',
      kind: 'reference', readingTime: 6,
      intro: '<p>These utilities support identifiers, numeric boundaries, and extension points. They operate on values supplied by your application. The <a href="#/overview">Overview</a> describes how they fit into the library.</p>',
      sections: [
        {
          id: 'hashes', title: 'Hash text and mix seeds',
          html: `<p><code>Hash64.fnv1a</code> accepts a byte array or a string. The string overload hashes its UTF-8 bytes. <code>Hash64.mix64(long)</code> mixes a 64-bit input using SplitMix-style operations. Both outputs are stable throughout Ashcore 1.x; signed long overflow is intentional arithmetic modulo 2<sup>64</sup>.</p>
          ${code('java', 'StableHashes.java', `import java.nio.charset.StandardCharsets;
import nsk.nu.ashcore.api.hash.Hash64;

public final class StableHashes {
    public static void main(String[] args) {
        String tag = "terrain";
        long textHash = Hash64.fnv1a(tag);
        long byteHash = Hash64.fnv1a(tag.getBytes(StandardCharsets.UTF_8));
        if (textHash != byteHash) throw new AssertionError("Encoding differs");
        if (Hash64.mix64(0) != 0) throw new AssertionError("Unexpected mixer");
        System.out.println(textHash == byteHash);
    }
}`)}
          <p>This prints <code>true</code>. FNV-1a work grows with byte length; the string overload also allocates UTF-8 bytes. Hash collisions are possible. These functions are noncryptographic: use a suitable security API for passwords, authentication, or tamper detection. For tagged random seeds, <a href="#/random?section=seed-and-state">SeedSequence</a> supplies the combination.</p>`
        },
        {
          id: 'morton-encoding', title: 'Pack coordinates with Morton encoding',
          html: `<p><code>Morton</code> interleaves coordinate bits into a Z-order code. Encoding and matching decoders preserve coordinates within the supported range. A Morton code is a reversible coordinate key, not a hashed identifier or distance measurement.</p>
          ${table(['Encoding', 'Coordinate bounds and decoding'], [
            ['<code>encode2D(x, y)</code>', 'Both axes accept the full signed 32-bit range. Decode with <code>decode2D_X(code)</code> and <code>decode2D_Y(code)</code>.'],
            ['<code>encode3D(x, y, z)</code>', 'Every axis must lie in <code>[-1048576, 1048575]</code>, inclusive. Decode with <code>decode3D_X</code>, <code>decode3D_Y</code>, and <code>decode3D_Z</code>. Out-of-range input throws <code>IllegalArgumentException</code>.']
          ])}
          ${code('java', 'CoordinateKeys.java', `import nsk.nu.ashcore.api.hash.Morton;

public final class CoordinateKeys {
    public static void main(String[] args) {
        long key = Morton.encode3D(-12, 64, 23);
        int x = Morton.decode3D_X(key);
        int y = Morton.decode3D_Y(key);
        int z = Morton.decode3D_Z(key);
        if (x != -12 || y != 64 || z != 23) {
            throw new AssertionError("Coordinate round trip failed");
        }
        System.out.println(x + ", " + y + ", " + z);
    }
}`)}
          <p>The output is <code>-12, 64, 23</code>. Ashcore biases signed coordinates before interleaving; this is not Minecraft's block-position packing format. Keep the encoding dimension with stored data. The 2D code uses all 64 bits and can be negative as a Java long. Use unsigned comparison if sorting those codes by their unsigned numeric Z-order.</p>
          <p>A coordinate key does not establish whether a shape intersects that cell. Use <a href="#/raycasting">Raycasting</a> or <a href="#/collisions">Collisions</a> for geometric queries.</p>`
        },
        {
          id: 'inclusive-ranges', title: 'Use inclusive ranges',
          html: `<p><code>IntRange</code> and <code>DoubleRange</code> are immutable records with inclusive bounds <code>[min, max]</code>. Their constructors reject reversed ordered bounds. The <code>of(a, b)</code> factories reorder valid numeric arguments so the smaller one becomes the minimum.</p>
          ${table(['Operation', 'Result'], [
            ['<code>contains(value)</code>', 'Includes both endpoints.'],
            ['<code>clamp(value)</code>', 'Returns the nearest endpoint for an outside value, or the original value when inside.'],
            ['<code>intersects(other)</code>', 'Returns true when the ranges overlap, including contact at a shared endpoint.'],
            ['<code>expandToInclude(value)</code>', 'Returns a containing range; returns the same instance when expansion is unnecessary.'],
            ['<code>IntRange.sizeInclusive()</code>', 'Returns <code>max − min + 1</code> as a long; the full int range has 4,294,967,296 entries.'],
            ['<code>DoubleRange.width()</code>', 'Returns <code>max − min</code>; this is a width, not an element count.']
          ])}
          ${code('java', 'InclusiveRanges.java', `import nsk.nu.ashcore.api.math.DoubleRange;
import nsk.nu.ashcore.api.math.IntRange;

public final class InclusiveRanges {
    public static void main(String[] args) {
        IntRange levels = IntRange.of(5, 2);
        DoubleRange distance = new DoubleRange(0.0, 8.0);
        if (levels.sizeInclusive() != 4 || !levels.contains(5)
                || levels.clamp(9) != 5 || distance.width() != 8.0) {
            throw new AssertionError("Unexpected range");
        }
        System.out.println(levels.min() + ".." + levels.max());
    }
}`)}
          <p>This prints <code>2..5</code>. Random-generator bounds use a different convention: their maximum is excluded. Do not convert an inclusive maximum to <code>max + 1</code> without checking overflow.</p>
          ${note('DoubleRange does not validate finiteness', '<p>In 1.2.0, NaN endpoints can pass construction, and <code>contains(Double.NaN)</code> returns true because both comparisons are false. Validate endpoints and queried values with <code>Double.isFinite</code> when using finite ranges. Infinity can also produce an infinite or NaN width.</p>', true)}
          <p>See <a href="#/math">Math and vectors</a> for interpolation and tolerance helpers.</p>`
        },
        {
          id: 'service-registry', title: 'Load identified providers',
          html: `<p><code>ServiceRegistry&lt;T&gt;</code> loads Java service providers whose service type extends <code>Identified</code>. Each provider supplies a nonblank <code>id()</code>, conventionally <code>namespace:name</code>. IDs must be unique within the service type. Select a provider by its explicit ID; iteration and initialization order are unspecified.</p>
          ${table(['Method', 'Contract'], [
            ['<code>ServiceRegistry.of(type)</code>', 'Builds a fresh registry using the thread context class loader, then the service type or system loader as fallbacks.'],
            ['<code>ServiceRegistry.of(type, loader)</code>', 'Builds a fresh registry with the explicit loader.'],
            ['<code>get(id)</code>', 'Returns an <code>Optional</code>; empty when no provider has that ID.'],
            ['<code>require(id)</code>', 'Returns the provider, or throws <code>IllegalStateException</code> when missing.'],
            ['<code>contains(id)</code>', 'Returns whether the ID is present.'],
            ['<code>ids()</code>, <code>all()</code>', 'Return immutable collections with unspecified iteration order. Provider objects are not copied.'],
            ['<code>size()</code>, <code>isEmpty()</code>', 'Describe the providers loaded into this registry.']
          ])}
          <p>All lookup methods reject null or blank IDs with <code>IllegalArgumentException</code>. Loading is eager: creating the registry executes provider initialization, constructors, and ID methods. Duplicate or invalid provider IDs throw <code>IllegalStateException</code>. ServiceLoader errors propagate, and completed provider side effects are not rolled back.</p>
          <p>For a classpath provider, package a <code>META-INF/services/&lt;service-interface-binary-name&gt;</code> file listing provider binary names. Each classpath provider needs a public no-argument constructor and must implement the service interface. Use an explicit loader when your application isolates extensions in separate class loaders.</p>
          <p>Ashcore 1.2.0 ships no production service-registration files. The SplitMix64 implementation takes a seed and is created through <code>DeterministicRandoms</code>; its identifier alone does not register it with ServiceLoader. Registry membership is immutable, but the returned providers may contain mutable state and need synchronization.</p>
          <p>The <a href="#/api-index">API index</a> lists extension interfaces. For missing-provider errors, see <a href="#/troubleshooting">Troubleshooting</a>.</p>`
        }
      ]
    }
  );
}());
