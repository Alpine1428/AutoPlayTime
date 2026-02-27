package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayTimeScanner {

    public enum State { IDLE, SEND, WAIT }

    private static final PlayTimeScanner INSTANCE = new PlayTimeScanner();

    public static PlayTimeScanner get() {
        return INSTANCE;
    }

    private State state = State.IDLE;

    private final List<String> players = new ArrayList<>();
    private final Map<String, PlayerData> data = new LinkedHashMap<>();

    private int index = 0;
    private String current = null;
    private boolean hidingBlock = false;

    // Parse first line: Obshchee vremya v igre: Xch., Ym., Zs.
    private static final Pattern TOTAL =
            Pattern.compile("\u041e\u0431\u0449\u0435\u0435 \u0432\u0440\u0435\u043c\u044f \u0432 \u0438\u0433\u0440\u0435:\\s*(\\d+)\u0447\\.?,?\\s*(\\d+)\u043c\\.?,?\\s*(\\d+)\u0441");

    private static final String SEPARATOR = "---------------------------------------------------";
    private static final String PLAYTIME_HEADER = "--PlayTimeAPI--";

    private boolean firstTotalFound = false;

    public void start(MinecraftClient mc) {
        if (state != State.IDLE) return;

        players.clear();
        data.clear();
        index = 0;
        hidingBlock = false;

        if (mc.getNetworkHandler() == null) return;

        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList())
            players.add(e.getProfile().getName());

        if (players.isEmpty()) return;

        state = State.SEND;
    }

    public void stop() {
        state = State.IDLE;
        hidingBlock = false;
        CommandQueue.clear();
    }

    public void tick(MinecraftClient mc) {
        if (state != State.SEND) return;

        if (index >= players.size()) {
            state = State.IDLE;
            if (mc.player != null) {
                mc.player.sendMessage(
                    Text.literal("\u00a7a\u0421\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u043e: " + data.size() + " \u0438\u0433\u0440\u043e\u043a\u043e\u0432"),
                    false);
            }
            return;
        }

        current = players.get(index++);
        firstTotalFound = false;
        CommandQueue.add("playtime " + current);
        state = State.WAIT;
    }

    public boolean handle(String msg) {

        if (msg.contains(PLAYTIME_HEADER)) {
            hidingBlock = true;
            return true;
        }

        if (hidingBlock && msg.contains(SEPARATOR) && !msg.contains("PlayTimeAPI")) {
            hidingBlock = false;

            if (state == State.WAIT && !firstTotalFound) {
                state = State.SEND;
            }
            return true;
        }

        if (hidingBlock) {
            if (state == State.WAIT) {
                Matcher m = TOTAL.matcher(msg);
                if (m.find() && !firstTotalFound) {
                    firstTotalFound = true;

                    long sec =
                            Integer.parseInt(m.group(1)) * 3600L +
                            Integer.parseInt(m.group(2)) * 60L +
                            Integer.parseInt(m.group(3));

                    data.put(current, new PlayerData(current, sec));
                    state = State.SEND;
                }
            }
            return true;
        }

        return false;
    }

    public List<PlayerData> getSorted() {
        List<PlayerData> list = new ArrayList<>(data.values());
        Collections.sort(list);
        return list;
    }

    public boolean scanning() {
        return state != State.IDLE;
    }

    public int progress() {
        return index;
    }

    public int total() {
        return players.size();
    }

    public Map<String, PlayerData> getData() {
        return data;
    }
}
