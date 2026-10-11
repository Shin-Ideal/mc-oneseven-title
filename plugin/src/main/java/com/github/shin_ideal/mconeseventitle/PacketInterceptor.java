package com.github.shin_ideal.mconeseventitle;

import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PacketInterceptor {

    private static final String HANDLER_NAME = "titlebackport_interceptor";
    private static final Pattern CLEAN_PATTERN = Pattern.compile("text='([^']*)'|LiteralContents\\[text=([^,\\]]*)\\]");

    public static void injectPlayer(Player player) {
        Channel channel = getChannel(player);
        if (channel == null) {
            return;
        }

        ChannelPipeline pipeline = channel.pipeline();
        if (pipeline.get(HANDLER_NAME) != null) {
            pipeline.remove(HANDLER_NAME);
        }

        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                try {
                    handleOutgoingPacket(player, msg);
                } catch (Throwable t) {
                    // Suppress error to avoid disrupting packet flow
                }
                super.write(ctx, msg, promise);
            }
        };

        if (pipeline.get("encoder") != null) {
            pipeline.addAfter("encoder", HANDLER_NAME, handler);
        } else if (pipeline.get("packet_handler") != null) {
            pipeline.addBefore("packet_handler", HANDLER_NAME, handler);
        } else {
            pipeline.addLast(HANDLER_NAME, handler);
        }
    }

    public static void removePlayer(Player player) {
        Channel channel = getChannel(player);
        if (channel == null) {
            return;
        }
        try {
            ChannelPipeline pipeline = channel.pipeline();
            if (pipeline.get(HANDLER_NAME) != null) {
                pipeline.remove(HANDLER_NAME);
            }
        } catch (Throwable ignored) {
        }
    }

    private static void handleOutgoingPacket(Player player, Object msg) {
        if (msg == null) {
            return;
        }

        String className = msg.getClass().getSimpleName();

        // 1.19.4+ ClientboundBundlePacket
        if (className.contains("BundlePacket")) {
            Iterable<?> subPackets = null;
            for (Method m : msg.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && Iterable.class.isAssignableFrom(m.getReturnType())) {
                    try {
                        subPackets = (Iterable<?>) m.invoke(msg);
                        break;
                    } catch (Throwable ignored) {
                    }
                }
            }
            if (subPackets != null) {
                for (Object sub : subPackets) {
                    handleOutgoingPacket(player, sub);
                }
            }
            return;
        }

        // Fast check: If it's not a title or chat packet, skip immediately without player lookup
        boolean isTitlePacket = className.contains("Title")
                || className.contains("SetTitleText")
                || className.contains("SetSubtitleText")
                || className.contains("SetTitlesAnimation")
                || className.contains("ClearTitles")
                || className.contains("PacketPlayOutChat")
                || className.contains("SystemChat")
                || className.contains("SetActionBarText");

        if (!isTitlePacket) {
            return;
        }

        // Check player compatibility only when a title-related packet is detected
        if (!TitleBackportAPI.isCompatiblePlayer(player)) {
            return;
        }

        // 1.8 - 1.16 PacketPlayOutTitle
        if (className.contains("PacketPlayOutTitle")) {
            handlePacketPlayOutTitle(player, msg);
            return;
        }

        // 1.17+ Modern Title packets
        if (className.contains("SetTitleText")) {
            Object component = getComponentFromPacket(msg);
            String text = extractText(component);
            TitleBackportAPI.sendTitle(player, text);
            return;
        }

        if (className.contains("SetSubtitleText")) {
            Object component = getComponentFromPacket(msg);
            String text = extractText(component);
            TitleBackportAPI.sendSubtitle(player, text);
            return;
        }

        if (className.contains("SetTitlesAnimation")) {
            Integer fadeIn = (Integer) getFieldValueOrMethod(msg, "fadeIn", "b", "c");
            Integer stay = (Integer) getFieldValueOrMethod(msg, "stay", "c", "d");
            Integer fadeOut = (Integer) getFieldValueOrMethod(msg, "fadeOut", "d", "e");
            if (fadeIn != null && stay != null && fadeOut != null) {
                TitleBackportAPI.setTimes(player, fadeIn, stay, fadeOut);
            }
            return;
        }

        if (className.contains("ClearTitles")) {
            Boolean resetTimes = (Boolean) getFieldValueOrMethod(msg, "resetTimes", "b");
            if (Boolean.TRUE.equals(resetTimes)) {
                TitleBackportAPI.reset(player);
            } else {
                TitleBackportAPI.clear(player);
            }
            return;
        }

        // 1.8 - 1.18 PacketPlayOutChat (with position 2 / GAME_INFO)
        if (className.contains("PacketPlayOutChat")) {
            handlePacketPlayOutChat(player, msg);
            return;
        }

        // 1.19+ System chat with overlay (action bar)
        if (className.contains("SystemChat")) {
            Boolean overlay = (Boolean) getFieldValueOrMethod(msg, "overlay", "b", "c");
            if (Boolean.TRUE.equals(overlay)) {
                Object component = getComponentFromPacket(msg);
                String text = extractText(component);
                TitleBackportAPI.sendActionBar(player, text);
            }
            return;
        }

        // 1.19+ ClientboundSetActionBarTextPacket
        if (className.contains("SetActionBarText")) {
            Object component = getComponentFromPacket(msg);
            String text = extractText(component);
            TitleBackportAPI.sendActionBar(player, text);
        }
    }

    private static void handlePacketPlayOutTitle(Player player, Object packet) {
        Object actionEnum = getFieldValueOrMethod(packet, "a", "action");
        String actionName = actionEnum != null ? actionEnum.toString() : "";

        if ("TITLE".equalsIgnoreCase(actionName)) {
            Object component = getFieldValueOrMethod(packet, "b", "text", "message");
            String text = extractText(component);
            TitleBackportAPI.sendTitle(player, text);
        } else if ("SUBTITLE".equalsIgnoreCase(actionName)) {
            Object component = getFieldValueOrMethod(packet, "b", "text", "message");
            String text = extractText(component);
            TitleBackportAPI.sendSubtitle(player, text);
        } else if ("ACTIONBAR".equalsIgnoreCase(actionName)) {
            Object component = getFieldValueOrMethod(packet, "b", "text", "message");
            String text = extractText(component);
            TitleBackportAPI.sendActionBar(player, text);
        } else if ("TIMES".equalsIgnoreCase(actionName)) {
            Integer fadeIn = (Integer) getFieldValueOrMethod(packet, "c", "fadeIn");
            Integer stay = (Integer) getFieldValueOrMethod(packet, "d", "stay");
            Integer fadeOut = (Integer) getFieldValueOrMethod(packet, "e", "fadeOut");
            if (fadeIn != null && stay != null && fadeOut != null) {
                TitleBackportAPI.setTimes(player, fadeIn, stay, fadeOut);
            }
        } else if ("CLEAR".equalsIgnoreCase(actionName)) {
            TitleBackportAPI.clear(player);
        } else if ("RESET".equalsIgnoreCase(actionName)) {
            TitleBackportAPI.reset(player);
        }
    }

    private static void handlePacketPlayOutChat(Player player, Object packet) {
        boolean isActionBar = false;

        Object typeObj = getFieldValueOrMethod(packet, "b", "type", "position");
        if (typeObj instanceof Number) {
            if (((Number) typeObj).byteValue() == 2) {
                isActionBar = true;
            }
        } else if (typeObj != null) {
            if ("GAME_INFO".equalsIgnoreCase(typeObj.toString())) {
                isActionBar = true;
            }
        }

        if (isActionBar) {
            Object component = getFieldValueOrMethod(packet, "a", "message", "text");
            String text = extractText(component);
            TitleBackportAPI.sendActionBar(player, text);
        }
    }

    private static Object getComponentFromPacket(Object packet) {
        if (packet == null) {
            return null;
        }
        // Try method call (Record accessor like text(), getComponent())
        for (Method m : packet.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && (m.getName().equals("text") || m.getName().equals("message")
                    || m.getReturnType().getSimpleName().contains("Component"))) {
                try {
                    return m.invoke(packet);
                } catch (Throwable ignored) {
                }
            }
        }
        return getFieldValueByType(packet, "Component");
    }

    private static String extractText(Object component) {
        if (component == null) {
            return "";
        }
        if (component instanceof String) {
            return (String) component;
        }

        // 1. Try Kyori Adventure Component (Paper 1.16+)
        try {
            Class<?> compClass = Class.forName("net.kyori.adventure.text.Component");
            if (compClass.isAssignableFrom(component.getClass())) {
                Class<?> gsonClass = Class.forName("net.kyori.adventure.text.serializer.gson.GsonComponentSerializer");
                Method gsonMethod = gsonClass.getMethod("gson");
                Object gsonInst = gsonMethod.invoke(null);
                Method serializeMethod = gsonInst.getClass().getMethod("serialize", compClass);
                String json = (String) serializeMethod.invoke(gsonInst, component);
                if (json != null && !json.isEmpty()) {
                    return json;
                }
            }
        } catch (Throwable ignored) {
        }

        // 2. Try CraftChatMessage.toJSON(component) or CraftChatMessage.fromComponent(component)
        try {
            String cbPackage = Bukkit.getServer().getClass().getPackage().getName();
            String[] candidateClasses = new String[]{
                    cbPackage + ".util.CraftChatMessage",
                    "org.bukkit.craftbukkit.util.CraftChatMessage"
            };
            for (String cn : candidateClasses) {
                try {
                    Class<?> ccm = Class.forName(cn);
                    for (Method m : ccm.getMethods()) {
                        if (m.getName().equals("toJSON") && m.getParameterCount() == 1) {
                            Object json = m.invoke(null, component);
                            if (json instanceof String && !((String) json).isEmpty()) {
                                return (String) json;
                            }
                        }
                    }
                    for (Method m : ccm.getMethods()) {
                        if (m.getName().equals("fromComponent") && m.getParameterCount() == 1) {
                            Object text = m.invoke(null, component);
                            if (text instanceof String && !((String) text).isEmpty()) {
                                return (String) text;
                            }
                        }
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }
        } catch (Throwable ignored) {
        }

        // 3. Try ChatSerializer.a(component) or Serializer.toJson(component)
        try {
            Class<?> compClass = component.getClass();
            List<Class<?>> classesToScan = new ArrayList<>();
            classesToScan.add(compClass);
            classesToScan.addAll(Arrays.asList(compClass.getInterfaces()));
            if (compClass.getSuperclass() != null) {
                classesToScan.add(compClass.getSuperclass());
            }

            for (Class<?> c : classesToScan) {
                for (Class<?> inner : c.getDeclaredClasses()) {
                    if (inner.getSimpleName().contains("Serializer")) {
                        for (Method m : inner.getMethods()) {
                            if (m.getParameterCount() == 1 && m.getReturnType() == String.class
                                    && Modifier.isStatic(m.getModifiers())) {
                                Object json = m.invoke(null, component);
                                if (json instanceof String && !((String) json).isEmpty()) {
                                    return (String) json;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        // 4. Try NMS methods: c() (formatted in 1.8), e() (unformatted in 1.8), getString(), getText()
        for (String methodName : new String[]{"getString", "getText", "c", "e"}) {
            try {
                Method m = component.getClass().getMethod(methodName);
                if (m.getParameterCount() == 0 && m.getReturnType() == String.class) {
                    Object res = m.invoke(component);
                    if (res instanceof String && !((String) res).isEmpty()) {
                        return (String) res;
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        // 5. Fallback: Parse out text from component.toString()
        String raw = component.toString();
        Matcher matcher = CLEAN_PATTERN.matcher(raw);
        if (matcher.find()) {
            String text = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (text != null) {
                return text;
            }
        }

        return raw;
    }

    private static Object getFieldValueOrMethod(Object obj, String... names) {
        if (obj == null) {
            return null;
        }
        // Try getters or record accessors first
        for (String name : names) {
            try {
                Method m = obj.getClass().getMethod(name);
                if (m.getParameterCount() == 0) {
                    return m.invoke(obj);
                }
            } catch (Throwable ignored) {
            }
        }
        // Then try fields
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (String name : names) {
                try {
                    Field f = clazz.getDeclaredField(name);
                    f.setAccessible(true);
                    return f.get(obj);
                } catch (Throwable ignored) {
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private static Object getFieldValueByType(Object obj, String typeSimpleName) {
        if (obj == null) {
            return null;
        }
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getType().getSimpleName().contains(typeSimpleName)) {
                    try {
                        f.setAccessible(true);
                        return f.get(obj);
                    } catch (Throwable ignored) {
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static Channel getChannel(Player player) {
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object entityPlayer = getHandle.invoke(player);
            return searchChannel(entityPlayer, 0);
        } catch (Throwable t) {
            // Channel lookup failed
        }
        return null;
    }

    private static Channel searchChannel(Object obj, int depth) {
        if (obj == null || depth > 3) {
            return null;
        }
        Class<?> clazz = obj.getClass();
        while (clazz != null && clazz != Object.class && !clazz.getName().startsWith("java.")) {
            for (Field f : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) {
                    continue;
                }
                try {
                    f.setAccessible(true);
                    Object val = f.get(obj);
                    if (val == null) {
                        continue;
                    }
                    if (val instanceof Channel) {
                        return (Channel) val;
                    }
                    if (depth < 2) {
                        Channel found = searchChannel(val, depth + 1);
                        if (found != null) {
                            return found;
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }
}
