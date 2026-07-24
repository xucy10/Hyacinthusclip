package moe.luminolmc.riceear.nms.wrappers;

import moe.luminolmc.riceear.nms.NmsReflection;
import moe.luminolmc.riceear.nms.NmsVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class NmsServer {

    private final Object handle;
    private final NmsVersion version;

    public NmsServer(@NotNull Object handle, @NotNull NmsVersion version) {
        this.handle = handle;
        this.version = version;
    }

    @NotNull
    public Object getHandle() {
        return handle;
    }

    @Nullable
    public String getVersion() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getServerVersion");
        if (method != null) {
            return NmsReflection.invokeMethod(method, handle);
        }
        method = NmsReflection.getMethodRecursive(handle.getClass(), "getVersion");
        if (method != null) {
            return NmsReflection.invokeMethod(method, handle);
        }
        return null;
    }

    public int getMaxPlayers() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getMaxPlayers");
        if (method != null) {
            Integer result = NmsReflection.invokeMethod(method, handle);
            return result != null ? result : 0;
        }
        return 0;
    }

    public int getOnlinePlayers() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getPlayerCount");
        if (method != null) {
            Integer result = NmsReflection.invokeMethod(method, handle);
            return result != null ? result : 0;
        }
        return 0;
    }

    @Nullable
    public Object getPlayerList() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getPlayerList");
            if (method != null) {
                return NmsReflection.invokeMethod(method, handle);
            }
        }
        return null;
    }

    public void shutdown() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "safeShutdown");
        if (method != null) {
            NmsReflection.invokeMethod(method, handle);
            return;
        }
        method = NmsReflection.getMethodRecursive(handle.getClass(), "safeShutdown", boolean.class);
        if (method != null) {
            NmsReflection.invokeMethod(method, handle, false);
        }
    }
}