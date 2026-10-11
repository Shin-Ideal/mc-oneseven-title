package com.github.shin_ideal.mconeseventitle;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class TitleBackportPlugin extends JavaPlugin implements Listener, PluginMessageListener {

    @Override
    public void onEnable() {
        TitleBackportAPI.init(this);

        // Register Plugin Messaging Channels (Legacy for 1.7-1.12, Namespaced for 1.13+)
        try {
            getServer().getMessenger().registerOutgoingPluginChannel(this, TitlePacketSerializer.CHANNEL_NAME);
            getServer().getMessenger().registerIncomingPluginChannel(this, TitlePacketSerializer.CHANNEL_NAME, this);
        } catch (Throwable ignored) {
        }
        try {
            getServer().getMessenger().registerOutgoingPluginChannel(this, TitlePacketSerializer.MODERN_CHANNEL_NAME);
            getServer().getMessenger().registerIncomingPluginChannel(this, TitlePacketSerializer.MODERN_CHANNEL_NAME, this);
        } catch (Throwable ignored) {
        }

        // Register event listener
        getServer().getPluginManager().registerEvents(this, this);

        // Inject interceptor for currently online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            PacketInterceptor.injectPlayer(p);
        }

        getLogger().info("TitleBackport Plugin enabled! Bridge active for 1.7.10 clients.");
    }

    @Override
    public void onDisable() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PacketInterceptor.removePlayer(p);
        }
        getLogger().info("TitleBackport Plugin disabled.");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Run slightly delayed to ensure Netty channel and pipeline are fully established
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (event.getPlayer().isOnline()) {
                PacketInterceptor.injectPlayer(event.getPlayer());
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        PacketInterceptor.removePlayer(event.getPlayer());
        TitleBackportAPI.removePlayer(event.getPlayer());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // Can be used for client-to-server acknowledgements if needed in the future
    }
}
