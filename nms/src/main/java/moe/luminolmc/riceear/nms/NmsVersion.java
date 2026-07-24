package moe.luminolmc.riceear.nms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum NmsVersion {
    v1_8_R1("1.8", "1_8_R1", "v1_8_R1"),
    v1_8_R2("1.8.3", "1_8_R2", "v1_8_R2"),
    v1_8_R3("1.8.8", "1_8_R3", "v1_8_R3"),
    v1_9_R1("1.9", "1_9_R1", "v1_9_R1"),
    v1_9_R2("1.9.4", "1_9_R2", "v1_9_R2"),
    v1_10_R1("1.10", "1_10_R1", "v1_10_R1"),
    v1_11_R1("1.11", "1_11_R1", "v1_11_R1"),
    v1_12_R1("1.12", "1_12_R1", "v1_12_R1"),
    v1_13_R1("1.13", "1_13_R1", "v1_13_R1"),
    v1_13_R2("1.13.2", "1_13_R2", "v1_13_R2"),
    v1_14_R1("1.14", "1_14_R1", "v1_14_R1"),
    v1_15_R1("1.15", "1_15_R1", "v1_15_R1"),
    v1_16_R1("1.16.1", "1_16_R1", "v1_16_R1"),
    v1_16_R2("1.16.2", "1_16_R2", "v1_16_R2"),
    v1_16_R3("1.16.5", "1_16_R3", "v1_16_R3"),
    v1_17_R1("1.17", "1_17_R1", "v1_17_R1"),
    v1_18_R1("1.18", "1_18_R1", "v1_18_R1"),
    v1_18_R2("1.18.2", "1_18_R2", "v1_18_R2"),
    v1_19_R1("1.19", "1_19_R1", "v1_19_R1"),
    v1_19_R2("1.19.3", "1_19_R2", "v1_19_R2"),
    v1_19_R3("1.19.4", "1_19_R3", "v1_19_R3"),
    v1_20_R1("1.20", "1_20_R1", "v1_20_R1"),
    v1_20_R2("1.20.2", "1_20_R2", "v1_20_R2"),
    v1_20_R3("1.20.4", "1_20_R3", "v1_20_R3"),
    v1_20_R4("1.20.6", "1_20_R4", "v1_20_R4"),
    v1_21_R1("1.21", "1_21_R1", "v1_21_R1"),
    v1_21_R2("1.21.2", "1_21_R2", "v1_21_R2"),
    v1_21_R3("1.21.4", "1_21_R3", "v1_21_R3"),
    v1_21_R4("1.21.5", "1_21_R4", "v1_21_R4"),
    v1_22_R1("1.22", "1_22_R1", "v1_22_R1"),
    v1_22_R2("1.22.1", "1_22_R2", "v1_22_R2"),
    UNKNOWN("unknown", "unknown", "unknown");

    private final String mcVersion;
    private final String nmsVersion;
    private final String packageVersion;

    NmsVersion(String mcVersion, String nmsVersion, String packageVersion) {
        this.mcVersion = mcVersion;
        this.nmsVersion = nmsVersion;
        this.packageVersion = packageVersion;
    }

    public String getMcVersion() {
        return mcVersion;
    }

    public String getNmsVersion() {
        return nmsVersion;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public String getNmsPackage() {
        return "net.minecraft.server." + packageVersion;
    }

    public String getCraftBukkitPackage() {
        return "org.bukkit.craftbukkit." + packageVersion;
    }

    public boolean isAtLeast(@NotNull NmsVersion other) {
        return this.ordinal() >= other.ordinal();
    }

    public boolean isBefore(@NotNull NmsVersion other) {
        return this.ordinal() < other.ordinal();
    }

    public boolean isBetween(@NotNull NmsVersion from, @NotNull NmsVersion to) {
        return this.ordinal() >= from.ordinal() && this.ordinal() <= to.ordinal();
    }

    public boolean isAfter_1_16() {
        return isAtLeast(v1_17_R1);
    }

    public boolean isAfter_1_12() {
        return isAtLeast(v1_13_R1);
    }

    public boolean isAfter_1_8() {
        return isAtLeast(v1_9_R1);
    }

    public boolean isAtLeast_1_21_5() {
        return isAtLeast(v1_21_R4);
    }

    public boolean isAtLeast_1_22() {
        return isAtLeast(v1_22_R1);
    }

    @NotNull
    public static NmsVersion getLatest() {
        NmsVersion[] values = values();
        return values[values.length - 2];
    }

    @Nullable
    public static NmsVersion fromBukkitVersion(@NotNull String bukkitVersion) {
        for (NmsVersion version : values()) {
            if (version == UNKNOWN) continue;
            if (bukkitVersion.contains(version.packageVersion)
                    || bukkitVersion.contains(version.nmsVersion)) {
                return version;
            }
        }
        return UNKNOWN;
    }

    @Nullable
    public static NmsVersion fromMcVersion(@NotNull String mcVersion) {
        for (NmsVersion version : values()) {
            if (version.mcVersion.equals(mcVersion)) {
                return version;
            }
        }

        NmsVersion best = UNKNOWN;
        for (NmsVersion version : values()) {
            if (version == UNKNOWN) continue;
            if (mcVersion.startsWith(version.mcVersion)) {
                if (best == UNKNOWN || version.mcVersion.length() > best.mcVersion.length()) {
                    best = version;
                }
            }
        }
        return best;
    }
}