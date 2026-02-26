
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
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
    private int tick = 0;
    private String current = null;

    private static final Pattern TIME =
            Pattern.compile("(\\d+)ч.*, (\\d+)м.*, (\\d+)с");

    public void start(MinecraftClient mc) {
        if (state != State.IDLE) return;

        players.clear();
        playtimes.clear();
        index = 0;

        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList())
            players.add(e.getProfile().getName());

        state = State.SCANNING;
    }

    public void stop() {
        state = State.IDLE;
        players.clear();
        current = null;
    }

    public void tick(MinecraftClient mc) {
        if (state != State.SCANNING) return;
        if (mc.player == null) return;

        if (++tick < PlayTimeConfig.get().delayTicks) return;
        tick = 0;

        if (index >= players.size()) {
            state = State.IDLE;
            return;
        }

        current = players.get(index++);
        state = State.WAITING;
        mc.player.networkHandler.sendChatCommand("playtime " + current);
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
            current = null;
            return true;
        }

        if (msg.contains("PlayTimeAPI"))
            return true;

        return false;
    }

    public Map<String, Long> getPlaytimes() {
        return playtimes;
    }

    public boolean isScanning() {
        return state != State.IDLE;
    }
}
