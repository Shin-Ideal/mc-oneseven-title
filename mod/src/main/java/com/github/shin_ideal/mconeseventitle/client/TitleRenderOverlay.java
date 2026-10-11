package com.github.shin_ideal.mconeseventitle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TitleRenderOverlay {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }

        ScaledResolution res = event.resolution;
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();
        FontRenderer fontRenderer = mc.fontRenderer;

        // Render Title and Subtitle
        float titleAlpha = TitleManager.getTitleAlpha(event.partialTicks);
        int titleAlphaInt = (int) (titleAlpha * 255.0F);
        if (titleAlphaInt > 255) {
            titleAlphaInt = 255;
        }

        if (titleAlphaInt > 8) {
            String title = TitleManager.getTitleText();
            String subtitle = TitleManager.getSubtitleText();

            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GL11.glDisable(GL11.GL_ALPHA_TEST);

            // Render Title (Scale x4)
            if (title != null && !title.isEmpty()) {
                GL11.glPushMatrix();
                GL11.glTranslatef(width / 2.0F, height / 2.0F - 20.0F, 0.0F);
                GL11.glScalef(4.0F, 4.0F, 4.0F);
                int strWidth = fontRenderer.getStringWidth(title);
                fontRenderer.drawString(title, -strWidth / 2, -4, (titleAlphaInt << 24) | 0x00FFFFFF, true);
                GL11.glPopMatrix();
            }

            // Render Subtitle (Scale x2)
            if (subtitle != null && !subtitle.isEmpty()) {
                GL11.glPushMatrix();
                GL11.glTranslatef(width / 2.0F, height / 2.0F + 8.0F, 0.0F);
                GL11.glScalef(2.0F, 2.0F, 2.0F);
                int strWidthSub = fontRenderer.getStringWidth(subtitle);
                fontRenderer.drawString(subtitle, -strWidthSub / 2, -4, (titleAlphaInt << 24) | 0x00FFFFFF, true);
                GL11.glPopMatrix();
            }

            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glPopMatrix();
        }

        // Render Action Bar
        float abAlpha = TitleManager.getActionbarAlpha(event.partialTicks);
        int abAlphaInt = (int) (abAlpha * 255.0F);
        if (abAlphaInt > 255) {
            abAlphaInt = 255;
        }

        if (abAlphaInt > 8) {
            String actionbar = TitleManager.getActionbarText();
            if (actionbar != null && !actionbar.isEmpty()) {
                GL11.glPushMatrix();
                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                GL11.glDisable(GL11.GL_ALPHA_TEST);

                int y = height - 68;
                if (mc.playerController != null && !mc.playerController.shouldDrawHUD()) {
                    y += 14;
                }
                int strWidth = fontRenderer.getStringWidth(actionbar);
                int x = (width - strWidth) / 2;
                fontRenderer.drawString(actionbar, x, y, (abAlphaInt << 24) | 0x00FFFFFF, true);

                GL11.glDisable(GL11.GL_BLEND);
                GL11.glEnable(GL11.GL_ALPHA_TEST);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glPopMatrix();
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (mc.isGamePaused()) {
            return;
        }
        TitleManager.clientTick();
    }

    @SubscribeEvent
    public void onDisconnect(ClientDisconnectionFromServerEvent event) {
        TitleManager.reset();
    }

    @SubscribeEvent
    public void onConnect(ClientConnectedToServerEvent event) {
        TitleManager.reset();
    }
}
