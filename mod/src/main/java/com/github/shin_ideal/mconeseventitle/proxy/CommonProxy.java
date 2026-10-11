package com.github.shin_ideal.mconeseventitle.proxy;

import com.github.shin_ideal.mconeseventitle.command.CommandTitle;
import com.github.shin_ideal.mconeseventitle.network.TitleNetwork;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        TitleNetwork.init();
    }

    public void init(FMLInitializationEvent event) {
        // Overridden by ClientProxy
    }

    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandTitle());
    }
}
