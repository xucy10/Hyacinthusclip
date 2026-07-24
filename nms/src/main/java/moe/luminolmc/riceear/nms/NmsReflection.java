package moe.luminolmc.riceear.nms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NmsReflection {

    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Constructor<?>> CONSTRUCTOR_CACHE = new ConcurrentHashMap<>();

    private NmsReflection() {
        throw new UnsupportedOperationException("Utility class");
    }

    @Nullable
    public static Class<?> getClass(@NotNull String className) {
        return CLASS_CACHE.computeIfAbsent(className, name -> {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        });
    }

    @NotNull
    public static Class<?> getClassOrThrow(@NotNull String className) {
        Class<?> clazz = getClass(className);
        if (clazz == null) {
            throw new RuntimeException("Class not found: " + className);
        }
        return clazz;
    }

    @Nullable
    public static Class<?> getNmsClass(@NotNull NmsVersion version, @NotNull String simpleName) {
        return getClass(version.getNmsPackage() + "." + simpleName);
    }

    @NotNull
    public static Class<?> getNmsClassOrThrow(@NotNull NmsVersion version, @NotNull String simpleName) {
        return getClassOrThrow(version.getNmsPackage() + "." + simpleName);
    }

    @Nullable
    public static Class<?> getCraftBukkitClass(@NotNull NmsVersion version, @NotNull String simpleName) {
        return getClass(version.getCraftBukkitPackage() + "." + simpleName);
    }

    @NotNull
    public static Class<?> getCraftBukkitClassOrThrow(@NotNull NmsVersion version, @NotNull String simpleName) {
        return getClassOrThrow(version.getCraftBukkitPackage() + "." + simpleName);
    }

    @Nullable
    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... parameterTypes) {
        String key = clazz.getName() + "#" + methodName + Arrays.toString(parameterTypes);
        return METHOD_CACHE.computeIfAbsent(key, k -> {
            try {
                Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                try {
                    Method method = clazz.getMethod(methodName, parameterTypes);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ex) {
                    return null;
                }
            }
        });
    }

    @Nullable
    public static Method getMethodRecursive(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... parameterTypes) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Method method = getMethod(current, methodName, parameterTypes);
            if (method != null) return method;
            current = current.getSuperclass();
        }
        return null;
    }

    @Nullable
    public static Field getField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        String key = clazz.getName() + "#" + fieldName;
        return FIELD_CACHE.computeIfAbsent(key, k -> {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                try {
                    Field field = clazz.getField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ex) {
                    return null;
                }
            }
        });
    }

    @Nullable
    public static Field getFieldRecursive(@NotNull Class<?> clazz, @NotNull String fieldName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            Field field = getField(current, fieldName);
            if (field != null) return field;
            current = current.getSuperclass();
        }
        return null;
    }

    @Nullable
    public static Constructor<?> getConstructor(@NotNull Class<?> clazz, Class<?>... parameterTypes) {
        String key = clazz.getName() + "#init" + Arrays.toString(parameterTypes);
        return CONSTRUCTOR_CACHE.computeIfAbsent(key, k -> {
            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
                constructor.setAccessible(true);
                return constructor;
            } catch (NoSuchMethodException e) {
                return null;
            }
        });
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(@NotNull Field field, @Nullable Object instance) {
        try {
            return (T) field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot access field: " + field.getName(), e);
        }
    }

    public static void setFieldValue(@NotNull Field field, @Nullable Object instance, @Nullable Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot set field: " + field.getName(), e);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(@NotNull Method method, @Nullable Object instance, Object... args) {
        try {
            return (T) method.invoke(instance, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Cannot invoke method: " + method.getName(), cause);
        } catch (Exception e) {
            throw new RuntimeException("Cannot invoke method: " + method.getName(), e);
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(@NotNull Constructor<?> constructor, Object... args) {
        try {
            return (T) constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            throw new RuntimeException("Cannot create instance", cause);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create instance", e);
        }
    }

    public static void clearCaches() {
        CLASS_CACHE.clear();
        METHOD_CACHE.clear();
        FIELD_CACHE.clear();
        CONSTRUCTOR_CACHE.clear();
    }
}