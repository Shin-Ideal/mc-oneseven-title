package com.github.shin_ideal.mconeseventitle.proxy;

import net.minecraftforge.common.MinecraftForge;

import com.github.shin_ideal.mconeseventitle.client.TitleRenderOverlay;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        TitleRenderOverlay overlay = new TitleRenderOverlay();
        // RenderGameOverlayEvent is on MinecraftForge.EVENT_BUS
        MinecraftForge.EVENT_BUS.register(overlay);
        // TickEvent.ClientTickEvent is on FMLCommonHandler.instance().bus() in 1.7.10
        FMLCommonHandler.instance()
            .bus()
            .register(overlay);
    }
}
