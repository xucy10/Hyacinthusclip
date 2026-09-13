package moe.luminolmc.riceear.nms.asm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Immutable metadata snapshot of a single class, parsed via ASM without loading the class
 * into the JVM. This allows safe inspection of NMS classes across Minecraft versions even
 * when the class file is located inside a server jar that is not on the classpath.
 */
public final class ClassMetadata {

    private final String className;
    private final String superName;
    private final Set<String> interfaces;
    private final int access;
    private final List<MethodInfo> methods;
    private final List<FieldInfo> fields;

    public ClassMetadata(
            @NotNull String className,
            @Nullable String superName,
            @NotNull Set<String> interfaces,
            int access,
            @NotNull List<MethodInfo> methods,
            @NotNull List<FieldInfo> fields
    ) {
        this.className = className;
        this.superName = superName;
        this.interfaces = Set.copyOf(interfaces);
        this.access = access;
        this.methods = List.copyOf(methods);
        this.fields = List.copyOf(fields);
    }

    /** Binary class name, e.g. {@code net.minecraft.server.v1_20_R3.EntityPlayer}. */
    @NotNull
    public String getClassName() {
        return className;
    }

    /** Binary name of the superclass, or {@code null} for {@code java.lang.Object}. */
    @Nullable
    public String getSuperName() {
        return superName;
    }

    @NotNull
    public Set<String> getInterfaces() {
        return interfaces;
    }

    public int getAccess() {
        return access;
    }

    public boolean isInterface() {
        return (access & org.objectweb.asm.Opcodes.ACC_INTERFACE) != 0;
    }

    @NotNull
    public List<MethodInfo> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    @NotNull
    public List<FieldInfo> getFields() {
        return Collections.unmodifiableList(fields);
    }

    @Nullable
    public MethodInfo findMethod(@NotNull String name) {
        for (MethodInfo m : methods) {
            if (m.name().equals(name)) {
                return m;
            }
        }
        return null;
    }

    @NotNull
    public List<MethodInfo> findMethodsByReturnType(@NotNull String returnTypeDescriptor) {
        return methods.stream()
                .filter(m -> m.descriptor().endsWith(")" + returnTypeDescriptor))
                .toList();
    }

    @Nullable
    public FieldInfo findField(@NotNull String name) {
        for (FieldInfo f : fields) {
            if (f.name().equals(name)) {
                return f;
            }
        }
        return null;
    }

    @NotNull
    public List<FieldInfo> findFieldsByType(@NotNull String typeDescriptor) {
        return fields.stream()
                .filter(f -> f.descriptor().equals(typeDescriptor))
                .toList();
    }

    @Override
    public String toString() {
        return "ClassMetadata{" + className + ", methods=" + methods.size() + ", fields=" + fields.size() + "}";
    }

    /**
     * @param name       method name
     * @param descriptor ASM method descriptor, e.g. {@code (Lnet/minecraft/world/entity/Entity;)Z}
     * @param access     ASM access flags
     */
    public record MethodInfo(@NotNull String name, @NotNull String descriptor, int access) {
        public int parameterCount() {
            return org.objectweb.asm.Type.getArgumentTypes(descriptor).length;
        }

        @Nullable
        public String getReturnTypeInternalName() {
            return org.objectweb.asm.Type.getReturnType(descriptor).getInternalName();
        }
    }

    /**
     * @param name       field name
     * @param descriptor ASM field descriptor, e.g. {@code Lnet/minecraft/world/entity/Entity;}
     * @param access     ASM access flags
     */
    public record FieldInfo(@NotNull String name, @NotNull String descriptor, int access) {
        @Nullable
        public String getTypeInternalName() {
            return org.objectweb.asm.Type.getType(descriptor).getInternalName();
        }
    }
}
