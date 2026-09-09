package nsk.nu.ashcore;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.ToolProvider;
import java.io.DataInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class PackagedArtifactIT {
    private final Path build = Path.of(System.getProperty("ashcore.buildDirectory"));
    private final String finalName = System.getProperty("ashcore.finalName");

    @TempDir
    Path temp;

    @Test
    void artifacts_containJava21ClassesSourcesAndDocumentation() throws Exception {
        String entry = "nsk/nu/ashcore/api/geometry/Ray";
        try (JarFile jar = new JarFile(mainJar().toFile());
             JarFile sources = new JarFile(build.resolve(finalName + "-sources.jar").toFile());
             JarFile docs = new JarFile(build.resolve(finalName + "-javadoc.jar").toFile())) {
            try (DataInputStream data = new DataInputStream(jar.getInputStream(jar.getJarEntry(entry + ".class")))) {
                assertEquals(0xcafebabe, data.readInt());
                data.readUnsignedShort();
                assertEquals(65, data.readUnsignedShort());
            }
            assertNotNull(sources.getJarEntry(entry + ".java"));
            assertNotNull(docs.getJarEntry(entry + ".html"));
            assertNotNull(docs.getJarEntry("index.html"));
            assertFalse(jar.stream().anyMatch(e -> e.getName().contains("/testing/") || e.getName().startsWith("org/junit/")));
            // Ashcore supplies a registry, but has no built-in ServiceLoader providers.
            assertFalse(jar.stream().anyMatch(e -> e.getName().startsWith("META-INF/services/")));
            Properties coordinates = new Properties();
            try (var input = jar.getInputStream(jar.getJarEntry("META-INF/maven/dev.nasaka.blackframe/ashcore/pom.properties"))) {
                coordinates.load(input);
            }
            assertEquals(System.getProperty("ashcore.version"), coordinates.getProperty("version"));
        }
    }

    @Test
    void readmeQuickStart_compilesAndRunsAgainstMainJarOnly() throws Exception {
        String readme = Files.readString(Path.of(System.getProperty("ashcore.basedir"), "README.md"));
        var example = Pattern.compile("```java\\s*\\R(.*?)```", Pattern.DOTALL).matcher(readme);
        assertTrue(example.find());
        Path source = temp.resolve("AshcoreQuickStart.java");
        Files.writeString(source, example.group(1));
        assertNotNull(ToolProvider.getSystemJavaCompiler(), "Verification requires a JDK");
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null,
                "--release", "21", "-encoding", "UTF-8", "-classpath", mainJar().toString(), "-d", temp.toString(), source.toString()));
        try (URLClassLoader loader = new URLClassLoader(new URL[]{temp.toUri().toURL(), mainJar().toUri().toURL()}, ClassLoader.getPlatformClassLoader())) {
            loader.loadClass("AshcoreQuickStart").getMethod("main", String[].class).invoke(null, (Object) new String[0]);
        }
    }

    @Test
    void serviceRegistry_loadsPackagedProvidersWithIsolatedClassLoader() throws Exception {
        String fixture = "nsk.nu.ashcore.api.spi.testing.";
        Path providers = temp.resolve("providers.jar");
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(providers))) {
            for (String name : new String[]{"GoodService", "GoodServiceImpl", "DuplicateService", "DuplicateServiceA", "DuplicateServiceB"}) {
                String entry = fixture.replace('.', '/') + name + ".class";
                jar.putNextEntry(new JarEntry(entry));
                try (var input = getClass().getClassLoader().getResourceAsStream(entry)) {
                    assertNotNull(input);
                    input.transferTo(jar);
                }
                jar.closeEntry();
            }
            addService(jar, fixture + "GoodService", fixture + "GoodServiceImpl\n");
            addService(jar, fixture + "DuplicateService", fixture + "DuplicateServiceA\n" + fixture + "DuplicateServiceB\n");
        }
        try (URLClassLoader loader = new URLClassLoader(new URL[]{mainJar().toUri().toURL(), providers.toUri().toURL()}, ClassLoader.getPlatformClassLoader())) {
            Class<?> registry = loader.loadClass("nsk.nu.ashcore.api.spi.ServiceRegistry");
            assertEquals(mainJar().toUri().toURL(), registry.getProtectionDomain().getCodeSource().getLocation());
            var factory = registry.getMethod("of", Class.class, ClassLoader.class);
            Object loaded = factory.invoke(null, loader.loadClass(fixture + "GoodService"), loader);
            Object provider = registry.getMethod("require", String.class).invoke(loaded, "test:good");
            assertEquals(providers.toUri().toURL(), provider.getClass().getProtectionDomain().getCodeSource().getLocation());
            assertEquals(Optional.empty(), registry.getMethod("get", String.class).invoke(loaded, "test:absent"));
            assertEquals(1, ((Collection<?>) registry.getMethod("all").invoke(loaded)).size());
            InvocationTargetException missing = assertThrows(InvocationTargetException.class,
                    () -> registry.getMethod("require", String.class).invoke(loaded, "test:absent"));
            assertInstanceOf(IllegalStateException.class, missing.getCause());
            InvocationTargetException duplicate = assertThrows(InvocationTargetException.class,
                    () -> factory.invoke(null, loader.loadClass(fixture + "DuplicateService"), loader));
            assertInstanceOf(IllegalStateException.class, duplicate.getCause());
        }
    }

    private Path mainJar() { return build.resolve(finalName + ".jar"); }

    private static void addService(JarOutputStream jar, String service, String providers) throws Exception {
        jar.putNextEntry(new JarEntry("META-INF/services/" + service));
        jar.write(providers.getBytes(StandardCharsets.UTF_8));
        jar.closeEntry();
    }
}
