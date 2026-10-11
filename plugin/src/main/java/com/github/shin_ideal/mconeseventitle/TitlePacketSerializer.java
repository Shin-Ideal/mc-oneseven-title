package com.github.shin_ideal.mconeseventitle;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TitlePacketSerializer {

    public static final String CHANNEL_NAME = "TitleBackport";
    public static final String MODERN_CHANNEL_NAME = "titlebackport:main";

    public static byte[] buildTitle(String jsonOrText) {
        return buildStringPacket(0, jsonOrText);
    }

    public static byte[] buildSubtitle(String jsonOrText) {
        return buildStringPacket(1, jsonOrText);
    }

    public static byte[] buildActionBar(String jsonOrText) {
        return buildStringPacket(2, jsonOrText);
    }

    public static byte[] buildTimes(int fadeIn, int stay, int fadeOut) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            bos.write(0); // FML discriminator
            bos.write(3); // TIMES
            dos.writeInt(fadeIn);
            dos.writeInt(stay);
            dos.writeInt(fadeOut);
        } catch (IOException ignored) {
        }
        return bos.toByteArray();
    }

    public static byte[] buildClear() {
        return new byte[]{0, 4};
    }

    public static byte[] buildReset() {
        return new byte[]{0, 5};
    }

    private static byte[] buildStringPacket(int actionId, String text) {
        if (text == null) {
            text = "";
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            bos.write(0); // FML discriminator
            bos.write(actionId); // TITLE(0), SUBTITLE(1), ACTIONBAR(2)
            writeUTF8String(bos, text);
        } catch (IOException ignored) {
        }
        return bos.toByteArray();
    }

    private static void writeUTF8String(ByteArrayOutputStream out, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.write(bytes);
    }

    private static void writeVarInt(ByteArrayOutputStream out, int value) {
        while ((value & -128) != 0) {
            out.write(value & 127 | 128);
            value >>>= 7;
        }
        out.write(value);
    }
}
