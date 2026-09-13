package moe.luminolmc.riceear.nms.wrappers;

import moe.luminolmc.riceear.nms.NmsReflection;
import moe.luminolmc.riceear.nms.NmsVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Reflection wrapper for the NMS boss bar implementations ({@code BossBattleServer} and its
 * pre-1.9 equivalents). Supports 1.8.8 through latest versions, mirroring the naming changes
 * between Mojang-mapped and obfuscated runtimes.
 */
public class NmsBossBar {

    protected final Object handle;
    protected final NmsVersion version;

    public NmsBossBar(@NotNull Object handle, @NotNull NmsVersion version) {
        this.handle = handle;
        this.version = version;
    }

    /**
     * Creates a new server-side boss battle instance.
     *
     * @return the NMS handle, or {@code null} if construction failed
     */
    @Nullable
    public static Object create(@NotNull NmsVersion version, @NotNull String title, float progress) {
        if (version.isAtLeast(NmsVersion.v1_9_R1)) {
            Class<?> componentClass = NmsReflection.getClass("net.minecraft.network.chat.Component");
            Object component = toComponent(componentClass, title);
            if (component != null) {
                // BossBattleServer moved between packages across versions — try all known locations.
                Class<?> bossBattleServer = null;
                for (String name : new String[]{
                        "net.minecraft.network.chat.BossBattleServer",
                        "net.minecraft.server.BossBattleServer"}) {
                    bossBattleServer = NmsReflection.getClass(name);
                    if (bossBattleServer != null) {
                        break;
                    }
                }
                if (bossBattleServer != null) {
                    for (var constructor : bossBattleServer.getDeclaredConstructors()) {
                        if (constructor.getParameterCount() == 1) {
                            try {
                                constructor.setAccessible(true);
                                return constructor.newInstance(component);
                            } catch (Exception ignored) {
                                // try next constructor
                            }
                        }
                    }
                }
            }
        }
        // Legacy (1.8.8): BossBattle lives in net.minecraft.server with a String constructor.
        Class<?> legacy = NmsReflection.getNmsClass(version, "BossBattle");
        if (legacy == null) {
            legacy = NmsReflection.getClass("net.minecraft.server.BossBattle");
        }
        if (legacy != null) {
            try {
                var constructor = legacy.getDeclaredConstructor(String.class);
                constructor.setAccessible(true);
                return constructor.newInstance(title);
            } catch (Exception ignored) {
                // fall through
            }
        }
        return null;
    }

    public void setProgress(float progress) {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "setProgress");
        if (method == null) {
            method = NmsReflection.getMethodRecursive(handle.getClass(), "a");
            if (method != null && method.getParameterCount() == 1 && method.getParameterTypes()[0] == float.class) {
                // candidate found below
            } else {
                method = null;
            }
        }
        if (method != null) {
            NmsReflection.invokeMethod(method, handle, progress);
        }
    }

    public float getProgress() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getProgress");
        if (method != null) {
            Object value = NmsReflection.invokeMethod(method, handle);
            if (value instanceof Float f) {
                return f;
            }
        }
        FieldAccessor accessor = fieldAccessor("a", float.class);
        if (accessor != null) {
            Object value = accessor.get(handle);
            if (value instanceof Float f) {
                return f;
            }
        }
        return 0.0f;
    }

    public void setTitle(@NotNull String title) {
        Object component = toComponent(NmsReflection.getClass("net.minecraft.network.chat.Component"), title);
        if (component == null) {
            return;
        }
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "setTitle");
        if (method == null) {
            method = NmsReflection.getMethodRecursive(handle.getClass(), "a");
        }
        if (method != null && method.getParameterCount() == 1) {
            NmsReflection.invokeMethod(method, handle, component);
        }
    }

    public void setVisible(boolean visible) {
        // 1.9+: darkness/visibility handled by removing/adding the bar on the client;
        // the server object exposes setDarkenSky-style flags, so we simply try the common names.
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "setVisible");
        if (method == null) {
            method = NmsReflection.getMethodRecursive(handle.getClass(), "b");
        }
        if (method != null && method.getParameterCount() == 1 && method.getParameterTypes()[0] == boolean.class) {
            NmsReflection.invokeMethod(method, handle, visible);
        }
    }

    @Nullable
    public Object getHandle() {
        return handle;
    }

    @Nullable
    private FieldAccessor fieldAccessor(@NotNull String mojangName, @NotNull Class<?> type) {
        try {
            var field = handle.getClass().getDeclaredField(mojangName);
            field.setAccessible(true);
            if (field.getType() != type) {
                return null;
            }
            return new FieldAccessor(field);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    @Nullable
    private static Object toComponent(@Nullable Class<?> componentClass, @NotNull String text) {
        if (componentClass == null) {
            return null;
        }
        Method literal = NmsReflection.getMethod(componentClass, "literal", String.class);
        if (literal != null) {
            return NmsReflection.invokeMethod(literal, null, text);
        }
        return null;
    }

    private record FieldAccessor(java.lang.reflect.Field field) {
        Object get(Object target) {
            try {
                return field.get(target);
            } catch (IllegalAccessException e) {
                return null;
            }
        }
    }
}
