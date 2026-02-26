
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import java.util.regex.*;

public class ModerationManager {

    public enum State { IDLE, WAIT_FIND, WAIT_PLAYTIME }

    private static State state = State.IDLE;
    private static String target;

    private static final Pattern FIND =
            Pattern.compile("находится на сервере (.+)");
    private static final Pattern ACTIVITY =
            Pattern.compile("(\\d+)ч.*, (\\d+)м.*, (\\d+)с");

    public static void start(String nick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        target = nick;
        state = State.WAIT_FIND;

        mc.player.networkHandler.sendChatCommand("hm spy " + nick);
        mc.player.networkHandler.sendChatCommand("find " + nick);
    }

    public static void handle(String msg) {
        if (state == State.IDLE) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (state == State.WAIT_FIND && msg.contains("находится на сервере")) {

            Matcher m = FIND.matcher(msg);
            if (!m.find()) return;

            String server = m.group(1);
            String moderator = mc.player.getName().getString();

            if (!moderator.equalsIgnoreCase(target)) {
                if (server.startsWith("lanarchy"))
                    mc.player.networkHandler.sendChatCommand("ln " + server.replace("lanarchy", ""));
                if (server.startsWith("l2anarchy"))
                    mc.player.networkHandler.sendChatCommand("ln120 " + server.replace("l2anarchy", ""));
                if (server.startsWith("anarchy"))
                    mc.player.networkHandler.sendChatCommand("cn " + server.replace("anarchy", ""));
            }

            state = State.WAIT_PLAYTIME;
            mc.player.networkHandler.sendChatCommand("playtime " + target);
        }

        if (state == State.WAIT_PLAYTIME && msg.contains("Последняя активность")) {

            Matcher m = ACTIVITY.matcher(msg);
            if (!m.find()) return;

            int sec =
                    Integer.parseInt(m.group(1)) * 3600 +
                    Integer.parseInt(m.group(2)) * 60 +
                    Integer.parseInt(m.group(3));

            if (sec <= PlayTimeConfig.get().activitySeconds)
                mc.player.networkHandler.sendChatCommand("hm spyfrz");
            else
                mc.player.sendMessage(Text.literal("Игрок не активен проследите сами"), false);

            state = State.IDLE;
        }
    }
}
