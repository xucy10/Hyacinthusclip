package moe.luminolmc.riceear.nms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VersionDetector {

    private static volatile NmsVersion detectedVersion;
    private static volatile String rawVersion;

    private VersionDetector() {
        throw new UnsupportedOperationException("Utility class");
    }

    @NotNull
    public static NmsVersion detect() {
        if (detectedVersion != null) {
            return detectedVersion;
        }

        synchronized (VersionDetector.class) {
            if (detectedVersion != null) {
                return detectedVersion;
            }

            detectedVersion = detectInternal();
            return detectedVersion;
        }
    }

    @NotNull
    private static NmsVersion detectInternal() {
        rawVersion = tryGetBukkitVersion();
        if (rawVersion != null) {
            NmsVersion version = NmsVersion.fromBukkitVersion(rawVersion);
            if (version != null && version != NmsVersion.UNKNOWN) {
                return version;
            }
        }

        String mcVersion = tryGetMinecraftVersion();
        if (mcVersion != null) {
            NmsVersion version = NmsVersion.fromMcVersion(mcVersion);
            if (version != null && version != NmsVersion.UNKNOWN) {
                return version;
            }
        }

        NmsVersion version = tryDetectFromPackage();
        if (version != NmsVersion.UNKNOWN) {
            return version;
        }

        return NmsVersion.UNKNOWN;
    }

    @Nullable
    private static String tryGetBukkitVersion() {
        try {
            Class<?> bukkitClass = Class.forName("org.bukkit.Bukkit");
            Object server = bukkitClass.getMethod("getServer").invoke(null);
            Object bukkitVersion = server.getClass().getMethod("getBukkitVersion").invoke(server);
            return (String) bukkitVersion;
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static String tryGetMinecraftVersion() {
        try {
            Class<?> sharedConstantsClass = Class.forName("net.minecraft.SharedConstants");
            Object version = sharedConstantsClass.getMethod("getCurrentVersion").invoke(null);
            Object name = version.getClass().getMethod("getName").invoke(version);
            return (String) name;
        } catch (Exception e) {
            return null;
        }
    }

    @NotNull
    private static NmsVersion tryDetectFromPackage() {
        for (Package pkg : Package.getPackages()) {
            String name = pkg.getName();
            if (name.startsWith("net.minecraft.server.")) {
                String versionPart = name.substring("net.minecraft.server.".length());
                NmsVersion version = NmsVersion.fromBukkitVersion(versionPart);
                if (version != NmsVersion.UNKNOWN) {
                    return version;
                }
            }
            if (name.startsWith("net.minecraft.server.v")) {
                String versionPart = name.substring("net.minecraft.server.".length());
                for (NmsVersion version : NmsVersion.values()) {
                    if (version == NmsVersion.UNKNOWN) continue;
                    if (versionPart.equals(version.getPackageVersion())) {
                        return version;
                    }
                }
            }
        }

        return NmsVersion.UNKNOWN;
    }

    @Nullable
    public static String getRawVersion() {
        if (rawVersion == null) {
            detect();
        }
        return rawVersion;
    }

    public static void reset() {
        synchronized (VersionDetector.class) {
            detectedVersion = null;
            rawVersion = null;
        }
    }
}