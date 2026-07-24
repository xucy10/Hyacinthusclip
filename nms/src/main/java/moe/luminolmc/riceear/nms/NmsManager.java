package moe.luminolmc.riceear.nms;

import moe.luminolmc.riceear.nms.wrappers.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NmsManager {

    private static volatile NmsManager instance;
    private final NmsVersion version;

    private NmsManager(@NotNull NmsVersion version) {
        this.version = version;
    }

    @NotNull
    public static NmsManager getInstance() {
        if (instance == null) {
            synchronized (NmsManager.class) {
                if (instance == null) {
                    instance = new NmsManager(VersionDetector.detect());
                }
            }
        }
        return instance;
    }

    @NotNull
    public NmsVersion getVersion() {
        return version;
    }

    @Nullable
    public Class<?> getNmsClass(@NotNull String key) {
        return NmsClassRegistry.getNmsClass(key, version);
    }

    @Nullable
    public Class<?> getCraftBukkitClass(@NotNull String key) {
        return NmsClassRegistry.getCraftBukkitClass(key, version);
    }

    public boolean isVersion(@NotNull NmsVersion version) {
        return this.version == version;
    }

    public boolean isAtLeast(@NotNull NmsVersion version) {
        return this.version.isAtLeast(version);
    }

    public boolean isBefore(@NotNull NmsVersion version) {
        return this.version.isBefore(version);
    }

    @NotNull
    public NmsEntity wrapEntity(@NotNull Object nmsEntity) {
        return new NmsEntity(nmsEntity, version);
    }

    @NotNull
    public NmsPlayer wrapPlayer(@NotNull Object nmsPlayer) {
        return new NmsPlayer(nmsPlayer, version);
    }

    @NotNull
    public NmsWorld wrapWorld(@NotNull Object nmsWorld) {
        return new NmsWorld(nmsWorld, version);
    }

    @NotNull
    public NmsItemStack wrapItemStack(@NotNull Object nmsItemStack) {
        return new NmsItemStack(nmsItemStack, version);
    }

    @NotNull
    public NmsPacket wrapPacket(@NotNull Object nmsPacket) {
        return new NmsPacket(nmsPacket, version);
    }

    @NotNull
    public NmsBlock wrapBlock(@NotNull Object nmsBlock) {
        return new NmsBlock(nmsBlock, version);
    }

    @NotNull
    public NmsChat wrapChat(@NotNull Object nmsChatComponent) {
        return new NmsChat(nmsChatComponent, version);
    }

    @NotNull
    public NmsServer wrapServer(@NotNull Object nmsServer) {
        return new NmsServer(nmsServer, version);
    }

    public boolean isFolia() {
        return FoliaSupport.isFolia();
    }

    @NotNull
    public FoliaScheduler getFoliaScheduler() {
        return new FoliaScheduler();
    }

    public static void reset() {
        VersionDetector.reset();
        NmsClassRegistry.clearCache();
        NmsReflection.clearCaches();
        instance = null;
    }
}