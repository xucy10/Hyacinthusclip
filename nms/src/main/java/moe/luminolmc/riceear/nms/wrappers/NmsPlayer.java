package moe.luminolmc.riceear.nms.wrappers;

import moe.luminolmc.riceear.nms.NmsReflection;
import moe.luminolmc.riceear.nms.NmsVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

public class NmsPlayer extends NmsEntity {

    public NmsPlayer(@NotNull Object handle, @NotNull NmsVersion version) {
        super(handle, version);
    }

    @Nullable
    public String getName() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getGameProfile");
            if (method != null) {
                Object profile = NmsReflection.invokeMethod(method, handle);
                if (profile != null) {
                    Method getNameMethod = NmsReflection.getMethodRecursive(profile.getClass(), "getName");
                    if (getNameMethod != null) {
                        return NmsReflection.invokeMethod(getNameMethod, profile);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public UUID getUniqueId() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getGameProfile");
            if (method != null) {
                Object profile = NmsReflection.invokeMethod(method, handle);
                if (profile != null) {
                    Method getUUIDMethod = NmsReflection.getMethodRecursive(profile.getClass(), "getId");
                    if (getUUIDMethod != null) {
                        return NmsReflection.invokeMethod(getUUIDMethod, profile);
                    }
                }
            }
        }
        return super.getUniqueId();
    }

    public int getPing() {
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getPing");
        if (method != null) {
            Integer result = NmsReflection.invokeMethod(method, handle);
            return result != null ? result : 0;
        }
        method = NmsReflection.getMethodRecursive(handle.getClass(), "latency");
        if (method != null) {
            Integer result = NmsReflection.invokeMethod(method, handle);
            return result != null ? result : 0;
        }
        return 0;
    }

    @Nullable
    public Object getPlayerConnection() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "connection");
            if (method != null) {
                return NmsReflection.invokeMethod(method, handle);
            }
        }
        Field field = NmsReflection.getFieldRecursive(handle.getClass(), "playerConnection");
        if (field != null) {
            return NmsReflection.getFieldValue(field, handle);
        }
        field = NmsReflection.getFieldRecursive(handle.getClass(), "b");
        if (field != null) {
            return NmsReflection.getFieldValue(field, handle);
        }
        return null;
    }

    public void sendPacket(@NotNull Object packet) {
        Object connection = getPlayerConnection();
        if (connection == null) return;

        String methodName = version.isAtLeast(NmsVersion.v1_20_R1) ? "send" : "sendPacket";
        Method method = NmsReflection.getMethodRecursive(connection.getClass(), methodName, Object.class);
        if (method == null) {
            method = NmsReflection.getMethodRecursive(connection.getClass(), methodName, getPacketClass());
        }
        if (method != null) {
            NmsReflection.invokeMethod(method, connection, packet);
        }
    }

    @Nullable
    private Class<?> getPacketClass() {
        if (version.isAtLeast(NmsVersion.v1_20_R1)) {
            return NmsReflection.getClass("net.minecraft.network.protocol.Packet");
        }
        return NmsReflection.getNmsClass(version, "Packet");
    }
}