package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModerationManager {

    private static String targetNick;

    private static final Pattern FIND_PATTERN =
            Pattern.compile("находится на сервере (.+)");

    private static final Pattern ACTIVITY_PATTERN =
            Pattern.compile("(\\d+)ч.*, (\\d+)м.*, (\\d+)с");

    public static void start(String nick) {
        targetNick = nick;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        mc.player.networkHandler.sendChatCommand("hm spy " + nick);
        mc.player.networkHandler.sendChatCommand("find " + nick);
    }

    public static void handleChat(String msg) {

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // ================= FIND =================
        if (msg.contains("находится на сервере")) {

            Matcher m = FIND_PATTERN.matcher(msg);
            if (!m.find()) return;

            String server = m.group(1);
            String moderator = mc.player.getName().getString();

            if (!moderator.equalsIgnoreCase(targetNick)) {

                if (server.startsWith("lanarchy")) {
                    mc.player.networkHandler.sendChatCommand(
                            "ln " + server.replace("lanarchy", "")
                    );
                }

                if (server.startsWith("l2anarchy")) {
                    mc.player.networkHandler.sendChatCommand(
                            "ln120 " + server.replace("l2anarchy", "")
                    );
                }

                if (server.startsWith("anarchy")) {
                    mc.player.networkHandler.sendChatCommand(
                            "cn " + server.replace("anarchy", "")
                    );
                }
            }

            mc.player.networkHandler.sendChatCommand("playtime " + targetNick);
        }

        // ================= ACTIVITY =================
        if (msg.contains("Последняя активность")) {

            Matcher m = ACTIVITY_PATTERN.matcher(msg);
            if (!m.find()) return;

            int hours = Integer.parseInt(m.group(1));
            int minutes = Integer.parseInt(m.group(2));
            int seconds = Integer.parseInt(m.group(3));

            int totalSeconds = hours * 3600 + minutes * 60 + seconds;

            if (totalSeconds <= PlayTimeConfig.get().activitySeconds) {
                mc.player.networkHandler.sendChatCommand("hm spyfrz");
            } else {
                mc.player.sendMessage(
                        Text.literal("Игрок не активен проследите сами"),
                        false
                );
            }
        }
    }
}
