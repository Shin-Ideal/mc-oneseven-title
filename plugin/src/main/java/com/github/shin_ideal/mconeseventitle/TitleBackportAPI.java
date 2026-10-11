package com.github.shin_ideal.mconeseventitle;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TitleBackportAPI {

    private static Plugin plugin;
    private static final Set<UUID> COMPATIBLE_CACHE = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void init(Plugin pl) {
        plugin = pl;
    }

    public static void removePlayer(Player player) {
        if (player != null) {
            COMPATIBLE_CACHE.remove(player.getUniqueId());
        }
    }

    public static boolean isCompatiblePlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (COMPATIBLE_CACHE.contains(uuid)) {
            return true;
        }

        // 1. Check if client registered either legacy or modern channel
        try {
            Set<String> channels = player.getListeningPluginChannels();
            if (channels != null && (channels.contains(TitlePacketSerializer.CHANNEL_NAME)
                    || channels.contains(TitlePacketSerializer.MODERN_CHANNEL_NAME))) {
                COMPATIBLE_CACHE.add(uuid);
                return true;
            }
        } catch (Throwable ignored) {
        }

        // 2. Check protocol version via ViaVersion if present on this backend server
        try {
            Class<?> viaClass = Class.forName("com.viaversion.viaversion.api.Via");
            Object api = viaClass.getMethod("getAPI").invoke(null);
            int version = (int) api.getClass().getMethod("getPlayerVersion", UUID.class).invoke(api, uuid);
            // Protocol 5 = 1.7.6-1.7.10, 4 = 1.7.2-1.7.5
            if (version <= 5) {
                COMPATIBLE_CACHE.add(uuid);
                return true;
            } else {
                return false;
            }
        } catch (ClassNotFoundException e) {
            // ViaVersion is not on backend (e.g. Velocity proxy environment).
            // Default to true so custom payload is dispatched to proxy/client
            COMPATIBLE_CACHE.add(uuid);
            return true;
        } catch (Throwable ignored) {
        }

        // Fallback: Default to true in proxy environments
        COMPATIBLE_CACHE.add(uuid);
        return true;
    }

    public static void sendTitle(Player player, String jsonOrText) {
        sendBytes(player, TitlePacketSerializer.buildTitle(jsonOrText));
    }

    public static void sendSubtitle(Player player, String jsonOrText) {
        sendBytes(player, TitlePacketSerializer.buildSubtitle(jsonOrText));
    }

    public static void sendActionBar(Player player, String jsonOrText) {
        sendBytes(player, TitlePacketSerializer.buildActionBar(jsonOrText));
    }

    public static void setTimes(Player player, int fadeIn, int stay, int fadeOut) {
        sendBytes(player, TitlePacketSerializer.buildTimes(fadeIn, stay, fadeOut));
    }

    public static void clear(Player player) {
        sendBytes(player, TitlePacketSerializer.buildClear());
    }

    public static void reset(Player player) {
        sendBytes(player, TitlePacketSerializer.buildReset());
    }

    private static void sendBytes(Player player, byte[] data) {
        if (plugin != null && player != null && player.isOnline() && isCompatiblePlayer(player)) {
            Set<String> outgoing = Bukkit.getMessenger().getOutgoingChannels(plugin);
            Set<String> listening = null;
            try {
                listening = player.getListeningPluginChannels();
            } catch (Throwable ignored) {
            }

            boolean sent = false;

            // Modern namespaced channel (1.13+)
            if (outgoing.contains(TitlePacketSerializer.MODERN_CHANNEL_NAME)) {
                try {
                    player.sendPluginMessage(plugin, TitlePacketSerializer.MODERN_CHANNEL_NAME, data);
                    sent = true;
                } catch (Throwable ignored) {
                }
            }

            // Legacy channel (1.8-1.12)
            if (outgoing.contains(TitlePacketSerializer.CHANNEL_NAME)) {
                try {
                    player.sendPluginMessage(plugin, TitlePacketSerializer.CHANNEL_NAME, data);
                    sent = true;
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
