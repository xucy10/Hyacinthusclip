package moe.luminolmc.riceear.nms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class FoliaScheduler {

    private final boolean isFolia;
    @Nullable
    private final Object globalRegionScheduler;
    @Nullable
    private final Object asyncScheduler;
    @Nullable
    private final Object regionScheduler;

    public FoliaScheduler() {
        this.isFolia = FoliaSupport.isFolia();
        this.globalRegionScheduler = FoliaSupport.getGlobalRegionScheduler();
        this.asyncScheduler = FoliaSupport.getAsyncScheduler();
        this.regionScheduler = FoliaSupport.getRegionScheduler();
    }

    @NotNull
    public static FoliaScheduler get() {
        return new FoliaScheduler();
    }

    public boolean isFolia() {
        return isFolia;
    }

    public void global(@NotNull Runnable task) {
        if (!isFolia || globalRegionScheduler == null) {
            task.run();
            return;
        }
        try {
            Method execute = globalRegionScheduler.getClass().getMethod("execute",
                    Class.forName("org.bukkit.plugin.Plugin"), Runnable.class);
            Object plugin = FoliaSupport.getCallingPlugin();
            execute.invoke(globalRegionScheduler, plugin, task);
        } catch (Exception e) {
            task.run();
        }
    }

    public void globalDelayed(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        if (!isFolia || globalRegionScheduler == null) {
            try {
                Thread.sleep(unit.toMillis(delay));
            } catch (InterruptedException ignored) {
            }
            task.run();
            return;
        }
        try {
            Method execute = globalRegionScheduler.getClass().getMethod("runDelayed",
                    Class.forName("org.bukkit.plugin.Plugin"),
                    Consumer.class, long.class);
            Object plugin = FoliaSupport.getCallingPlugin();
            Consumer<Object> consumer = t -> task.run();
            execute.invoke(globalRegionScheduler, plugin, consumer, unit.toMillis(delay) / 50);
        } catch (Exception e) {
            task.run();
        }
    }

    public void globalAtFixedRate(@NotNull Runnable task, long initialDelay, long period,
                                   @NotNull TimeUnit unit) {
        if (!isFolia || globalRegionScheduler == null) {
            try {
                Thread.sleep(unit.toMillis(initialDelay));
            } catch (InterruptedException ignored) {
            }
            while (true) {
                try {
                    task.run();
                    Thread.sleep(unit.toMillis(period));
                } catch (InterruptedException ignored) {
                    break;
                }
            }
            return;
        }
        try {
            Method execute = globalRegionScheduler.getClass().getMethod("runAtFixedRate",
                    Class.forName("org.bukkit.plugin.Plugin"),
                    Consumer.class, long.class, long.class);
            Object plugin = FoliaSupport.getCallingPlugin();
            Consumer<Object> consumer = t -> task.run();
            execute.invoke(globalRegionScheduler, plugin, consumer,
                    unit.toMillis(initialDelay) / 50, unit.toMillis(period) / 50);
        } catch (Exception ignored) {
        }
    }

    public void region(@NotNull Object world, int chunkX, int chunkZ, @NotNull Runnable task) {
        if (!isFolia || regionScheduler == null) {
            task.run();
            return;
        }
        try {
            Method execute = regionScheduler.getClass().getMethod("execute",
                    Class.forName("org.bukkit.plugin.Plugin"),
                    Class.forName("org.bukkit.World"),
                    int.class, int.class, Runnable.class);
            Object plugin = FoliaSupport.getCallingPlugin();
            execute.invoke(regionScheduler, plugin, world, chunkX, chunkZ, task);
        } catch (Exception e) {
            task.run();
        }
    }

    public void regionDelayed(@NotNull Object world, int chunkX, int chunkZ,
                               @NotNull Runnable task, long delayTicks) {
        if (!isFolia || regionScheduler == null) {
            try {
                Thread.sleep(delayTicks * 50);
            } catch (InterruptedException ignored) {
            }
            task.run();
            return;
        }
        try {
            Method execute = regionScheduler.getClass().getMethod("runDelayed",
                    Class.forName("org.bukkit.plugin.Plugin"),
                    Class.forName("org.bukkit.World"),
                    int.class, int.class,
                    Consumer.class, long.class);
            Object plugin = FoliaSupport.getCallingPlugin();
            Consumer<Object> consumer = t -> task.run();
            execute.invoke(regionScheduler, plugin, world, chunkX, chunkZ, consumer, delayTicks);
        } catch (Exception e) {
            task.run();
        }
    }

    public void entity(@NotNull Object entity, @NotNull Runnable task) {
        entity(entity, task, null);
    }

    public void entity(@NotNull Object entity, @NotNull Runnable task, @Nullable Runnable retired) {
        if (!isFolia) {
            task.run();
            return;
        }
        try {
            Object entityScheduler = FoliaSupport.getEntityScheduler(entity);
            if (entityScheduler != null) {
                Method execute = entityScheduler.getClass().getMethod("execute",
                        Class.forName("org.bukkit.plugin.Plugin"),
                        Runnable.class, Runnable.class, long.class);
                Object plugin = FoliaSupport.getCallingPlugin();
                execute.invoke(entityScheduler, plugin, task, retired, 1L);
                return;
            }
        } catch (Exception ignored) {
        }
        task.run();
    }

    public void async(@NotNull Runnable task) {
        if (!isFolia || asyncScheduler == null) {
            new Thread(task).start();
            return;
        }
        try {
            Method runNow = asyncScheduler.getClass().getMethod("runNow",
                    Class.forName("org.bukkit.plugin.Plugin"),
                    Consumer.class);
            Object plugin = FoliaSupport.getCallingPlugin();
            Consumer<Object> consumer = t -> task.run();
            runNow.invoke(asyncScheduler, plugin, consumer);
        } catch (Exception e) {
            new Thread(task).start();
        }
    }

    public void asyncDelayed(@NotNull Runnable task, long delay, @NotNull TimeUnit unit) {
        if (!isFolia || asyncScheduler == null) {
            new Thread(() -> {
                try {
                    Thread.sleep(unit.toMillis(delay));
                } catch (InterruptedException ignored) {
                }
                task.run();
            }).start();
            return;
        }
        try {
            Method runDelayed = asyncScheduler.getClass().getMethod("runDelayed",
                    Class.forName("org.bukkit.plugin.Plugin"),
                    Consumer.class, long.class, TimeUnit.class);
            Object plugin = FoliaSupport.getCallingPlugin();
            Consumer<Object> consumer = t -> task.run();
            runDelayed.invoke(asyncScheduler, plugin, consumer, delay, unit);
        } catch (Exception e) {
            new Thread(() -> {
                try {
                    Thread.sleep(unit.toMillis(delay));
                } catch (InterruptedException ignored) {
                }
                task.run();
            }).start();
        }
    }

    public void cancelAll() {
        if (!isFolia) return;
        try {
            if (globalRegionScheduler != null) {
                Method cancel = globalRegionScheduler.getClass().getMethod("cancel",
                        Class.forName("org.bukkit.plugin.Plugin"));
                Object plugin = FoliaSupport.getCallingPlugin();
                cancel.invoke(globalRegionScheduler, plugin);
            }
            if (asyncScheduler != null) {
                Method cancel = asyncScheduler.getClass().getMethod("cancel",
                        Class.forName("org.bukkit.plugin.Plugin"));
                Object plugin = FoliaSupport.getCallingPlugin();
                cancel.invoke(asyncScheduler, plugin);
            }
        } catch (Exception ignored) {
        }
    }
}