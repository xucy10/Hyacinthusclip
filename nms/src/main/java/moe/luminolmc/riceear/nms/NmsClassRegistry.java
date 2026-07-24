package moe.luminolmc.riceear.nms;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class NmsClassRegistry {

    private static final Map<String, Map<NmsVersion, String>> CLASS_MAPPINGS = new HashMap<>();
    private static final Map<String, Class<?>> RESOLVED_CLASSES = new HashMap<>();

    private NmsClassRegistry() {
        throw new UnsupportedOperationException("Registry class");
    }

    static {
        register("EntityPlayer",
                Map.of(NmsVersion.v1_8_R3, "EntityPlayer",
                        NmsVersion.v1_12_R1, "EntityPlayer",
                        NmsVersion.v1_16_R3, "EntityPlayer",
                        NmsVersion.v1_17_R1, "server.level.EntityPlayer",
                        NmsVersion.v1_20_R1, "server.level.ServerPlayer",
                        NmsVersion.v1_21_R1, "server.level.ServerPlayer"));

        register("PlayerConnection",
                Map.of(NmsVersion.v1_8_R3, "PlayerConnection",
                        NmsVersion.v1_12_R1, "PlayerConnection",
                        NmsVersion.v1_16_R3, "PlayerConnection",
                        NmsVersion.v1_17_R1, "server.network.PlayerConnection",
                        NmsVersion.v1_20_R1, "server.network.ServerGamePacketListenerImpl",
                        NmsVersion.v1_21_R1, "server.network.ServerGamePacketListenerImpl"));

        register("NetworkManager",
                Map.of(NmsVersion.v1_8_R3, "NetworkManager",
                        NmsVersion.v1_12_R1, "NetworkManager",
                        NmsVersion.v1_16_R3, "NetworkManager",
                        NmsVersion.v1_17_R1, "network.NetworkManager",
                        NmsVersion.v1_20_R1, "network.Connection",
                        NmsVersion.v1_21_R1, "network.Connection"));

        register("MinecraftServer",
                Map.of(NmsVersion.v1_8_R3, "MinecraftServer",
                        NmsVersion.v1_12_R1, "MinecraftServer",
                        NmsVersion.v1_16_R3, "MinecraftServer",
                        NmsVersion.v1_17_R1, "server.MinecraftServer",
                        NmsVersion.v1_20_R1, "server.MinecraftServer",
                        NmsVersion.v1_21_R1, "server.MinecraftServer"));

        register("WorldServer",
                Map.of(NmsVersion.v1_8_R3, "WorldServer",
                        NmsVersion.v1_12_R1, "WorldServer",
                        NmsVersion.v1_16_R3, "WorldServer",
                        NmsVersion.v1_17_R1, "server.level.WorldServer",
                        NmsVersion.v1_20_R1, "server.level.ServerLevel",
                        NmsVersion.v1_21_R1, "server.level.ServerLevel"));

        register("Entity",
                Map.of(NmsVersion.v1_8_R3, "Entity",
                        NmsVersion.v1_12_R1, "Entity",
                        NmsVersion.v1_16_R3, "Entity",
                        NmsVersion.v1_17_R1, "world.entity.Entity",
                        NmsVersion.v1_20_R1, "world.entity.Entity",
                        NmsVersion.v1_21_R1, "world.entity.Entity"));

        register("EntityLiving",
                Map.of(NmsVersion.v1_8_R3, "EntityLiving",
                        NmsVersion.v1_12_R1, "EntityLiving",
                        NmsVersion.v1_16_R3, "EntityLiving",
                        NmsVersion.v1_17_R1, "world.entity.EntityLiving",
                        NmsVersion.v1_20_R1, "world.entity.LivingEntity",
                        NmsVersion.v1_21_R1, "world.entity.LivingEntity"));

        register("EntityHuman",
                Map.of(NmsVersion.v1_8_R3, "EntityHuman",
                        NmsVersion.v1_12_R1, "EntityHuman",
                        NmsVersion.v1_16_R3, "EntityHuman",
                        NmsVersion.v1_17_R1, "world.entity.player.EntityHuman",
                        NmsVersion.v1_20_R1, "world.entity.player.Player",
                        NmsVersion.v1_21_R1, "world.entity.player.Player"));

        register("ItemStack",
                Map.of(NmsVersion.v1_8_R3, "ItemStack",
                        NmsVersion.v1_12_R1, "ItemStack",
                        NmsVersion.v1_16_R3, "ItemStack",
                        NmsVersion.v1_17_R1, "world.item.ItemStack",
                        NmsVersion.v1_20_R1, "world.item.ItemStack",
                        NmsVersion.v1_21_R1, "world.item.ItemStack"));

        register("Block",
                Map.of(NmsVersion.v1_8_R3, "Block",
                        NmsVersion.v1_12_R1, "Block",
                        NmsVersion.v1_16_R3, "Block",
                        NmsVersion.v1_17_R1, "world.level.block.Block",
                        NmsVersion.v1_20_R1, "world.level.block.Block",
                        NmsVersion.v1_21_R1, "world.level.block.Block"));

        register("BlockPosition",
                Map.of(NmsVersion.v1_8_R3, "BlockPosition",
                        NmsVersion.v1_12_R1, "BlockPosition",
                        NmsVersion.v1_16_R3, "BlockPosition",
                        NmsVersion.v1_17_R1, "core.BlockPosition",
                        NmsVersion.v1_20_R1, "core.BlockPos",
                        NmsVersion.v1_21_R1, "core.BlockPos"));

        register("IChatBaseComponent",
                Map.of(NmsVersion.v1_8_R3, "IChatBaseComponent",
                        NmsVersion.v1_12_R1, "IChatBaseComponent",
                        NmsVersion.v1_16_R3, "IChatBaseComponent",
                        NmsVersion.v1_17_R1, "network.chat.IChatBaseComponent",
                        NmsVersion.v1_20_R1, "network.chat.Component",
                        NmsVersion.v1_21_R1, "network.chat.Component"));

        register("ChatMessageType",
                Map.of(NmsVersion.v1_8_R3, "ChatMessageType",
                        NmsVersion.v1_12_R1, "ChatMessageType",
                        NmsVersion.v1_16_R3, "ChatMessageType",
                        NmsVersion.v1_17_R1, "network.chat.ChatMessageType",
                        NmsVersion.v1_20_R1, "network.chat.ChatType",
                        NmsVersion.v1_21_R1, "network.chat.ChatType"));

        register("Packet",
                Map.of(NmsVersion.v1_8_R3, "Packet",
                        NmsVersion.v1_12_R1, "Packet",
                        NmsVersion.v1_16_R3, "Packet",
                        NmsVersion.v1_17_R1, "network.protocol.Packet",
                        NmsVersion.v1_20_R1, "network.protocol.Packet",
                        NmsVersion.v1_21_R1, "network.protocol.Packet"));

        register("PacketPlayOutChat",
                Map.of(NmsVersion.v1_8_R3, "PacketPlayOutChat",
                        NmsVersion.v1_12_R1, "PacketPlayOutChat",
                        NmsVersion.v1_16_R3, "PacketPlayOutChat",
                        NmsVersion.v1_17_R1, "network.protocol.game.PacketPlayOutChat",
                        NmsVersion.v1_20_R1, "network.protocol.game.ClientboundSystemChatPacket",
                        NmsVersion.v1_21_R1, "network.protocol.game.ClientboundSystemChatPacket"));

        register("PacketPlayOutTitle",
                Map.of(NmsVersion.v1_8_R3, "PacketPlayOutTitle",
                        NmsVersion.v1_12_R1, "PacketPlayOutTitle",
                        NmsVersion.v1_16_R3, "PacketPlayOutTitle",
                        NmsVersion.v1_17_R1, "network.protocol.game.PacketPlayOutTitle",
                        NmsVersion.v1_20_R1, "network.protocol.game.ClientboundSetTitleTextPacket",
                        NmsVersion.v1_21_R1, "network.protocol.game.ClientboundSetTitleTextPacket"));

        register("PacketPlayOutPlayerInfo",
                Map.of(NmsVersion.v1_8_R3, "PacketPlayOutPlayerInfo",
                        NmsVersion.v1_12_R1, "PacketPlayOutPlayerInfo",
                        NmsVersion.v1_16_R3, "PacketPlayOutPlayerInfo",
                        NmsVersion.v1_17_R1, "network.protocol.game.PacketPlayOutPlayerInfo",
                        NmsVersion.v1_20_R1, "network.protocol.game.ClientboundPlayerInfoUpdatePacket",
                        NmsVersion.v1_21_R1, "network.protocol.game.ClientboundPlayerInfoUpdatePacket"));

        register("PacketPlayOutEntityMetadata",
                Map.of(NmsVersion.v1_8_R3, "PacketPlayOutEntityMetadata",
                        NmsVersion.v1_12_R1, "PacketPlayOutEntityMetadata",
                        NmsVersion.v1_16_R3, "PacketPlayOutEntityMetadata",
                        NmsVersion.v1_17_R1, "network.protocol.game.PacketPlayOutEntityMetadata",
                        NmsVersion.v1_20_R1, "network.protocol.game.ClientboundSetEntityDataPacket",
                        NmsVersion.v1_21_R1, "network.protocol.game.ClientboundSetEntityDataPacket"));

        register("PacketPlayOutNamedEntitySpawn",
                Map.of(NmsVersion.v1_8_R3, "PacketPlayOutNamedEntitySpawn",
                        NmsVersion.v1_12_R1, "PacketPlayOutNamedEntitySpawn",
                        NmsVersion.v1_16_R3, "PacketPlayOutNamedEntitySpawn",
                        NmsVersion.v1_17_R1, "network.protocol.game.PacketPlayOutNamedEntitySpawn",
                        NmsVersion.v1_20_R1, "network.protocol.game.ClientboundAddPlayerPacket",
                        NmsVersion.v1_21_R1, "network.protocol.game.ClientboundAddPlayerPacket"));

        register("PacketPlayOutEntityDestroy",
                Map.of(NmsVersion.v1_8_R3, "PacketPlayOutEntityDestroy",
                        NmsVersion.v1_12_R1, "PacketPlayOutEntityDestroy",
                        NmsVersion.v1_16_R3, "PacketPlayOutEntityDestroy",
                        NmsVersion.v1_17_R1, "network.protocol.game.PacketPlayOutEntityDestroy",
                        NmsVersion.v1_20_R1, "network.protocol.game.ClientboundRemoveEntitiesPacket",
                        NmsVersion.v1_21_R1, "network.protocol.game.ClientboundRemoveEntitiesPacket"));

        register("DataWatcher",
                Map.of(NmsVersion.v1_8_R3, "DataWatcher",
                        NmsVersion.v1_12_R1, "DataWatcher",
                        NmsVersion.v1_16_R3, "DataWatcher",
                        NmsVersion.v1_17_R1, "network.syncher.DataWatcher",
                        NmsVersion.v1_20_R1, "network.syncher.SynchedEntityData",
                        NmsVersion.v1_21_R1, "network.syncher.SynchedEntityData"));

        register("CraftServer",
                Map.of(NmsVersion.v1_8_R3, "CraftServer",
                        NmsVersion.v1_12_R1, "CraftServer",
                        NmsVersion.v1_16_R3, "CraftServer",
                        NmsVersion.v1_17_R1, "CraftServer",
                        NmsVersion.v1_20_R1, "CraftServer",
                        NmsVersion.v1_21_R1, "CraftServer"));

        register("CraftWorld",
                Map.of(NmsVersion.v1_8_R3, "CraftWorld",
                        NmsVersion.v1_12_R1, "CraftWorld",
                        NmsVersion.v1_16_R3, "CraftWorld",
                        NmsVersion.v1_17_R1, "CraftWorld",
                        NmsVersion.v1_20_R1, "CraftWorld",
                        NmsVersion.v1_21_R1, "CraftWorld"));

        register("CraftPlayer",
                Map.of(NmsVersion.v1_8_R3, "entity.CraftPlayer",
                        NmsVersion.v1_12_R1, "entity.CraftPlayer",
                        NmsVersion.v1_16_R3, "entity.CraftPlayer",
                        NmsVersion.v1_17_R1, "entity.CraftPlayer",
                        NmsVersion.v1_20_R1, "entity.CraftPlayer",
                        NmsVersion.v1_21_R1, "entity.CraftPlayer"));

        register("CraftEntity",
                Map.of(NmsVersion.v1_8_R3, "entity.CraftEntity",
                        NmsVersion.v1_12_R1, "entity.CraftEntity",
                        NmsVersion.v1_16_R3, "entity.CraftEntity",
                        NmsVersion.v1_17_R1, "entity.CraftEntity",
                        NmsVersion.v1_20_R1, "entity.CraftEntity",
                        NmsVersion.v1_21_R1, "entity.CraftEntity"));

        register("CraftItemStack",
                Map.of(NmsVersion.v1_8_R3, "inventory.CraftItemStack",
                        NmsVersion.v1_12_R1, "inventory.CraftItemStack",
                        NmsVersion.v1_16_R3, "inventory.CraftItemStack",
                        NmsVersion.v1_17_R1, "inventory.CraftItemStack",
                        NmsVersion.v1_20_R1, "inventory.CraftItemStack",
                        NmsVersion.v1_21_R1, "inventory.CraftItemStack"));
    }

    private static void register(@NotNull String key, @NotNull Map<NmsVersion, String> mappings) {
        Map<NmsVersion, String> versionMap = new EnumMap<>(NmsVersion.class);
        versionMap.putAll(mappings);

        NmsVersion lastVersion = NmsVersion.UNKNOWN;
        String lastName = null;
        for (NmsVersion version : NmsVersion.values()) {
            if (version == NmsVersion.UNKNOWN) continue;
            if (versionMap.containsKey(version)) {
                lastVersion = version;
                lastName = versionMap.get(version);
            } else if (lastName != null) {
                versionMap.put(version, lastName);
            }
        }

        CLASS_MAPPINGS.put(key, Collections.unmodifiableMap(versionMap));
    }

    @Nullable
    public static String getClassName(@NotNull String key, @NotNull NmsVersion version) {
        Map<NmsVersion, String> mappings = CLASS_MAPPINGS.get(key);
        if (mappings == null) return null;

        String name = mappings.get(version);
        if (name != null) return name;

        NmsVersion best = null;
        for (NmsVersion v : mappings.keySet()) {
            if (v.ordinal() <= version.ordinal() && (best == null || v.ordinal() > best.ordinal())) {
                best = v;
            }
        }
        return best != null ? mappings.get(best) : null;
    }

    @Nullable
    public static Class<?> getNmsClass(@NotNull String key, @NotNull NmsVersion version) {
        String className = getClassName(key, version);
        if (className == null) return null;

        String cacheKey = version.name() + ":" + key;
        return RESOLVED_CLASSES.computeIfAbsent(cacheKey, k -> {
            String fullName = className.contains(".") ? className : version.getNmsPackage() + "." + className;
            return NmsReflection.getClass(fullName);
        });
    }

    @Nullable
    public static Class<?> getCraftBukkitClass(@NotNull String key, @NotNull NmsVersion version) {
        String className = getClassName(key, version);
        if (className == null) return null;

        String cacheKey = "cb:" + version.name() + ":" + key;
        return RESOLVED_CLASSES.computeIfAbsent(cacheKey, k -> {
            String fullName = version.getCraftBukkitPackage() + "." + className;
            return NmsReflection.getClass(fullName);
        });
    }

    public static void clearCache() {
        RESOLVED_CLASSES.clear();
    }
}