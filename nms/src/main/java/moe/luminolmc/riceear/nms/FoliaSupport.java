package moe.luminolmc.riceear.nms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class FoliaSupport {

    private static volatile Boolean isFolia;
    private static volatile Boolean isFoliaClassPresent;

    private FoliaSupport() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static boolean isFolia() {
        if (isFolia == null) {
            synchronized (FoliaSupport.class) {
                if (isFolia == null) {
                    isFolia = detectFolia();
                }
            }
        }
        return isFolia;
    }

    public static boolean isFoliaClassPresent() {
        if (isFoliaClassPresent == null) {
            synchronized (FoliaSupport.class) {
                if (isFoliaClassPresent == null) {
                    try {
                        Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                        isFoliaClassPresent = true;
                    } catch (ClassNotFoundException e) {
                        isFoliaClassPresent = false;
                    }
                }
            }
        }
        return isFoliaClassPresent;
    }

    private static boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Method isGlobalTickThread = Class.forName("org.bukkit.Bukkit")
                    .getMethod("isGlobalTickThread");
            return (boolean) isGlobalTickThread.invoke(null);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isGlobalTickThread() {
        try {
            Method method = Class.forName("org.bukkit.Bukkit")
                    .getMethod("isGlobalTickThread");
            return (boolean) method.invoke(null);
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isTickThread() {
        try {
            Method method = Class.forName("org.bukkit.Bukkit")
                    .getMethod("isTickThread");
            return (boolean) method.invoke(null);
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isOwnedByCurrentRegion(@NotNull Object world) {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method method = bukkitClass.getMethod("isOwnedByCurrentRegion",
                    Class.forName("org.bukkit.World"));
            return (boolean) method.invoke(null, world);
        } catch (Exception e) {
            return true;
        }
    }

    @Nullable
    public static Object getGlobalRegionScheduler() {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method method = bukkitClass.getMethod("getGlobalRegionScheduler");
            return method.invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Object getAsyncScheduler() {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method method = bukkitClass.getMethod("getAsyncScheduler");
            return method.invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Object getRegionScheduler() {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Method method = bukkitClass.getMethod("getRegionScheduler");
            return method.invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Object getEntityScheduler(@NotNull Object entity) {
        try {
            Method method = entity.getClass().getMethod("getScheduler");
            return method.invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }

    public static void executeOnRegion(@NotNull Object world, int chunkX, int chunkZ,
                                        @NotNull Runnable task) {
        try {
            Object regionScheduler = getRegionScheduler();
            if (regionScheduler != null) {
                Method method = regionScheduler.getClass().getMethod("execute",
                        Class.forName("org.bukkit.plugin.Plugin"),
                        Class.forName("org.bukkit.World"),
                        int.class, int.class,
                        Runnable.class);
                Object plugin = getCallingPlugin();
                method.invoke(regionScheduler, plugin, world, chunkX, chunkZ, task);
                return;
            }
        } catch (Exception ignored) {
        }
        task.run();
    }

    public static void executeOnRegion(@NotNull Object world, int chunkX, int chunkZ,
                                        @NotNull Object plugin, @NotNull Runnable task) {
        try {
            Object regionScheduler = getRegionScheduler();
            if (regionScheduler != null) {
                Method method = regionScheduler.getClass().getMethod("execute",
                        Class.forName("org.bukkit.plugin.Plugin"),
                        Class.forName("org.bukkit.World"),
                        int.class, int.class,
                        Runnable.class);
                method.invoke(regionScheduler, plugin, world, chunkX, chunkZ, task);
                return;
            }
        } catch (Exception ignored) {
        }
        task.run();
    }

    public static void executeOnEntity(@NotNull Object entity, @NotNull Runnable task,
                                        @Nullable Runnable retired) {
        try {
            Object entityScheduler = getEntityScheduler(entity);
            if (entityScheduler != null) {
                Method method = entityScheduler.getClass().getMethod("execute",
                        Class.forName("org.bukkit.plugin.Plugin"),
                        Runnable.class, Runnable.class, long.class);
                Object plugin = getCallingPlugin();
                method.invoke(entityScheduler, plugin, task, retired, 1L);
                return;
            }
        } catch (Exception ignored) {
        }
        task.run();
    }

    public static void runOnGlobalRegion(@NotNull Runnable task) {
        try {
            Object globalScheduler = getGlobalRegionScheduler();
            if (globalScheduler != null) {
                Method method = globalScheduler.getClass().getMethod("execute",
                        Class.forName("org.bukkit.plugin.Plugin"),
                        Runnable.class);
                Object plugin = getCallingPlugin();
                method.invoke(globalScheduler, plugin, task);
                return;
            }
        } catch (Exception ignored) {
        }
        task.run();
    }

    public static void runAsync(@NotNull Runnable task) {
        try {
            Object asyncScheduler = getAsyncScheduler();
            if (asyncScheduler != null) {
                Method method = asyncScheduler.getClass().getMethod("runNow",
                        Class.forName("org.bukkit.plugin.Plugin"),
                        Class.forName("java.util.function.Consumer"));
                Object plugin = getCallingPlugin();
                java.util.function.Consumer<Object> consumer = t -> task.run();
                method.invoke(asyncScheduler, plugin, consumer);
                return;
            }
        } catch (Exception ignored) {
        }
        new Thread(task).start();
    }

    @Nullable
    public static Object getCallingPlugin() {
        try {
            Class<?> javaPluginClass = Class.forName("org.bukkit.plugin.java.JavaPlugin");
            Method getProvidingPlugin = javaPluginClass.getMethod("getProvidingPlugin", Class.class);
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

            for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
                try {
                    Class<?> clazz = Class.forName(element.getClassName(), false, contextLoader);
                    Object plugin = getProvidingPlugin.invoke(null, clazz);
                    if (plugin != null) {
                        return plugin;
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static void ensureTickThread(@NotNull String operation) {
        if (isFolia()) {
            if (!isTickThread()) {
                throw new IllegalStateException(
                        operation + " must be executed on the tick thread when running on Folia");
            }
        }
    }

    public static void ensureRegionThread(@NotNull Object world, @NotNull String operation) {
        if (isFolia()) {
            if (!isOwnedByCurrentRegion(world)) {
                throw new IllegalStateException(
                        operation + " must be executed in the owning region for world " + world);
            }
        }
    }
}