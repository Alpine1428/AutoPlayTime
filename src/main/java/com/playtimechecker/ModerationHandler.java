package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModerationHandler {

    public enum State {
        IDLE,
        SPY_SENT,
        FIND_SENT,
        SWITCHING_SERVER,
        PLAYTIME_SENT,
        DONE
    }

    private static State state = State.IDLE;
    private static String targetNick = null;
    private static String moderatorNick = null;
    private static int waitTicks = 0;
    private static boolean hidingBlock = false;
    private static boolean foundActivity = false;

    private static final Pattern FIND_PATTERN =
            Pattern.compile("\\u0418\\u0433\\u0440\\u043e\\u043a\\s+(\\S+)\\s+\\u043d\\u0430\\u0445\\u043e\\u0434\\u0438\\u0442\\u0441\\u044f \\u043d\\u0430 \\u0441\\u0435\\u0440\\u0432\\u0435\\u0440\\u0435\\s+(\\S+)");

    private static final Pattern ACTIVITY_PATTERN =
            Pattern.compile("\\u041f\\u043e\\u0441\\u043b\\u0435\\u0434\\u043d\\u044f\\u044f \\u0430\\u043a\\u0442\\u0438\\u0432\\u043d\\u043e\\u0441\\u0442\\u044c:\\s*(\\d+)\\u0447\\.?,?\\s*(\\d+)\\u043c\\.?,?\\s*(\\d+)\\u0441");

    private static final String SEPARATOR = "---------------------------------------------------";
    private static final String PLAYTIME_HEADER = "--PlayTimeAPI--";

    public static void startModeration(String nick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        targetNick = nick;
        moderatorNick = mc.player.getGameProfile().getName();
        state = State.SPY_SENT;
        hidingBlock = false;
        foundActivity = false;
        waitTicks = 0;

        CommandQueue.add("hm spy " + nick);
        CommandQueue.add("find " + nick);
    }

    public static void stop() {
        state = State.IDLE;
        targetNick = null;
        hidingBlock = false;
    }

    public static void tick() {
        if (state == State.IDLE || state == State.DONE) return;

        waitTicks++;

        if (waitTicks > 200) {
            state = State.IDLE;
            return;
        }
    }

    public static boolean handle(String msg) {
        if (state == State.IDLE) return false;

        if (state == State.SPY_SENT || state == State.FIND_SENT) {
            Matcher findM = FIND_PATTERN.matcher(msg);
            if (findM.find()) {
                state = State.FIND_SENT;
                String server = findM.group(2).toLowerCase();

                String switchCmd = getServerSwitchCommand(server);

                if (switchCmd != null && !targetNick.equalsIgnoreCase(moderatorNick)) {
                    CommandQueue.add(switchCmd);
                }

                CommandQueue.add("playtime " + targetNick);
                state = State.PLAYTIME_SENT;

                return true;
            }
        }

        if (state == State.PLAYTIME_SENT) {

            if (msg.contains(PLAYTIME_HEADER)) {
                hidingBlock = true;
                return true;
            }

            if (hidingBlock && msg.contains(SEPARATOR) && !msg.contains("PlayTimeAPI")) {
                hidingBlock = false;

                if (!foundActivity) {
                    sendLocalMessage("\u00a7e\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u043f\u0440\u0435\u0434\u0435\u043b\u0438\u0442\u044c \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u044c");
                }
                state = State.IDLE;
                return true;
            }

            if (hidingBlock) {
                Matcher actM = ACTIVITY_PATTERN.matcher(msg);
                if (actM.find()) {
                    foundActivity = true;

                    long sec =
                            Integer.parseInt(actM.group(1)) * 3600L +
                            Integer.parseInt(actM.group(2)) * 60L +
                            Integer.parseInt(actM.group(3));

                    int threshold = PlayTimeConfig.get().activitySeconds;

                    if (sec <= threshold) {
                        CommandQueue.add("hm spyfrz");
                        sendLocalMessage("\u00a7a\u0418\u0433\u0440\u043e\u043a \u0430\u043a\u0442\u0438\u0432\u0435\u043d! /hm spyfrz \u043e\u0442\u043f\u0440\u0430\u0432\u043b\u0435\u043d\u043e.");
                    } else {
                        sendLocalMessage("\u00a7c\u0418\u0433\u0440\u043e\u043a \u043d\u0435 \u0430\u043a\u0442\u0438\u0432\u0435\u043d \u043f\u0440\u043e\u0441\u043b\u0435\u0434\u0438\u0442\u0435 \u0441\u0430\u043c\u0438");
                    }
                }
                return true;
            }
        }

        return false;
    }

    private static String getServerSwitchCommand(String server) {
        if (server.startsWith("lanarchy")) {
            String num = server.substring("lanarchy".length());
            return "ln " + num;
        }
        if (server.startsWith("l2anarchy")) {
            String num = server.substring("l2anarchy".length());
            return "ln120 " + num;
        }
        if (server.startsWith("anarchy")) {
            String num = server.substring("anarchy".length());
            return "cn " + num;
        }
        return null;
    }

    private static void sendLocalMessage(String text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(text), false);
        }
    }

    public static State getState() {
        return state;
    }

    public static String getTargetNick() {
        return targetNick;
    }

    public static boolean isActive() {
        return state != State.IDLE && state != State.DONE;
    }
}
