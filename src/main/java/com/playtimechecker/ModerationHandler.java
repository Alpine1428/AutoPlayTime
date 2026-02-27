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
        SERVER_SWITCH_WAIT,
        PLAYTIME_SENT,
        RECHECK_WAIT,
        RECHECK_PLAYTIME_SENT,
        DONE
    }

    private static State state = State.IDLE;
    private static String targetNick = null;
    private static String moderatorNick = null;
    private static int waitTicks = 0;
    private static boolean hidingBlock = false;
    private static boolean foundActivity = false;

    // Server switch wait
    private static int switchWaitTicks = 0;
    private static final int SWITCH_WAIT = 200; // 10 sec = 200 ticks

    // Recheck loop
    private static int recheckTicks = 0;
    private static final int RECHECK_INTERVAL = 60;  // 3 sec
    private static final int RECHECK_TIMEOUT = 300;  // 15 sec
    private static final long ACTIVITY_THRESHOLD = 7; // 7 sec

    private static final Pattern FIND_PATTERN =
            Pattern.compile("\u0418\u0433\u0440\u043e\u043a\\s+(\\S+)\\s+\u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435\\s+(\\S+)");

    private static final Pattern ACTIVITY_PATTERN =
            Pattern.compile("\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u044f\u044f \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u044c:\\s*(\\d+)\u0447\\.?,?\\s*(\\d+)\u043c\\.?,?\\s*(\\d+)\u0441");

    private static final String SEPARATOR = "---------------------------------------------------";
    private static final String PLAYTIME_HEADER = "--PlayTimeAPI--";

    private static boolean didServerSwitch = false;

    public static void startModeration(String nick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        targetNick = nick;
        moderatorNick = mc.player.getGameProfile().getName();
        state = State.SPY_SENT;
        hidingBlock = false;
        foundActivity = false;
        waitTicks = 0;
        switchWaitTicks = 0;
        recheckTicks = 0;
        didServerSwitch = false;

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

        // Global timeout 60 sec
        if (waitTicks > 1200) {
            sendMsg("\u00a7c\u0422\u0430\u0439\u043c\u0430\u0443\u0442 \u043c\u043e\u0434\u0435\u0440\u0430\u0446\u0438\u0438");
            state = State.IDLE;
            return;
        }

        // Wait 10 sec after server switch before sending /playtime
        if (state == State.SERVER_SWITCH_WAIT) {
            switchWaitTicks++;
            if (switchWaitTicks >= SWITCH_WAIT) {
                sendMsg("\u00a7e\u041f\u0435\u0440\u0435\u0445\u043e\u0434 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d. \u041f\u0440\u043e\u0432\u0435\u0440\u044f\u0435\u043c \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u044c...");
                foundActivity = false;
                hidingBlock = false;
                CommandQueue.add("playtime " + targetNick);
                state = State.PLAYTIME_SENT;
            }
            return;
        }

        // Recheck loop: every 3 sec send /playtime, timeout 15 sec
        if (state == State.RECHECK_WAIT) {
            recheckTicks++;

            if (recheckTicks >= RECHECK_TIMEOUT) {
                sendMsg("\u00a7c\u0418\u0433\u0440\u043e\u043a \u0430\u0444\u043a \u043f\u0440\u043e\u0441\u043b\u0435\u0434\u0438\u0442\u0435 \u0441\u0430\u043c\u0438");
                state = State.IDLE;
                return;
            }

            if (recheckTicks % RECHECK_INTERVAL == 0) {
                foundActivity = false;
                hidingBlock = false;
                CommandQueue.add("playtime " + targetNick);
                state = State.RECHECK_PLAYTIME_SENT;
            }
        }
    }

    public static boolean handle(String msg) {
        if (state == State.IDLE) return false;

        // Handle /find response
        if (state == State.SPY_SENT || state == State.FIND_SENT) {
            Matcher findM = FIND_PATTERN.matcher(msg);
            if (findM.find()) {
                String server = findM.group(2).toLowerCase();
                String switchCmd = getServerSwitch(server);

                if (switchCmd != null && !targetNick.equalsIgnoreCase(moderatorNick)) {
                    // Need to switch server -> wait 10 sec
                    CommandQueue.add(switchCmd);
                    didServerSwitch = true;
                    switchWaitTicks = 0;
                    state = State.SERVER_SWITCH_WAIT;
                    sendMsg("\u00a7e\u041f\u0435\u0440\u0435\u0445\u043e\u0434 \u043d\u0430 " + server + "... \u0416\u0434\u0435\u043c 10\u0441");
                } else {
                    // Same server or same nick -> check immediately
                    foundActivity = false;
                    hidingBlock = false;
                    CommandQueue.add("playtime " + targetNick);
                    state = State.PLAYTIME_SENT;
                }
                return true;
            }
        }

        // Handle playtime response
        if (state == State.PLAYTIME_SENT || state == State.RECHECK_PLAYTIME_SENT) {
            return handleBlock(msg);
        }

        return false;
    }

    private static boolean handleBlock(String msg) {

        if (msg.contains(PLAYTIME_HEADER)) {
            hidingBlock = true;
            return true;
        }

        if (hidingBlock && msg.contains(SEPARATOR) && !msg.contains("PlayTimeAPI")) {
            hidingBlock = false;
            if (!foundActivity) {
                sendMsg("\u00a7e\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u0430 \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u044c");
                state = State.IDLE;
            }
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

                if (sec <= ACTIVITY_THRESHOLD) {
                    // Active! Freeze
                    CommandQueue.add("hm spyfrz");
                    sendMsg("\u00a7a\u0410\u043a\u0442\u0438\u0432\u0435\u043d! " + sec + "\u0441. /hm spyfrz");
                    state = State.IDLE;
                } else {
                    // Not active enough
                    if (state == State.PLAYTIME_SENT) {
                        // First check failed -> start recheck loop
                        sendMsg("\u00a7e\u0410\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u044c: " + sec + "\u0441 > 7\u0441. \u041f\u043e\u0432\u0442\u043e\u0440 15\u0441...");
                        state = State.RECHECK_WAIT;
                        recheckTicks = 0;
                    } else if (state == State.RECHECK_PLAYTIME_SENT) {
                        // Still not active -> back to wait
                        sendMsg("\u00a7e\u0410\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u044c: " + sec + "\u0441. \u0416\u0434\u0435\u043c...");
                        state = State.RECHECK_WAIT;
                    }
                }
            }
            return true;
        }

        return false;
    }

    private static String getServerSwitch(String server) {
        if (server.startsWith("lanarchy")) {
            return "ln " + server.substring(8);
        }
        if (server.startsWith("l2anarchy")) {
            return "ln120 " + server.substring(9);
        }
        if (server.startsWith("anarchy")) {
            return "cn " + server.substring(7);
        }
        return null;
    }

    private static void sendMsg(String text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(text), false);
        }
    }

    public static State getState() { return state; }
    public static String getTargetNick() { return targetNick; }
    public static boolean isActive() { return state != State.IDLE && state != State.DONE; }
}
