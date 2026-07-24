package moe.luminolmc.riceear.nms.wrappers;

import moe.luminolmc.riceear.nms.NmsReflection;
import moe.luminolmc.riceear.nms.NmsVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

public class NmsWorld {

    private final Object handle;
    private final NmsVersion version;

    public NmsWorld(@NotNull Object handle, @NotNull NmsVersion version) {
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
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getWorldData");
            if (method != null) {
                Object worldData = NmsReflection.invokeMethod(method, handle);
                if (worldData != null) {
                    Method getNameMethod = NmsReflection.getMethodRecursive(worldData.getClass(), "getLevelName");
                    if (getNameMethod != null) {
                        return NmsReflection.invokeMethod(getNameMethod, worldData);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public List<?> getPlayers() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getPlayers");
            if (method != null) {
                return NmsReflection.invokeMethod(method, handle);
            }
        }
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "players");
        if (method != null) {
            return NmsReflection.invokeMethod(method, handle);
        }
        return null;
    }

    @Nullable
    public Object getEntityById(int id) {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getEntity", int.class);
        if (method != null) {
            return NmsReflection.invokeMethod(method, handle, id);
        }
        return null;
    }

    @Nullable
    public Object getEntityByUUID(@NotNull UUID uuid) {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getEntity", UUID.class);
        if (method != null) {
            return NmsReflection.invokeMethod(method, handle, uuid);
        }
        return null;
    }

    public long getTime() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getDayTime");
        if (method != null) {
            Long result = NmsReflection.invokeMethod(method, handle);
            return result != null ? result : 0L;
        }
        return 0L;
    }

    public void setTime(long time) {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "setDayTime", long.class);
        if (method != null) {
            NmsReflection.invokeMethod(method, handle, time);
        }
    }
}