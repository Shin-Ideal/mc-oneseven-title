package com.github.shin_ideal.mconeseventitle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.shin_ideal.mconeseventitle.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(
    modid = TitleBackportMod.MODID,
    name = TitleBackportMod.MODNAME,
    version = Tags.VERSION,
    acceptedMinecraftVersions = "[1.7.10]")
public class TitleBackportMod {

    public static final String MODID = "titlebackport";
    public static final String MODNAME = "Title Backport";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.Instance(MODID)
    public static TitleBackportMod instance;

    @SidedProxy(
        clientSide = "com.github.shin_ideal.mconeseventitle.proxy.ClientProxy",
        serverSide = "com.github.shin_ideal.mconeseventitle.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOG.info("Initializing {} version {}", MODNAME, Tags.VERSION);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
