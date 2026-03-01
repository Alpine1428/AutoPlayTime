package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModerationHandler {

    public enum State {
        IDLE,
        REPORT_OPEN_WAIT,
        REPORT_SCAN,
        REPORT_NEXT_PAGE_WAIT,
        REPORT_CLICKED,
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
    private static boolean hasReport = false;

    private static int switchWaitTicks = 0;
    private static final int SWITCH_WAIT = 200;

    private static int recheckTicks = 0;
    private static final int RECHECK_INTERVAL = 60;
    private static final int RECHECK_TIMEOUT = 300;
    private static final long ACTIVITY_THRESHOLD = 7;

    private static int reportWaitTicks = 0;
    private static int reportClickDelay = 0;

    private static final String SUSPECT_KEY = "\u041f\u043e\u0434\u043e\u0437\u0440\u0435\u0432\u0430\u0435\u043c\u044b\u0439:";

    private static final Pattern FIND_PATTERN =
            Pattern.compile("\u0418\u0433\u0440\u043e\u043a\\s+(\\S+)\\s+\u043d\u0430\u0445\u043e\u0434\u0438\u0442\u0441\u044f \u043d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435\\s+(\\S+)");

    private static final Pattern ACTIVITY_PATTERN =
            Pattern.compile("\u041f\u043e\u0441\u043b\u0435\u0434\u043d\u044f\u044f \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u044c:\\s*(\\d+)\u0447\\.?,?\\s*(\\d+)\u043c\\.?,?\\s*(\\d+)\u0441");

    private static final String SEPARATOR = "---------------------------------------------------";
    private static final String PLAYTIME_HEADER = "--PlayTimeAPI--";

    public static void startModeration(String nick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        targetNick = nick;
        moderatorNick = mc.player.getGameProfile().getName();
        hidingBlock = false;
        foundActivity = false;
        waitTicks = 0;
        switchWaitTicks = 0;
        recheckTicks = 0;
        reportWaitTicks = 0;
        reportClickDelay = 0;

        hasReport = ReportManager.hasReport(nick);

        if (hasReport) {
            sendMsg("\u00a7b\u041e\u0442\u043a\u0440\u044b\u0432\u0430\u0435\u043c \u0440\u0435\u043f\u043e\u0440\u0442 " + nick + "...");
            mc.setScreen(null);
            mc.player.networkHandler.sendChatCommand("reportlist");
            state = State.REPORT_OPEN_WAIT;
        } else {
            startSpyFind();
        }
    }

    private static void startSpyFind() {
        state = State.SPY_SENT;
        CommandQueue.add("hm spy " + targetNick);
        CommandQueue.add("find " + targetNick);
    }

    public static void stop() {
        state = State.IDLE;
        targetNick = null;
        hidingBlock = false;
    }

    public static void tick() {
        if (state == State.IDLE || state == State.DONE) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        waitTicks++;
        if (waitTicks > 1200) {
            sendMsg("\u00a7c\u0422\u0430\u0439\u043c\u0430\u0443\u0442");
            state = State.IDLE;
            return;
        }

        // Report GUI states
        if (state == State.REPORT_OPEN_WAIT) {
            reportWaitTicks++;
            if (mc.player.currentScreenHandler != null
                    && mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
                state = State.REPORT_SCAN;
                reportWaitTicks = 0;
            }
            if (reportWaitTicks > 100) {
                sendMsg("\u00a7c\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u0440\u0435\u043f\u043e\u0440\u0442\u044b");
                startSpyFind();
            }
            return;
        }

        if (state == State.REPORT_SCAN) {
            reportWaitTicks++;
            if (reportWaitTicks < 10) return;
            if (mc.player.currentScreenHandler == null
                    || mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
                startSpyFind();
                return;
            }
            int slotCount = mc.player.currentScreenHandler.slots.size();
            int maxSlot = Math.min(45, slotCount);
            for (int i = 0; i < maxSlot; i++) {
                ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
                if (stack == null || stack.isEmpty()) continue;
                List<Text> lore = stack.getTooltip(mc.player, TooltipContext.Default.BASIC);
                for (Text t : lore) {
                    String line = t.getString();
                    int idx = line.indexOf(SUSPECT_KEY);
                    if (idx >= 0) {
                        String after = line.substring(idx + SUSPECT_KEY.length()).trim();
                        String nick = after.split("[\\s(]")[0].trim();
                        if (nick.equalsIgnoreCase(targetNick)) {
                            mc.interactionManager.clickSlot(
                                    mc.player.currentScreenHandler.syncId,
                                    i, 0, SlotActionType.PICKUP, mc.player);
                            sendMsg("\u00a7a\u0420\u0435\u043f\u043e\u0440\u0442 " + targetNick + " \u043e\u0442\u043a\u0440\u044b\u0442!");
                            state = State.REPORT_CLICKED;
                            reportClickDelay = 0;
                            return;
                        }
                    }
                }
            }
            if (slotCount >= 54) {
                ItemStack s44 = mc.player.currentScreenHandler.getSlot(44).getStack();
                if (s44 != null && !s44.isEmpty()) {
                    ItemStack s53 = mc.player.currentScreenHandler.getSlot(53).getStack();
                    if (s53 != null && !s53.isEmpty()) {
                        mc.interactionManager.clickSlot(
                                mc.player.currentScreenHandler.syncId,
                                53, 0, SlotActionType.PICKUP, mc.player);
                        state = State.REPORT_NEXT_PAGE_WAIT;
                        reportWaitTicks = 0;
                        return;
                    }
                }
            }
            sendMsg("\u00a7e\u0420\u0435\u043f\u043e\u0440\u0442 \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d");
            mc.player.closeScreen();
            startSpyFind();
            return;
        }

        if (state == State.REPORT_NEXT_PAGE_WAIT) {
            reportWaitTicks++;
            if (reportWaitTicks > 10) {
                state = State.REPORT_SCAN;
                reportWaitTicks = 0;
            }
            return;
        }

        if (state == State.REPORT_CLICKED) {
            reportClickDelay++;
            if (reportClickDelay >= 40) {
                if (mc.player.currentScreenHandler != null
                        && mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
                    mc.player.closeScreen();
                }
                startSpyFind();
            }
            return;
        }

        // Server switch wait
        if (state == State.SERVER_SWITCH_WAIT) {
            switchWaitTicks++;
            if (switchWaitTicks >= SWITCH_WAIT) {
                sendMsg("\u00a7e\u041f\u0435\u0440\u0435\u0445\u043e\u0434 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d. \u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430...");
                foundActivity = false;
                hidingBlock = false;
                CommandQueue.add("playtime " + targetNick);
                state = State.PLAYTIME_SENT;
            }
            return;
        }

        // Recheck loop
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

    /**
     * Called ONLY when isActive() == true (from ChatMixin).
     * Returns true if message should be hidden from chat.
     */
    public static boolean handle(String msg) {
        if (state == State.IDLE) return false;

        // Only intercept /find response when we are waiting for it
        if (state == State.SPY_SENT || state == State.FIND_SENT) {
            Matcher findM = FIND_PATTERN.matcher(msg);
            if (findM.find()) {
                String server = findM.group(2).toLowerCase();
                String switchCmd = getServerSwitch(server);
                if (switchCmd != null && !targetNick.equalsIgnoreCase(moderatorNick)) {
                    CommandQueue.add(switchCmd);
                    switchWaitTicks = 0;
                    state = State.SERVER_SWITCH_WAIT;
                    sendMsg("\u00a7e\u041f\u0435\u0440\u0435\u0445\u043e\u0434: " + server + " (10\u0441)");
                } else {
                    foundActivity = false;
                    hidingBlock = false;
                    CommandQueue.add("playtime " + targetNick);
                    state = State.PLAYTIME_SENT;
                }
                return true;
            }
        }

        // Only intercept playtime block when we sent /playtime
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
                long sec = Integer.parseInt(actM.group(1)) * 3600L
                         + Integer.parseInt(actM.group(2)) * 60L
                         + Integer.parseInt(actM.group(3));
                if (sec <= ACTIVITY_THRESHOLD) {
                    CommandQueue.add("hm spyfrz");
                    sendMsg("\u00a7a\u0410\u043a\u0442\u0438\u0432\u0435\u043d! " + sec + "\u0441 /hm spyfrz");
                    state = State.IDLE;
                } else {
                    if (state == State.PLAYTIME_SENT) {
                        sendMsg("\u00a7e\u0410\u043a\u0442: " + sec + "\u0441 > 7\u0441. \u041f\u043e\u0432\u0442\u043e\u0440 15\u0441...");
                        state = State.RECHECK_WAIT;
                        recheckTicks = 0;
                    } else if (state == State.RECHECK_PLAYTIME_SENT) {
                        sendMsg("\u00a7e\u0410\u043a\u0442: " + sec + "\u0441 \u0416\u0434\u0435\u043c...");
                        state = State.RECHECK_WAIT;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private static String getServerSwitch(String server) {
        if (server.startsWith("lanarchy")) return "ln " + server.substring(8);
        if (server.startsWith("l2anarchy")) return "ln120 " + server.substring(9);
        if (server.startsWith("anarchy")) return "cn " + server.substring(7);
        return null;
    }

    private static void sendMsg(String text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(Text.literal(text), false);
    }

    public static State getState() { return state; }
    public static String getTargetNick() { return targetNick; }
    public static boolean isActive() { return state != State.IDLE && state != State.DONE; }
}
