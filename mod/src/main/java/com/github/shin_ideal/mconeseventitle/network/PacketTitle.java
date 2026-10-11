package com.github.shin_ideal.mconeseventitle.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class PacketTitle implements IMessage {

    private TitleAction action;
    private String text;
    private int fadeIn;
    private int stay;
    private int fadeOut;

    public PacketTitle() {
        this.action = TitleAction.CLEAR;
        this.text = "";
    }

    public static PacketTitle title(String text) {
        PacketTitle pkt = new PacketTitle();
        pkt.action = TitleAction.TITLE;
        pkt.text = text != null ? text : "";
        return pkt;
    }

    public static PacketTitle subtitle(String text) {
        PacketTitle pkt = new PacketTitle();
        pkt.action = TitleAction.SUBTITLE;
        pkt.text = text != null ? text : "";
        return pkt;
    }

    public static PacketTitle actionbar(String text) {
        PacketTitle pkt = new PacketTitle();
        pkt.action = TitleAction.ACTIONBAR;
        pkt.text = text != null ? text : "";
        return pkt;
    }

    public static PacketTitle times(int fadeIn, int stay, int fadeOut) {
        PacketTitle pkt = new PacketTitle();
        pkt.action = TitleAction.TIMES;
        pkt.fadeIn = fadeIn;
        pkt.stay = stay;
        pkt.fadeOut = fadeOut;
        return pkt;
    }

    public static PacketTitle clear() {
        PacketTitle pkt = new PacketTitle();
        pkt.action = TitleAction.CLEAR;
        return pkt;
    }

    public static PacketTitle reset() {
        PacketTitle pkt = new PacketTitle();
        pkt.action = TitleAction.RESET;
        return pkt;
    }

    public TitleAction getAction() {
        return action;
    }

    public String getText() {
        return text;
    }

    public int getFadeIn() {
        return fadeIn;
    }

    public int getStay() {
        return stay;
    }

    public int getFadeOut() {
        return fadeOut;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int actionId = buf.readByte() & 0xFF;
        this.action = TitleAction.fromId(actionId);

        switch (action) {
            case TITLE:
            case SUBTITLE:
            case ACTIONBAR:
                this.text = ByteBufUtils.readUTF8String(buf);
                break;
            case TIMES:
                this.fadeIn = buf.readInt();
                this.stay = buf.readInt();
                this.fadeOut = buf.readInt();
                break;
            case CLEAR:
            case RESET:
            default:
                break;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(action.getId());

        switch (action) {
            case TITLE:
            case SUBTITLE:
            case ACTIONBAR:
                ByteBufUtils.writeUTF8String(buf, text != null ? text : "");
                break;
            case TIMES:
                buf.writeInt(fadeIn);
                buf.writeInt(stay);
                buf.writeInt(fadeOut);
                break;
            case CLEAR:
            case RESET:
            default:
                break;
        }
    }
}
