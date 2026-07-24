package moe.luminolmc.riceear.nms.wrappers;

import moe.luminolmc.riceear.nms.NmsReflection;
import moe.luminolmc.riceear.nms.NmsVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class NmsBlock {

    private final Object handle;
    private final NmsVersion version;

    public NmsBlock(@NotNull Object handle, @NotNull NmsVersion version) {
        this.handle = handle;
        this.version = version;
    }

    @NotNull
    public Object getHandle() {
        return handle;
    }

    @Nullable
    public String getName() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getName");
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

    public float getHardness() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "defaultBlockState");
            if (method != null) {
                Object blockState = NmsReflection.invokeMethod(method, handle);
                if (blockState != null) {
                    Method getHardness = NmsReflection.getMethodRecursive(blockState.getClass(), "getDestroySpeed");
                    if (getHardness != null) {
                        try {
                            Float result = NmsReflection.invokeMethod(getHardness, blockState, null, null);
                            return result != null ? result : 0f;
                        } catch (Exception e) {
                            try {
                                Float result = NmsReflection.invokeMethod(getHardness, blockState);
                                return result != null ? result : 0f;
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            }
        }
        return 0f;
    }

    public boolean isAir() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "isAir");
            if (method != null) {
                Boolean result = NmsReflection.invokeMethod(method, handle);
                return result != null && result;
            }
        }
        return false;
    }
}