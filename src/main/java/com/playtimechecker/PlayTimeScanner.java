
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import java.util.*;
import java.util.regex.*;

public class PlayTimeScanner {

    public enum State { IDLE, SENDING, WAITING }

    private static final PlayTimeScanner INSTANCE = new PlayTimeScanner();
    public static PlayTimeScanner get() { return INSTANCE; }

    private State state = State.IDLE;
    private final List<String> players = new ArrayList<>();
    private final Map<String, PlayerData> data = new HashMap<>();

    private int index = 0;
    private String current = null;
    private boolean hideBlock = false;

    private static final Pattern TIME =
            Pattern.compile("Общее время в игре:\s*(\\d+)ч.*,\s*(\\d+)м.*,\s*(\\d+)с");

    public void start(MinecraftClient mc) {

        if (state != State.IDLE) return;

        players.clear();
        data.clear();
        index = 0;

        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList())
            players.add(e.getProfile().getName());

        state = State.SENDING;
    }

    public void stop() {
        state = State.IDLE;
        players.clear();
    }

    public void tick(MinecraftClient mc) {

        if (state == State.IDLE) return;

        if (state == State.SENDING) {

            if (index >= players.size()) {
                state = State.IDLE;
                return;
            }

            current = players.get(index++);
            CommandQueue.add("playtime " + current);
            state = State.WAITING;
        }
    }

    public boolean handleChat(String msg) {

        if (msg.contains("PlayTimeAPI")) {
            hideBlock = true;
            return true;
        }

        if (hideBlock) {
            if (msg.contains("---"))
                hideBlock = false;
            return true;
        }

        if (state != State.WAITING) return false;

        Matcher m = TIME.matcher(msg);

        if (m.find()) {

            long sec =
                    Integer.parseInt(m.group(1)) * 3600L +
                    Integer.parseInt(m.group(2)) * 60L +
                    Integer.parseInt(m.group(3));

            data.put(current, new PlayerData(current, sec));
            state = State.SENDING;
            return true;
        }

        return false;
    }

    public List<PlayerData> getSorted() {
        List<PlayerData> list = new ArrayList<>(data.values());
        Collections.sort(list);
        return list;
    }

    public boolean isScanning() { return state != State.IDLE; }
    public int getProgress() { return index; }
    public int getTotal() { return players.size(); }
}
