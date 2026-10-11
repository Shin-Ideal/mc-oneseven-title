package com.github.shin_ideal.mconeseventitle.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

import com.github.shin_ideal.mconeseventitle.network.PacketTitle;
import com.github.shin_ideal.mconeseventitle.network.TitleNetwork;

public class CommandTitle extends CommandBase {

    @Override
    public String getCommandName() {
        return "title";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/title <player> <title|subtitle|actionbar|clear|reset|times> ...";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            throw new WrongUsageException(getCommandUsage(sender));
        }

        EntityPlayerMP[] targets = PlayerSelector.matchPlayers(sender, args[0]);
        if (targets == null || targets.length == 0) {
            EntityPlayerMP singlePlayer = getPlayer(sender, args[0]);
            targets = new EntityPlayerMP[] { singlePlayer };
        }

        String subCommand = args[1].toLowerCase();

        switch (subCommand) {
            case "clear": {
                PacketTitle pkt = PacketTitle.clear();
                for (EntityPlayerMP player : targets) {
                    TitleNetwork.sendTo(pkt, player);
                }
                func_152373_a(sender, this, "Cleared title for %s player(s)", targets.length);
                break;
            }
            case "reset": {
                PacketTitle pkt = PacketTitle.reset();
                for (EntityPlayerMP player : targets) {
                    TitleNetwork.sendTo(pkt, player);
                }
                func_152373_a(sender, this, "Reset title settings for %s player(s)", targets.length);
                break;
            }
            case "times": {
                if (args.length < 5) {
                    throw new WrongUsageException("/title <player> times <fadeIn> <stay> <fadeOut>");
                }
                int fadeIn = parseInt(sender, args[2]);
                int stay = parseInt(sender, args[3]);
                int fadeOut = parseInt(sender, args[4]);
                PacketTitle pkt = PacketTitle.times(fadeIn, stay, fadeOut);
                for (EntityPlayerMP player : targets) {
                    TitleNetwork.sendTo(pkt, player);
                }
                func_152373_a(sender, this, "Set title display times to %s, %s, %s ticks", fadeIn, stay, fadeOut);
                break;
            }
            case "title":
            case "subtitle":
            case "actionbar": {
                if (args.length < 3) {
                    throw new WrongUsageException("/title <player> " + subCommand + " <raw json or text>");
                }
                String messageJson = buildJsonMessage(args, 2);
                PacketTitle pkt;
                if ("title".equals(subCommand)) {
                    pkt = PacketTitle.title(messageJson);
                } else if ("subtitle".equals(subCommand)) {
                    pkt = PacketTitle.subtitle(messageJson);
                } else {
                    pkt = PacketTitle.actionbar(messageJson);
                }

                for (EntityPlayerMP player : targets) {
                    TitleNetwork.sendTo(pkt, player);
                }
                func_152373_a(sender, this, "Sent %s to %s player(s)", subCommand, targets.length);
                break;
            }
            default:
                throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    private String buildJsonMessage(String[] args, int startIndex) {
        StringBuilder sb = new StringBuilder();
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) {
                sb.append(" ");
            }
            sb.append(args[i]);
        }
        String raw = sb.toString();

        // 1. If valid JSON object or array, parse directly
        if (raw.startsWith("{") || raw.startsWith("[")) {
            try {
                IChatComponent component = IChatComponent.Serializer.func_150699_a(raw);
                if (component != null) {
                    return IChatComponent.Serializer.func_150696_a(component);
                }
            } catch (Exception ignored) {
                // Not valid JSON, fall back to plain text
            }
        }

        // 2. If wrapped in double quotes (e.g. "text"), strip the outer quotes
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
            try {
                IChatComponent component = IChatComponent.Serializer.func_150699_a(raw);
                if (component != null) {
                    return IChatComponent.Serializer.func_150696_a(component);
                }
            } catch (Exception ignored) {}
            raw = raw.substring(1, raw.length() - 1);
        }

        // 3. Support standard & color codes if not JSON
        String formatted = raw.replace('&', '\u00a7');
        IChatComponent comp = new ChatComponentText(formatted);
        return IChatComponent.Serializer.func_150696_a(comp);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(
                args,
                MinecraftServer.getServer()
                    .getAllUsernames());
        }
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, "title", "subtitle", "actionbar", "clear", "reset", "times");
        }
        return null;
    }
}
