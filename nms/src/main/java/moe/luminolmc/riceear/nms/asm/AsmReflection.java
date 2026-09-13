package moe.luminolmc.riceear.nms.asm;

import moe.luminolmc.riceear.nms.NmsReflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ASM-powered reflection bridge. When name-based lookups via {@link NmsReflection} fail
 * (field and method names are not stable across Minecraft versions), this class falls back
 * to matching members <b>by type</b> using ASM-parsed metadata instead of guessing.
 *
 * <p>All lookups are cached and thread-safe.</p>
 */
public final class AsmReflection {

    private static final Map<String, Field> FIELD_BY_TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, List<Field>> FIELDS_BY_TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Method> METHOD_BY_TYPE_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Field> FIELD_BY_NAME_CACHE = new ConcurrentHashMap<>();

    private AsmReflection() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Finds the (unique) instance or static field of the given type on the holder class.
     * Useful for NMS versions where a field was renamed but its type did not change.
     *
     * @param holder    the class declaring the field (superclasses are searched)
     * @param fieldType expected field type
     * @return the resolved field, or {@code null} if zero or multiple candidates were found
     */
    @Nullable
    public static Field findFieldByType(@NotNull Class<?> holder, @NotNull Class<?> fieldType) {
        String key = holder.getName() + "#" + fieldType.getName();
        return FIELD_BY_TYPE_CACHE.computeIfAbsent(key, k -> resolveFieldByType(holder, fieldType));
    }

    /**
     * Finds all fields of the given type on the holder class (including inherited ones),
     * ordered from the most specific class upwards.
     */
    @NotNull
    public static List<Field> findFieldsByType(@NotNull Class<?> holder, @NotNull Class<?> fieldType) {
        String key = holder.getName() + "#" + fieldType.getName();
        List<Field> cached = FIELDS_BY_TYPE_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        List<Field> result = new ArrayList<>();
        String descriptor = descriptorOf(fieldType);
        Class<?> current = holder;
        while (current != null && current != Object.class) {
            ClassMetadata metadata = AsmClassLocator.fromClass(current);
            if (metadata != null) {
                for (ClassMetadata.FieldInfo info : metadata.findFieldsByType(descriptor)) {
                    try {
                        Field field = current.getDeclaredField(info.name());
                        field.setAccessible(true);
                        result.add(field);
                    } catch (NoSuchFieldException ignored) {
                        // Class file out of sync with runtime class — skip.
                    }
                }
            }
            current = current.getSuperclass();
        }
        result = List.copyOf(result);
        FIELDS_BY_TYPE_CACHE.put(key, result);
        return result;
    }

    /**
     * Finds a method by its return type and exact parameter types, ignoring its name.
     * Useful when NMS method names are obfuscated or version-dependent.
     */
    @Nullable
    public static Method findMethodByType(@NotNull Class<?> holder, @Nullable Class<?> returnType, @NotNull Class<?>... parameterTypes) {
        String key = holder.getName() + "#" + (returnType == null ? "any" : returnType.getName())
                + "(" + joinTypes(parameterTypes) + ")";
        return METHOD_BY_TYPE_CACHE.computeIfAbsent(key, k -> resolveMethodByType(holder, returnType, parameterTypes));
    }

    /**
     * Resolves a declared field by name using ASM metadata first and {@link Class} reflection
     * second; unlike plain reflection it also works when only the class file is available
     * (metadata is returned without resolving the field object — see {@link #getFieldInfo}).
     */
    @Nullable
    public static Field findField(@NotNull Class<?> holder, @NotNull String fieldName) {
        String key = holder.getName() + "#" + fieldName;
        return FIELD_BY_NAME_CACHE.computeIfAbsent(key, k -> {
            Field field = NmsReflection.getField(holder, fieldName);
            if (field != null) {
                return field;
            }
            // Fall back to a type-based match against ASM metadata with the same declared name.
            ClassMetadata metadata = AsmClassLocator.fromClass(holder);
            if (metadata == null) {
                return null;
            }
            ClassMetadata.FieldInfo info = metadata.findField(fieldName);
            if (info == null) {
                return null;
            }
            try {
                Field f = holder.getDeclaredField(info.name());
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                return null;
            }
        });
    }

    /**
     * Returns raw field metadata for the holder class without resolving a {@link Field}
     * object — usable even for classes not present on the classpath.
     */
    @Nullable
    public static ClassMetadata.FieldInfo getFieldInfo(@NotNull Class<?> holder, @NotNull String fieldName) {
        ClassMetadata metadata = AsmClassLocator.fromClass(holder);
        return metadata == null ? null : metadata.findField(fieldName);
    }

    @Nullable
    private static Field resolveFieldByType(@NotNull Class<?> holder, @NotNull Class<?> fieldType) {
        List<Field> candidates = findFieldsByType(holder, fieldType);
        return candidates.size() == 1 ? candidates.get(0) : null;
    }

    @Nullable
    private static Method resolveMethodByType(@NotNull Class<?> holder, @Nullable Class<?> returnType, @NotNull Class<?>... parameterTypes) {
        Class<?> current = holder;
        String returnDescriptor = returnType == null ? null : descriptorOf(returnType);
        while (current != null && current != Object.class) {
            ClassMetadata metadata = AsmClassLocator.fromClass(current);
            if (metadata != null) {
                List<ClassMetadata.MethodInfo> candidates = new ArrayList<>();
                if (returnDescriptor == null) {
                    candidates.addAll(metadata.getMethods());
                } else {
                    candidates.addAll(metadata.findMethodsByReturnType(returnDescriptor));
                }
                for (ClassMetadata.MethodInfo info : candidates) {
                    if (matchesParameters(info, parameterTypes)) {
                        try {
                            Method method = current.getDeclaredMethod(info.name(), parameterTypes);
                            method.setAccessible(true);
                            return method;
                        } catch (NoSuchMethodException ignored) {
                            // Metadata/runtime mismatch — continue searching.
                        }
                    }
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private static boolean matchesParameters(@NotNull ClassMetadata.MethodInfo info, @NotNull Class<?>... parameterTypes) {
        if (info.parameterCount() != parameterTypes.length) {
            return false;
        }
        var types = org.objectweb.asm.Type.getArgumentTypes(info.descriptor());
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!types[i].getClassName().equals(parameterTypes[i].getName())) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    private static String descriptorOf(@NotNull Class<?> type) {
        return org.objectweb.asm.Type.getDescriptor(type);
    }

    @NotNull
    private static String joinTypes(@NotNull Class<?>... types) {
        StringBuilder sb = new StringBuilder();
        for (Class<?> type : types) {
            if (sb.length() > 0) {
                sb.append(',');
            }
            sb.append(type.getName());
        }
        return sb.toString();
    }
}
