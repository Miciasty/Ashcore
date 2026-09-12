(() => {
  const {code, table, note} = window.WIKI_HTML;
  const card = (id, label, title, description) => `<a class="link-card block rounded-[6px] border border-line p-[18px] [background:linear-gradient(145deg,var(--surface),transparent)] transition-[border-color] duration-150 hover:border-accent" href="#/${id}"><span>${label} <b>↗</b></span><h3>${title}</h3><p>${description}</p></a>`;
  window.WIKI_PAGES.push(
    {
      id: 'overview', category: 'Getting started', title: 'Ashcore documentation', navTitle: 'Overview', kind: 'guide', readingTime: 3,
      description: 'Math, geometry, and repeatable generation for your Java project.',
      intro: '<p>Ashcore is a Java library for vectors, transforms, primitive collision queries, sampling, noise, hashing, and streaming statistics. Use these building blocks inside a Minecraft plugin or any Java application.</p>',
      sections: [
        {id: 'start-building', title: 'Start with a working example', html: `<p>This WIKI documents <strong>Ashcore 1.2.0</strong>. You need <strong>Java 21 or newer</strong>. The library has no production dependencies outside the Java standard library.</p><div class="link-cards my-[22px] grid grid-cols-2 gap-[13px] max-[680px]:grid-cols-1">${card('installation','01 · SETUP','Add Ashcore','Add the Maven or Gradle dependency and package it with your application.')}${card('quick-start','02 · FIRST RESULT','Run a geometry query','Intersect a ray with a box and check the distance and hit point.')}${card('random','03 · GENERATION','Repeat a random sequence','Choose an explicit generator, seed, and call order.')}${card('api-index','04 · REFERENCE','Find an API type','Browse the public types by package and follow their contracts.')}</div>`},
        {id: 'choose-a-tool', title: 'Choose a tool for the job', html: table(['Your task','Start here','Result'], [
          ['Move or rotate a point','<a href="#/math">Vectors</a> and <a href="#/transforms">transforms</a>','A new value in the coordinate system you supply.'],
          ['Test a targeting ray','<a href="#/raycasting">Ray queries</a>','A primitive contact distance, point, normal, or interval.'],
          ['Measure shape overlap','<a href="#/collisions">Collision queries</a>','Overlap, contact witnesses, or time of contact during translation.'],
          ['Generate terrain samples','<a href="#/noise">Noise</a>','A numeric sample to scale and convert into your terrain model.'],
          ['Choose weighted rewards','<a href="#/random">Random and sampling</a>','An index or item selected from caller-supplied weights.'],
          ['Track measurements','<a href="#/statistics">Streaming statistics</a>','An average, variance, window statistic, or retained sample.'],
          ['Build stable keys or load extensions','<a href="#/utilities">Utilities and services</a>','Hashes, packed integers, ranges, or providers by ID.']
        ])},
        {id: 'library-boundary', title: 'Where Ashcore fits', html: '<p>Your application supplies coordinates, units, seeds, and objects to test. Ashcore computes values from those inputs. It does not read a Minecraft world or register server commands, permissions, listeners, or configuration files.</p><p>A hit on a bounding box confirms contact with that box. If the box surrounds a detailed object, test that object separately. Collision queries do not apply forces or move entities.</p><p>Ashcore is the lowest Blackframe layer. Voxel storage belongs to Ashgrid; coordinate-frame graphs belong to Ashspace. Scene traversal and navigation belong to higher layers. You can use Ashcore on its own.</p>'},
        {id: 'terms', title: 'Terms used in this WIKI', html: table(['Term','Meaning'], [
          ['Position unit','The unit chosen by your application. Use blocks when supplying Minecraft positions, and use the same unit for bounds and radii.'],
          ['AABB / OBB','An axis-aligned box / a box with its own rotated axes. See <a href="#/geometry">geometry</a>.'],
          ['Ray distance','Distance from a ray origin along its normalized direction. A segment parameter instead measures a fraction of its length.'],
          ['Deterministic sequence','A repeatable result for a fixed algorithm, version, initial state, inputs, and call order.'],
          ['Mutable state','Data that an operation changes, such as the next random draw or accumulated statistics.'],
          ['Contact witness','A point on each tested shape, returned with the normal and penetration depth.']
        ])},
        {id: 'version-and-source', title: 'Version and source', html: '<p>The examples and contracts were checked against the 1.2.0 source. Maven coordinates use <code>dev.nasaka.blackframe:ashcore</code>; Java imports use <code>nsk.nu.ashcore</code>.</p><p>Read <a href="#/migration">migration notes</a> before updating an existing integration. The <a href="https://github.com/Miciasty/Ashcore/tree/v1.2.0">versioned source</a> and <a href="https://central.sonatype.com/artifact/dev.nasaka.blackframe/ashcore/1.2.0">Maven Central artifact</a> identify this release. Ashcore is distributed under the <a href="https://github.com/Miciasty/Ashcore/blob/v1.2.0/LICENSE">Apache License 2.0</a>.</p>'}
      ]
    },
    {
      id: 'installation', category: 'Getting started', title: 'Add Ashcore to your project', navTitle: 'Installation', kind: 'guide', readingTime: 5,
      description: 'Configure Java 21, add the dependency, and make it available at runtime.',
      sections: [
        {id: 'requirements', title: 'Requirements', html: '<p>Use JDK 21 or newer to compile and run the consumer. Ashcore is a library JAR. Add it to your application dependencies; it has no server entry point or <code>plugin.yml</code>.</p>'+table(['Requirement','Value'],[['Java','21 or newer'],['Maven coordinates','<code>dev.nasaka.blackframe:ashcore:1.2.0</code>'],['Repository','Maven Central'],['Java package prefix','<code>nsk.nu.ashcore</code>'],['Production dependencies','Java standard library only'],['Build Ashcore from source','Maven 3.9+ and JDK 21+']])},
        {id: 'maven', title: 'Maven', html: '<p>Add the dependency inside your existing <code>&lt;dependencies&gt;</code> element. Maven Central is available by default.</p>'+code('xml','pom.xml — dependency',`<dependency>
  <groupId>dev.nasaka.blackframe</groupId>
  <artifactId>ashcore</artifactId>
  <version>1.2.0</version>
</dependency>`)+ '<p>Set your project compiler release to 21 or newer. A complete minimal consumer POM is provided in <a href="#/quick-start">the quick start</a>.</p>'},
        {id: 'gradle', title: 'Gradle', html: '<p>In a Gradle Java project, add Maven Central and the dependency. This Kotlin DSL example selects a Java 21 toolchain.</p>'+code('kotlin','build.gradle.kts',`plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.nasaka.blackframe:ashcore:1.2.0")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}`)},
        {id: 'runtime', title: 'Package the library with a Minecraft plugin', html: '<p>The library must also be available when your plugin runs. If the plugin owns a private Ashcore copy, package its classes into the plugin JAR. Maven Shade can include and relocate them.</p><p>Add this plugin inside your consumer POM’s <code>&lt;build&gt;&lt;plugins&gt;</code> element. Replace <code>com.example.myplugin.libs.ashcore</code> with a package owned by your plugin.</p>'+code('xml','pom.xml — private library copy',`<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <version>3.6.2</version>
  <executions>
    <execution>
      <phase>package</phase>
      <goals><goal>shade</goal></goals>
      <configuration>
        <relocations>
          <relocation>
            <pattern>nsk.nu.ashcore</pattern>
            <shadedPattern>com.example.myplugin.libs.ashcore</shadedPattern>
          </relocation>
        </relocations>
      </configuration>
    </execution>
  </executions>
</plugin>`)+ '<p>Keep the original <code>nsk.nu.ashcore</code> imports in your source. Shade rewrites the packaged bytecode. Use one dependency strategy across libraries that exchange Ashcore types; separately relocated copies have different class identities. Preserve the applicable license and notice files when distributing the library.</p><p>This is a consumer packaging example. Adapt it to your plugin’s existing packaging configuration and verify the final JAR. See <a href="https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html">Maven Shade’s relocation documentation</a>.</p>'},
        {id: 'minecraft-inputs', title: 'Supply Minecraft data', html: '<p>Read the position, direction, and bounds through your server API, then copy their numeric components into Ashcore types. Keep every object in a query in the same world and coordinate system. A <code>Vector3</code> does not store a world identifier.</p><p>Ashcore does not select a server thread or scheduler. Follow the server API’s access rules when reading or updating game state. Give mutable generators and statistics a clear owner before using them from asynchronous work.</p>'},
        {id: 'verify', title: 'Verify the dependency', html: code('bash','Consumer project directory',`mvn dependency:tree -Dincludes=dev.nasaka.blackframe:ashcore`)+ '<p>The dependency tree should contain <code>dev.nasaka.blackframe:ashcore:jar:1.2.0</code>. Then <a href="#/quick-start">compile and run the first query</a>. If compilation works but the server reports a missing Ashcore class, inspect the packaged JAR and runtime classpath.</p>'}
      ]
    },
    {
      id: 'quick-start', category: 'Getting started', title: 'Run your first query', navTitle: 'Quick start', kind: 'guide', readingTime: 5,
      description: 'Compile a small Java application and check a ray, a random draw, and an average.',
      sections: [
        {id: 'create-project', title: 'Create a consumer project', html: '<p>Create a directory outside the Ashcore checkout. Save this file as <code>pom.xml</code>. The example compiles with Java 21 and downloads Ashcore from Maven Central.</p>'+code('xml','pom.xml',`<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example</groupId>
  <artifactId>ashcore-example</artifactId>
  <version>1.0.0</version>
  <properties>
    <maven.compiler.release>21</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
  <dependencies>
    <dependency>
      <groupId>dev.nasaka.blackframe</groupId>
      <artifactId>ashcore</artifactId>
      <version>1.2.0</version>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
      </plugin>
    </plugins>
  </build>
</project>`)},
        {id: 'write-example', title: 'Write the example', html: '<p>Save the following file under <code>src/main/java</code>. The ray begins at X = −2 and points toward a box whose first face lies at X = 0. All positions use the same arbitrary unit.</p>'+code('java','AshcoreQuickStart.java',`import java.util.Locale;
import nsk.nu.ashcore.api.collision.CollisionTests;
import nsk.nu.ashcore.api.geometry.AxisAlignedBox;
import nsk.nu.ashcore.api.geometry.Ray;
import nsk.nu.ashcore.api.math.Vector3;
import nsk.nu.ashcore.api.random.DeterministicRandom;
import nsk.nu.ashcore.api.random.DeterministicRandoms;
import nsk.nu.ashcore.api.stats.RunningStats;

public final class AshcoreQuickStart {
    public static void main(String[] args) {
        Ray ray = new Ray(new Vector3(-2, 0, 0), new Vector3(4, 0, 0));
        AxisAlignedBox box = new AxisAlignedBox(
                new Vector3(0, -1, -1), new Vector3(1, 1, 1));
        double distance = CollisionTests.rayVsBoxT(ray, box);
        if (!Double.isFinite(distance)) {
            throw new AssertionError("Expected a box hit");
        }
        Vector3 point = ray.at(distance);

        DeterministicRandom first = DeterministicRandoms.splitMix64(1337L);
        DeterministicRandom replay = DeterministicRandoms.splitMix64(1337L);
        int roll = first.nextInt(1, 7);
        boolean repeated = roll == replay.nextInt(1, 7);

        RunningStats stats = new RunningStats();
        stats.add(2);
        stats.add(4);
        stats.add(6);
        if (distance != 2 || point.x() != 0 || !repeated || stats.mean() != 4) {
            throw new AssertionError("Unexpected result");
        }
        System.out.printf(Locale.ROOT, "distance=%.1f, hitX=%.1f%n", distance, point.x());
        System.out.printf(Locale.ROOT, "repeat=%s, mean=%.1f%n", repeated, stats.mean());
    }
}`)},
        {id: 'run', title: 'Compile and run', html: '<p>Run these commands in the consumer directory. The first command also copies runtime dependencies into <code>target/dependency</code>.</p>'+code('powershell','Windows PowerShell',`mvn compile dependency:copy-dependencies -DincludeScope=runtime
java -cp "target/classes;target/dependency/*" AshcoreQuickStart`)+ '<p>On Linux or macOS, use a colon in the classpath:</p>'+code('bash','Linux / macOS',`java -cp "target/classes:target/dependency/*" AshcoreQuickStart`)+code('output','Expected output',`distance=2.0, hitX=0.0
repeat=true, mean=4.0`)},
        {id: 'understand-result', title: 'Read the result', html: '<p><code>Ray</code> normalizes the supplied direction, so the vector <code>(4, 0, 0)</code> becomes <code>(1, 0, 0)</code>. The returned distance is 2 position units. Call <code>ray.at(distance)</code> only after checking for a hit: a miss returns positive infinity.</p><p>The random example creates two independent generators with the same algorithm and seed. Their first bounded draws agree; the range <code>[1, 7)</code> includes 1 through 6. <code>RunningStats</code> accumulates three measurements whose mean is 4.</p>'+note('A geometric query does not change the world','<p>The example tests the supplied box and leaves all geometry unchanged. Your plugin decides what to do with the result.</p>')},
        {id: 'next', title: 'Extend the example', html: '<p>Use <a href="#/raycasting">ray queries</a> for hit normals, sphere tests, and oriented-box intervals. Read <a href="#/random">random and sampling</a> before sharing or persisting generator state. For generated terrain, <a href="#/noise">sample noise</a> and define your height scale separately.</p>'}
      ]
    }
  );
})();
