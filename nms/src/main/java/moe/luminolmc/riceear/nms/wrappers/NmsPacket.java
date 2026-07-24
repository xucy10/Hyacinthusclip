package moe.luminolmc.riceear.nms.wrappers;

import moe.luminolmc.riceear.nms.NmsReflection;
import moe.luminolmc.riceear.nms.NmsVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NmsPacket {

    private final Object handle;
    private final NmsVersion version;

    public NmsPacket(@NotNull Object handle, @NotNull NmsVersion version) {
        this.handle = handle;
        this.version = version;
    }

    @NotNull
    public Object getHandle() {
        return handle;
    }

    @Nullable
    public String getPacketName() {
        return handle.getClass().getSimpleName();
    }

    @Nullable
    public Object getFieldValue(@NotNull String fieldName) {
        Field field = NmsReflection.getFieldRecursive(handle.getClass(), fieldName);
        if (field != null) {
            return NmsReflection.getFieldValue(field, handle);
        }
        return null;
    }

    public void setFieldValue(@NotNull String fieldName, @Nullable Object value) {
        Field field = NmsReflection.getFieldRecursive(handle.getClass(), fieldName);
        if (field != null) {
            NmsReflection.setFieldValue(field, handle, value);
        }
    }

    @Nullable
    public Object getComponent(@NotNull String fieldName) {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Field field = NmsReflection.getFieldRecursive(handle.getClass(), fieldName);
            if (field != null) {
                Object component = NmsReflection.getFieldValue(field, handle);
                if (component != null) {
                    return component;
                }
            }
        }
        return null;
    }

    @Nullable
    public Object invokeGetter(@NotNull String methodName) {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), methodName);
        if (method != null) {
            return NmsReflection.invokeMethod(method, handle);
        }
        return null;
    }
}