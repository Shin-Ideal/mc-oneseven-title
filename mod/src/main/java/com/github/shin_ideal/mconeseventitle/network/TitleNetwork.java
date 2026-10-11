package com.github.shin_ideal.mconeseventitle.network;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class TitleNetwork {

    public static final String CHANNEL_NAME = "TitleBackport";
    public static final String MODERN_CHANNEL_NAME = "titlebackport:main";
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_NAME);
    public static final SimpleNetworkWrapper MODERN_INSTANCE = NetworkRegistry.INSTANCE
        .newSimpleChannel(MODERN_CHANNEL_NAME);

    public static void init() {
        INSTANCE.registerMessage(PacketTitleHandler.class, PacketTitle.class, 0, Side.CLIENT);
        MODERN_INSTANCE.registerMessage(PacketTitleHandler.class, PacketTitle.class, 0, Side.CLIENT);
    }

    public static void sendTo(PacketTitle message, EntityPlayerMP player) {
        INSTANCE.sendTo(message, player);
    }

    public static void sendToAll(PacketTitle message) {
        INSTANCE.sendToAll(message);
    }
}
