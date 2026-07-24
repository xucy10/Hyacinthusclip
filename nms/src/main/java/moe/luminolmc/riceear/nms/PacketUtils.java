package moe.luminolmc.riceear.nms;

import moe.luminolmc.riceear.nms.wrappers.NmsChat;
import moe.luminolmc.riceear.nms.wrappers.NmsPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.UUID;

public final class PacketUtils {

    private final NmsVersion version;

    public PacketUtils(@NotNull NmsVersion version) {
        this.version = version;
    }

    @NotNull
    public static PacketUtils get() {
        return new PacketUtils(NmsManager.getInstance().getVersion());
    }

    @Nullable
    public NmsPacket createChatPacket(@NotNull String message) {
        NmsChat chat = NmsChat.fromText(version, message);
        if (chat == null) return null;

        Class<?> packetClass = NmsClassRegistry.getNmsClass("PacketPlayOutChat", version);
        if (packetClass == null) return null;

        try {
            if (version.isAtLeast(NmsVersion.v1_20_R1)) {
                Constructor<?> constructor = NmsReflection.getConstructor(packetClass,
                        chat.getHandle().getClass(), boolean.class);
                if (constructor != null) {
                    Object packet = NmsReflection.newInstance(constructor, chat.getHandle(), false);
                    return new NmsPacket(packet, version);
                }
            } else if (version.isAtLeast(NmsVersion.v1_17_R1)) {
                Constructor<?> constructor = NmsReflection.getConstructor(packetClass,
                        chat.getHandle().getClass(), int.class);
                if (constructor != null) {
                    Object packet = NmsReflection.newInstance(constructor, chat.getHandle(), 1);
                    return new NmsPacket(packet, version);
                }
            } else {
                Constructor<?> constructor = NmsReflection.getConstructor(packetClass,
                        chat.getHandle().getClass(), byte.class);
                if (constructor != null) {
                    Object packet = NmsReflection.newInstance(constructor, chat.getHandle(), (byte) 1);
                    return new NmsPacket(packet, version);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Nullable
    public NmsPacket createTitlePacket(@NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
        Class<?> titlePacketClass = NmsClassRegistry.getNmsClass("PacketPlayOutTitle", version);
        if (titlePacketClass == null) return null;

        try {
            if (version.isAtLeast(NmsVersion.v1_20_R1)) {
                NmsChat titleChat = NmsChat.fromText(version, title);
                if (titleChat != null) {
                    Constructor<?> constructor = NmsReflection.getConstructor(titlePacketClass,
                            titleChat.getHandle().getClass());
                    if (constructor != null) {
                        Object packet = NmsReflection.newInstance(constructor, titleChat.getHandle());
                        return new NmsPacket(packet, version);
                    }
                }
            } else if (version.isAtLeast(NmsVersion.v1_17_R1)) {
                Class<?> enumTitleAction = NmsReflection.getNmsClass(version, "network.protocol.game.PacketPlayOutTitle$EnumTitleAction");
                if (enumTitleAction != null) {
                    Constructor<?> constructor = NmsReflection.getConstructor(titlePacketClass,
                            enumTitleAction, NmsClassRegistry.getNmsClass("IChatBaseComponent", version));
                    if (constructor != null) {
                        NmsChat titleChat = NmsChat.fromText(version, title);
                        if (titleChat != null) {
                            Object[] actions = enumTitleAction.getEnumConstants();
                            Object packet = NmsReflection.newInstance(constructor, actions[0], titleChat.getHandle());
                            return new NmsPacket(packet, version);
                        }
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Nullable
    public NmsPacket createEntityDestroyPacket(int... entityIds) {
        Class<?> packetClass = NmsClassRegistry.getNmsClass("PacketPlayOutEntityDestroy", version);
        if (packetClass == null) return null;

        try {
            if (version.isAtLeast(NmsVersion.v1_20_R1)) {
                Constructor<?> constructor = NmsReflection.getConstructor(packetClass, int[].class);
                if (constructor != null) {
                    Object packet = NmsReflection.newInstance(constructor, (Object) entityIds);
                    return new NmsPacket(packet, version);
                }
            } else {
                Constructor<?> constructor = NmsReflection.getConstructor(packetClass, int[].class);
                if (constructor != null) {
                    Object packet = NmsReflection.newInstance(constructor, (Object) entityIds);
                    return new NmsPacket(packet, version);
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Nullable
    public NmsPacket createPlayerInfoPacket(@NotNull UUID playerId, @NotNull String playerName, int ping, boolean add) {
        Class<?> packetClass = NmsClassRegistry.getNmsClass("PacketPlayOutPlayerInfo", version);
        if (packetClass == null) return null;

        try {
            if (version.isAtLeast(NmsVersion.v1_20_R1)) {
                return null;
            } else if (version.isAtLeast(NmsVersion.v1_17_R1)) {
                Class<?> enumAction = NmsReflection.getNmsClass(version, "network.protocol.game.PacketPlayOutPlayerInfo$EnumPlayerInfoAction");
                if (enumAction != null) {
                    Object[] actions = enumAction.getEnumConstants();
                    Constructor<?> constructor = NmsReflection.getConstructor(packetClass, enumAction);
                    if (constructor != null) {
                        Object packet = NmsReflection.newInstance(constructor, add ? actions[0] : actions[4]);
                        return new NmsPacket(packet, version);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    public void sendPacketToAll(@NotNull NmsPacket packet) {
        CraftBukkitBridge bridge = new CraftBukkitBridge(version);
        Object server = bridge.getMinecraftServer();
        if (server == null) return;

        Object playerList = null;
        Method getPlayerListMethod = NmsReflection.getMethodRecursive(server.getClass(), "getPlayerList");
        if (getPlayerListMethod != null) {
            playerList = NmsReflection.invokeMethod(getPlayerListMethod, server);
        }

        if (playerList == null) return;

        String sendMethodName = version.isAtLeast(NmsVersion.v1_20_R1) ? "broadcastAll" : "sendAll";
        Method broadcastMethod = NmsReflection.getMethodRecursive(playerList.getClass(), sendMethodName, packet.getHandle().getClass());
        if (broadcastMethod == null) {
            broadcastMethod = NmsReflection.getMethodRecursive(playerList.getClass(), "sendPacketToAllPlayers", packet.getHandle().getClass());
        }
        if (broadcastMethod != null) {
            NmsReflection.invokeMethod(broadcastMethod, playerList, packet.getHandle());
        }
    }
}