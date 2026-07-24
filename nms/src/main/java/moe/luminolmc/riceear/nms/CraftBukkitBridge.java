package moe.luminolmc.riceear.nms;

import moe.luminolmc.riceear.nms.wrappers.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class CraftBukkitBridge {

    private final NmsVersion version;

    public CraftBukkitBridge(@NotNull NmsVersion version) {
        this.version = version;
    }

    @NotNull
    public static CraftBukkitBridge get() {
        return new CraftBukkitBridge(NmsManager.getInstance().getVersion());
    }

    @Nullable
    public Object getHandle(@NotNull Object bukkitObject) {
        try {
            Method getHandleMethod = NmsReflection.getMethodRecursive(bukkitObject.getClass(), "getHandle");
            if (getHandleMethod != null) {
                return NmsReflection.invokeMethod(getHandleMethod, bukkitObject);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    public NmsEntity toNmsEntity(@NotNull Object bukkitEntity) {
        Object handle = getHandle(bukkitEntity);
        if (handle != null) {
            return new NmsEntity(handle, version);
        }
        return null;
    }

    @Nullable
    public NmsPlayer toNmsPlayer(@NotNull Object bukkitPlayer) {
        Object handle = getHandle(bukkitPlayer);
        if (handle != null) {
            return new NmsPlayer(handle, version);
        }
        return null;
    }

    @Nullable
    public NmsWorld toNmsWorld(@NotNull Object bukkitWorld) {
        Object handle = getHandle(bukkitWorld);
        if (handle != null) {
            return new NmsWorld(handle, version);
        }
        return null;
    }

    @Nullable
    public NmsItemStack toNmsItemStack(@NotNull Object bukkitItemStack) {
        Class<?> craftItemStackClass = NmsClassRegistry.getCraftBukkitClass("CraftItemStack", version);
        if (craftItemStackClass != null) {
            Method asNMSCopyMethod = NmsReflection.getMethod(craftItemStackClass, "asNMSCopy", bukkitItemStack.getClass());
            if (asNMSCopyMethod != null) {
                Object nmsStack = NmsReflection.invokeMethod(asNMSCopyMethod, null, bukkitItemStack);
                if (nmsStack != null) {
                    return new NmsItemStack(nmsStack, version);
                }
            }
        }
        Object handle = getHandle(bukkitItemStack);
        if (handle != null) {
            return new NmsItemStack(handle, version);
        }
        return null;
    }

    @Nullable
    public Object toBukkitEntity(@NotNull Object nmsEntity) {
        try {
            Class<?> craftEntityClass = NmsClassRegistry.getCraftBukkitClass("CraftEntity", version);
            if (craftEntityClass != null) {
                Method getEntityMethod = NmsReflection.getMethod(craftEntityClass, "getEntity",
                        NmsClassRegistry.getNmsClass("Entity", version));
                if (getEntityMethod != null) {
                    return NmsReflection.invokeMethod(getEntityMethod, null, nmsEntity);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    public Object toBukkitItemStack(@NotNull Object nmsItemStack) {
        try {
            Class<?> craftItemStackClass = NmsClassRegistry.getCraftBukkitClass("CraftItemStack", version);
            if (craftItemStackClass != null) {
                Method asBukkitCopyMethod = NmsReflection.getMethod(craftItemStackClass, "asBukkitCopy",
                        NmsClassRegistry.getNmsClass("ItemStack", version));
                if (asBukkitCopyMethod != null) {
                    return NmsReflection.invokeMethod(asBukkitCopyMethod, null, nmsItemStack);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    public Object toBukkitWorld(@NotNull Object nmsWorld) {
        try {
            Class<?> craftWorldClass = NmsClassRegistry.getCraftBukkitClass("CraftWorld", version);
            if (craftWorldClass != null) {
                Method getWorldMethod = NmsReflection.getMethod(craftWorldClass, "getWorld",
                        NmsClassRegistry.getNmsClass("WorldServer", version));
                if (getWorldMethod != null) {
                    return NmsReflection.invokeMethod(getWorldMethod, null, nmsWorld);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    public Object getMinecraftServer() {
        try {
            Class<?> craftServerClass = NmsClassRegistry.getCraftBukkitClass("CraftServer", version);
            if (craftServerClass != null) {
                Method getServerMethod = NmsReflection.getMethod(craftServerClass, "getServer");
                if (getServerMethod != null) {
                    return NmsReflection.invokeMethod(getServerMethod, null);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    @Nullable
    public NmsServer getNmsServer() {
        Object server = getMinecraftServer();
        if (server != null) {
            return new NmsServer(server, version);
        }
        return null;
    }
}