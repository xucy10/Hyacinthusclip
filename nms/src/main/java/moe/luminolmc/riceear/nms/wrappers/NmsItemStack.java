package moe.luminolmc.riceear.nms.wrappers;

import moe.luminolmc.riceear.nms.NmsReflection;
import moe.luminolmc.riceear.nms.NmsVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class NmsItemStack {

    private final Object handle;
    private final NmsVersion version;

    public NmsItemStack(@NotNull Object handle, @NotNull NmsVersion version) {
        this.handle = handle;
        this.version = version;
    }

    @NotNull
    public Object getHandle() {
        return handle;
    }

    public int getAmount() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getCount");
        if (method != null) {
            Integer result = NmsReflection.invokeMethod(method, handle);
            return result != null ? result : 0;
        }
        Field field = NmsReflection.getFieldRecursive(handle.getClass(), "count");
        if (field != null) {
            Integer result = NmsReflection.getFieldValue(field, handle);
            return result != null ? result : 0;
        }
        return 0;
    }

    public void setAmount(int amount) {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "setCount", int.class);
        if (method != null) {
            NmsReflection.invokeMethod(method, handle, amount);
            return;
        }
        Field field = NmsReflection.getFieldRecursive(handle.getClass(), "count");
        if (field != null) {
            NmsReflection.setFieldValue(field, handle, amount);
        }
    }

    public int getDurability() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getDamageValue");
            if (method != null) {
                Integer result = NmsReflection.invokeMethod(method, handle);
                return result != null ? result : 0;
            }
        }
        return 0;
    }

    public void setDurability(int durability) {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "setDamageValue", int.class);
            if (method != null) {
                NmsReflection.invokeMethod(method, handle, durability);
            }
        }
    }

    @Nullable
    public String getDisplayName() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getDisplayName");
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

    @Nullable
    public Object getTag() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getTag");
            if (method != null) {
                return NmsReflection.invokeMethod(method, handle);
            }
        }
        return null;
    }

    public boolean hasTag() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "hasTag");
            if (method != null) {
                Boolean result = NmsReflection.invokeMethod(method, handle);
                return result != null && result;
            }
        }
        return false;
    }

    @Nullable
    public Object getItem() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getItem");
            if (method != null) {
                return NmsReflection.invokeMethod(method, handle);
            }
        }
        return null;
    }
}