package moe.luminolmc.riceear.nms.wrappers;

import moe.luminolmc.riceear.nms.FoliaSupport;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * Unified task scheduler wrapper that transparently handles both the classic Bukkit
 * scheduler and the Folia schedulers ({@code RegionScheduler}, {@code GlobalRegionScheduler},
 * {@code AsyncScheduler}) present in Folia-based cores such as Mili and Luminol.
 *
 * <p>The Folia scheduler APIs are invoked reflectively so this class compiles against a
 * plain Bukkit API classpath and degrades gracefully on non-Folia servers.</p>
 */
public class NmsScheduler {

    /**
     * Runs the task on the region that owns the given location (Folia) or on the main
     * thread (classic Paper/Spigot).
     */
    public static void runAtLocation(@NotNull Object world, double x, double z, @NotNull Runnable task) {
        if (FoliaSupport.isFoliaClassPresent()) {
            Object regionScheduler = getScheduler("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            if (regionScheduler != null) {
                Method execute = findMethod(regionScheduler.getClass(), "execute", Object.class,
                        locationClass(), Consumer.class);
                if (execute != null) {
                    invoke(execute, regionScheduler, null, toLocation(world, x, z), (Consumer<Object>) t -> task.run());
                    return;
                }
            }
        }
        runSync(task);
    }

    /**
     * Runs the task on the global region (Folia) or the main thread (classic Bukkit).
     */
    public static void runGlobal(@NotNull Runnable task) {
        if (FoliaSupport.isFoliaClassPresent()) {
            Object scheduler = getScheduler("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            if (scheduler != null) {
                Method execute = findMethod(scheduler.getClass(), "execute", Consumer.class);
                if (execute != null) {
                    invoke(execute, scheduler, (Consumer<Object>) t -> task.run());
                    return;
                }
            }
        }
        runSync(task);
    }

    /**
     * Runs the task asynchronously. On Folia this uses the {@code AsyncScheduler}; elsewhere
     * the Bukkit async scheduler.
     */
    public static void runAsync(@NotNull Runnable task) {
        if (FoliaSupport.isFoliaClassPresent()) {
            Object scheduler = getScheduler("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            if (scheduler != null) {
                Method runNow = findMethod(scheduler.getClass(), "runNow", Object.class, Consumer.class);
                if (runNow != null) {
                    invoke(runNow, scheduler, null, (Consumer<Object>) t -> task.run());
                    return;
                }
            }
        }
        Object bukkitScheduler = getBukkitScheduler();
        if (bukkitScheduler != null) {
            Method runTaskAsynchronously = findMethod(bukkitScheduler.getClass(), "runTaskAsynchronously",
                    pluginClass(), Runnable.class);
            if (runTaskAsynchronously != null) {
                invoke(runTaskAsynchronously, bukkitScheduler, null, task);
            }
        }
    }

    /**
     * Runs the task on the global region (or main thread) after the given delay in ticks.
     */
    public static void runGlobalDelayed(@NotNull Runnable task, long delayTicks) {
        if (FoliaSupport.isFoliaClassPresent()) {
            Object scheduler = getScheduler("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            if (scheduler != null) {
                Method runDelayed = findMethod(scheduler.getClass(), "runDelayed", Consumer.class, long.class);
                if (runDelayed != null) {
                    invoke(runDelayed, scheduler, (Consumer<Object>) t -> task.run(), delayTicks);
                    return;
                }
            }
        }
        runSyncDelayed(task, delayTicks);
    }

    /**
     * Runs the task asynchronously after the given delay in ticks.
     */
    public static void runAsyncDelayed(@NotNull Runnable task, long delayTicks) {
        if (FoliaSupport.isFoliaClassPresent()) {
            Object scheduler = getScheduler("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            if (scheduler != null) {
                // Folia async API works with durations, 1 tick = 50 ms.
                Method runDelayed = findMethod(scheduler.getClass(), "runDelayed", Object.class,
                        java.time.Duration.class, Consumer.class);
                if (runDelayed != null) {
                    invoke(runDelayed, scheduler, null, java.time.Duration.ofMillis(delayTicks * 50L),
                            (Consumer<Object>) t -> task.run());
                    return;
                }
            }
        }
        Object bukkitScheduler = getBukkitScheduler();
        if (bukkitScheduler != null) {
            Method runTaskLater = findMethod(bukkitScheduler.getClass(), "runTaskLaterAsynchronously",
                    pluginClass(), Runnable.class, long.class);
            if (runTaskLater != null) {
                invoke(runTaskLater, bukkitScheduler, null, task, delayTicks);
            }
        }
    }

    /**
     * Cancels a scheduled Folia task handle ({@code ScheduledTask}) if applicable.
     */
    public static void cancel(@Nullable Object scheduledTask) {
        if (scheduledTask == null) {
            return;
        }
        Method cancel = findMethod(scheduledTask.getClass(), "cancel");
        if (cancel != null) {
            invoke(cancel, scheduledTask);
        }
    }

    /**
     * Converts a delay in ticks to the duration used by the Folia async schedulers.
     */
    public static java.time.Duration ticksToDuration(long ticks) {
        return java.time.Duration.ofMillis(ticks * 50L);
    }

    /**
     * Converts a duration to a best-effort tick count (1 tick = 50 ms).
     */
    public static long durationToTicks(@NotNull java.time.Duration duration) {
        return duration.toMillis() / 50L;
    }

    // ---- classic Bukkit fallbacks --------------------------------------------------

    private static void runSync(@NotNull Runnable task) {
        Object bukkitScheduler = getBukkitScheduler();
        if (bukkitScheduler != null) {
            Method runTask = findMethod(bukkitScheduler.getClass(), "runTask", pluginClass(), Runnable.class);
            if (runTask != null) {
                invoke(runTask, bukkitScheduler, null, task);
            }
        }
    }

    private static void runSyncDelayed(@NotNull Runnable task, long delayTicks) {
        Object bukkitScheduler = getBukkitScheduler();
        if (bukkitScheduler != null) {
            Method runTaskLater = findMethod(bukkitScheduler.getClass(), "runTaskLater",
                    pluginClass(), Runnable.class, long.class);
            if (runTaskLater != null) {
                invoke(runTaskLater, bukkitScheduler, null, task, delayTicks);
            }
        }
    }

    @Nullable
    private static Object getScheduler(@NotNull String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Method provider = findMethod(clazz, "get");
            if (provider != null) {
                return invoke(provider, null);
            }
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static Object getBukkitScheduler() {
        try {
            Method getScheduler = Class.forName("org.bukkit.Bukkit").getMethod("getScheduler");
            return invoke(getScheduler, null);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static Class<?> locationClass() {
        try {
            return Class.forName("org.bukkit.Location");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Nullable
    private static Class<?> pluginClass() {
        try {
            return Class.forName("org.bukkit.plugin.Plugin");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Nullable
    private static Object toLocation(@NotNull Object world, double x, double z) {
        try {
            Class<?> locationClass = locationClass();
            if (locationClass == null) {
                return null;
            }
            var constructor = locationClass.getConstructor(world.getClass(), double.class, double.class, double.class);
            return constructor.newInstance(world, x, 0.0, z);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static Method findMethod(@NotNull Class<?> clazz, @NotNull String name, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @Nullable
    private static Object invoke(@NotNull Method method, @Nullable Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            return null;
        }
    }

    private NmsScheduler() {
        throw new UnsupportedOperationException("Utility class");
    }
}
