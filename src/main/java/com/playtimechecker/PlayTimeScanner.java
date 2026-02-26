
package com.playtimechecker;

import java.util.*;
import java.util.regex.*;

public class PlayTimeScanner {

    public enum State { IDLE, SCANNING, WAITING }

    private static final PlayTimeScanner INSTANCE = new PlayTimeScanner();
    public static PlayTimeScanner get() { return INSTANCE; }

    private State state = State.IDLE;
    private final List<String> players = new ArrayList<>();
    private final Map<String, Long> playtimes = new HashMap<>();

    private int index = 0;
    private String current = null;

    private static final Pattern TIME =
            Pattern.compile("(\\d+)ч.*, (\\d+)м.*, (\\d+)с");

    public void start(net.minecraft.client.MinecraftClient mc) {
        if (state != State.IDLE) return;

        players.clear();
        playtimes.clear();
        index = 0;

        mc.getNetworkHandler().getPlayerList()
                .forEach(e -> players.add(e.getProfile().getName()));

        state = State.SCANNING;
    }

    public void stop() {
        state = State.IDLE;
        players.clear();
    }

    public void tick(net.minecraft.client.MinecraftClient mc) {
        if (state != State.SCANNING) return;

        if (index >= players.size()) {
            state = State.IDLE;
            return;
        }

        current = players.get(index++);
        state = State.WAITING;
        CommandQueue.add("playtime " + current);
    }

    public boolean handleChat(String msg) {
        if (state != State.WAITING) return false;

        Matcher m = TIME.matcher(msg);
        if (m.find()) {
            long sec =
                    Integer.parseInt(m.group(1)) * 3600L +
                    Integer.parseInt(m.group(2)) * 60L +
                    Integer.parseInt(m.group(3));

            playtimes.put(current, sec);
            state = State.SCANNING;
            return true;
        }

        return msg.contains("PlayTimeAPI");
    }

    public Map<String, Long> getPlaytimes() { return playtimes; }
    public boolean isScanning() { return state != State.IDLE; }
    public int getProgress() { return index; }
    public int getTotal() { return players.size(); }
}
