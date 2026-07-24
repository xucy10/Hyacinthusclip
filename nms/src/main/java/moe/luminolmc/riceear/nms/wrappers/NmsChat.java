package moe.luminolmc.riceear.nms.wrappers;

import moe.luminolmc.riceear.nms.NmsReflection;
import moe.luminolmc.riceear.nms.NmsVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class NmsChat {

    private final Object handle;
    private final NmsVersion version;

    public NmsChat(@NotNull Object handle, @NotNull NmsVersion version) {
        this.handle = handle;
        this.version = version;
    }

    @NotNull
    public Object getHandle() {
        return handle;
    }

    @Nullable
    public String toPlainText() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getString");
            if (method != null) {
                return NmsReflection.invokeMethod(method, handle);
            }
        }
        Method method = NmsReflection.getMethodRecursive(handle.getClass(), "getText");
        if (method != null) {
            return NmsReflection.invokeMethod(method, handle);
        }
        return null;
    }

    @Nullable
    public String toLegacyText() {
        if (version.isAtLeast(NmsVersion.v1_17_R1)) {
            try {
                Class<?> craftChatMessage = NmsReflection.getClass("org.bukkit.craftbukkit." + version.getPackageVersion() + ".util.CraftChatMessage");
                if (craftChatMessage != null) {
                    Method toLegacy = NmsReflection.getMethod(craftChatMessage, "fromComponent", handle.getClass());
                    if (toLegacy != null) {
                        return NmsReflection.invokeMethod(toLegacy, null, handle);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return toPlainText();
    }

    @Nullable
    public static NmsChat fromText(@NotNull NmsVersion version, @NotNull String text) {
        try {
            if (version.isAtLeast(NmsVersion.v1_17_R1)) {
                Class<?> componentClass = NmsReflection.getClass("net.minecraft.network.chat.Component");
                if (componentClass != null) {
                    Method literalMethod = NmsReflection.getMethod(componentClass, "literal", String.class);
                    if (literalMethod != null) {
                        Object component = NmsReflection.invokeMethod(literalMethod, null, text);
                        if (component != null) {
                            return new NmsChat(component, version);
                        }
                    }
                }
            }
            Class<?> chatSerializer = NmsReflection.getNmsClass(version, "ChatSerializer");
            if (chatSerializer == null) {
                chatSerializer = NmsReflection.getNmsClass(version, "IChatBaseComponent$ChatSerializer");
            }
            if (chatSerializer != null) {
                Method aMethod = NmsReflection.getMethod(chatSerializer, "a", String.class);
                if (aMethod != null) {
                    Object component = NmsReflection.invokeMethod(aMethod, null, "{\"text\":\"" + escapeJson(text) + "\"}");
                    if (component != null) {
                        return new NmsChat(component, version);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String escapeJson(@NotNull String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}