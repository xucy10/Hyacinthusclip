package moe.luminolmc.riceear.nms.wrappers;

import moe.luminolmc.riceear.nms.NmsReflection;
import moe.luminolmc.riceear.nms.NmsVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class NmsEntity {

    protected final Object handle;
    protected final NmsVersion version;

    public NmsEntity(@NotNull Object handle, @NotNull NmsVersion version) {
        this.handle = handle;
        this.version = version;
    }

    @NotNull
    public Object getHandle() {
        return handle;
    }

    public int getId() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getId");
        if (method != null) {
            return NmsReflection.invokeMethod(method, handle);
        }
        method = NmsReflection.getMethodRecursive(handle.getClass(), "ae");
        if (method != null) {
            return NmsReflection.invokeMethod(method, handle);
        }
        return -1;
    }

    @Nullable
    public UUID getUniqueId() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getUUID");
            if (method != null) {
                return NmsReflection.invokeMethod(method, handle);
            }
        }
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getUniqueID");
        if (method != null) {
            return NmsReflection.invokeMethod(method, handle);
        }
        return null;
    }

    @Nullable
    public String getCustomName() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getCustomName");
            if (method != null) {
                Object component = NmsReflection.invokeMethod(method, handle);
                if (component != null) {
                    Method getString = NmsReflection.getMethodRecursive(component.getClass(), "getString");
                    if (getString != null) {
                        return NmsReflection.invokeMethod(getString, component);
                    }
                }
            }
        }
        return null;
    }

    public void setCustomName(@Nullable String name) {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "setCustomName");
            if (method != null) {
                if (name == null) {
                    NmsReflection.invokeMethod(method, handle, (Object) null);
                } else {
                    Class<?> componentClass = NmsReflection.getClass("net.minecraft.network.chat.Component");
                    if (componentClass != null) {
                        Method literalMethod = NmsReflection.getMethod(componentClass, "literal", String.class);
                        if (literalMethod != null) {
                            Object component = NmsReflection.invokeMethod(literalMethod, null, name);
                            if (component != null) {
                                NmsReflection.invokeMethod(method, handle, component);
                            }
                        }
                    }
                }
                return;
            }
        }
        Field field = NmsReflection.getFieldRecursive(handle.getClass(), "customName");
        if (field != null) {
            NmsReflection.setFieldValue(field, handle, name);
        }
    }

    public boolean isAlive() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "isAlive");
        if (method != null) {
            Boolean result = NmsReflection.invokeMethod(method, handle);
            return result != null && result;
        }
        return false;
    }

    public double getX() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getX");
        if (method != null) {
            Double result = NmsReflection.invokeMethod(method, handle);
            return result != null ? result : 0.0;
        }
        return 0.0;
    }

    public double getY() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getY");
        if (method != null) {
            Double result = NmsReflection.invokeMethod(method, handle);
            return result != null ? result : 0.0;
        }
        return 0.0;
    }

    public double getZ() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getZ");
        if (method != null) {
            Double result = NmsReflection.invokeMethod(method, handle);
            return result != null ? result : 0.0;
        }
        return 0.0;
    }

    @Nullable
    public Object getWorld() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getCommandSenderWorld");
            if (method != null) {
                return NmsReflection.invokeMethod(method, handle);
            }
            method = NmsReflection.getMethodRecursive(handle.getClass(), "level");
            if (method != null) {
                return NmsReflection.invokeMethod(method, handle);
            }
            method = NmsReflection.getMethodRecursive(handle.getClass(), "getLevel");
            if (method != null) {
                return NmsReflection.invokeMethod(method, handle);
            }
        }
        Field field = NmsReflection.getFieldRecursive(handle.getClass(), "world");
        if (field != null) {
            return NmsReflection.getFieldValue(field, handle);
        }
        field = NmsReflection.getFieldRecursive(handle.getClass(), "level");
        if (field != null) {
            return NmsReflection.getFieldValue(field, handle);
        }
        return null;
    }

    public void teleport(double x, double y, double z) {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "setPos", double.class, double.class, double.class);
        if (method != null) {
            NmsReflection.invokeMethod(method, handle, x, y, z);
        }
    }
}