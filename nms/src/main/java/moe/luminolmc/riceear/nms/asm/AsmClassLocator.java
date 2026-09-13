package moe.luminolmc.riceear.nms.asm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ASM-based class locator. Parses {@code .class} files from jars, directories or raw bytes
 * and builds {@link ClassMetadata} snapshots without ever loading the classes into the JVM.
 *
 * <p>Typical usage inside the Mili ecosystem: index a server jar (e.g. a Mojang-mapped
 * Paper server produced by paperweight) to discover NMS fields and methods whose names
 * change between Minecraft versions.</p>
 */
public final class AsmClassLocator {

    private final Map<String, ClassMetadata> cache = new ConcurrentHashMap<>();
    private final List<Path> sources = new ArrayList<>();
    private volatile boolean indexed;

    /**
     * Registers a jar file or a class directory to be indexed.
     *
     * @return this locator, for chaining
     */
    @NotNull
    public AsmClassLocator addSource(@NotNull Path jarOrDirectory) {
        sources.add(jarOrDirectory);
        indexed = false;
        return this;
    }

    /**
     * Indexes all registered sources. Re-indexing is skipped if nothing changed.
     * The method is idempotent and thread-safe.
     */
    public synchronized void index() {
        if (indexed) {
            return;
        }
        for (Path source : sources) {
            if (!Files.exists(source)) {
                continue;
            }
            if (Files.isDirectory(source)) {
                scanDirectory(source);
            } else {
                scanJar(source);
            }
        }
        indexed = true;
    }

    private void scanJar(@NotNull Path jar) {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                    continue;
                }
                try (InputStream in = jarFile.getInputStream(entry)) {
                    ClassMetadata metadata = analyze(in);
                    if (metadata != null) {
                        cache.putIfAbsent(metadata.getClassName(), metadata);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to scan jar: " + jar, e);
        }
    }

    private void scanDirectory(@NotNull Path directory) {
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(p -> p.toString().endsWith(".class")).forEach(p -> {
                try (InputStream in = Files.newInputStream(p)) {
                    ClassMetadata metadata = analyze(in);
                    if (metadata != null) {
                        cache.putIfAbsent(metadata.getClassName(), metadata);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to scan class file: " + p, e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to walk directory: " + directory, e);
        }
    }

    /**
     * Returns the metadata for the given class if it was indexed, or {@code null}.
     *
     * @param binaryName binary class name, e.g. {@code net.minecraft.server.v1_20_R3.EntityPlayer}
     */
    @Nullable
    public ClassMetadata locate(@NotNull String binaryName) {
        index();
        return cache.get(binaryName);
    }

    /**
     * Returns all indexed classes whose binary name contains the given fragment.
     */
    @NotNull
    public List<ClassMetadata> search(@NotNull String nameFragment) {
        index();
        return cache.values().stream()
                .filter(m -> m.getClassName().contains(nameFragment))
                .collect(Collectors.toList());
    }

    @NotNull
    public Set<String> indexedClassNames() {
        index();
        return Collections.unmodifiableSet(cache.keySet());
    }

    /**
     * Builds metadata from an already-opened input stream (does not close it).
     */
    @Nullable
    public static ClassMetadata analyze(@NotNull InputStream in) throws IOException {
        byte[] bytes = in.readAllBytes();
        return analyze(bytes);
    }

    /**
     * Builds metadata from raw class file bytes.
     */
    @Nullable
    public static ClassMetadata analyze(@NotNull byte[] classBytes) {
        try {
            ClassReader reader = new ClassReader(classBytes);
            Collector collector = new Collector();
            reader.accept(collector, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
            return collector.toMetadata();
        } catch (Exception e) {
            // Not a valid class file (e.g. module-info in a jar) — skip silently.
            return null;
        }
    }

    /**
     * Builds metadata directly from a loaded class using its defining classloader.
     * The class is <b>not</b> initialized or resolved by this call beyond locating its bytes.
     */
    @Nullable
    public static ClassMetadata fromClass(@NotNull Class<?> clazz) {
        String resource = clazz.getName().replace('.', '/') + ".class";
        try (InputStream in = clazz.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return null;
            }
            return analyze(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read class bytes for " + clazz.getName(), e);
        }
    }

    private static final class Collector extends ClassVisitor {
        private String name;
        private String superName;
        private final Set<String> interfaces = new HashSet<>();
        private final List<ClassMetadata.MethodInfo> methods = new ArrayList<>();
        private final List<ClassMetadata.FieldInfo> fields = new ArrayList<>();

        Collector() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.name = name.replace('/', '.');
            this.superName = superName == null ? null : superName.replace('/', '.');
            if (interfaces != null) {
                for (String i : interfaces) {
                    this.interfaces.add(i.replace('/', '.'));
                }
            }
        }

        @Override
        public org.objectweb.asm.FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
            fields.add(new ClassMetadata.FieldInfo(name, descriptor, access));
            return null;
        }

        @Override
        public org.objectweb.asm.MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            methods.add(new ClassMetadata.MethodInfo(name, descriptor, access));
            return null;
        }

        ClassMetadata toMetadata() {
            if (name == null) {
                return null;
            }
            return new ClassMetadata(name, superName, interfaces, 0, methods, fields);
        }
    }
}
