package com.github.shin_ideal.mconeseventitle.network;

import com.github.shin_ideal.mconeseventitle.client.TitleManager;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class PacketTitleHandler implements IMessageHandler<PacketTitle, IMessage> {

    @Override
    public IMessage onMessage(PacketTitle message, MessageContext ctx) {
        if (message == null || message.getAction() == null) {
            return null;
        }

        switch (message.getAction()) {
            case TITLE:
                TitleManager.setTitle(message.getText());
                break;
            case SUBTITLE:
                TitleManager.setSubtitle(message.getText());
                break;
            case ACTIONBAR:
                TitleManager.setActionbar(message.getText());
                break;
            case TIMES:
                TitleManager.setTimes(message.getFadeIn(), message.getStay(), message.getFadeOut());
                break;
            case CLEAR:
                TitleManager.clear();
                break;
            case RESET:
                TitleManager.reset();
                break;
            default:
                break;
        }

        return null;
    }
}
