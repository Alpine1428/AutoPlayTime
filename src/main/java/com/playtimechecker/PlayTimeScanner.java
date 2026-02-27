
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import java.util.*;
import java.util.regex.*;

public class PlayTimeScanner {

    public enum State { IDLE, SEND, WAIT }

    private static final PlayTimeScanner INSTANCE = new PlayTimeScanner();
    public static PlayTimeScanner get() { return INSTANCE; }

    private State state = State.IDLE;

    private final List<String> players = new ArrayList<>();
    private final Map<String, PlayerData> data = new HashMap<>();

    private int index = 0;
    private String current = null;
    private boolean hide = false;

    private static final Pattern TOTAL =
        Pattern.compile("Общее время в игре:\s*(\\d+)ч.*,\s*(\\d+)м.*,\s*(\\d+)с");

    public void start(MinecraftClient mc) {

        if (state != State.IDLE) return;

        players.clear();
        data.clear();
        index = 0;

        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList())
            players.add(e.getProfile().getName());

        state = State.SEND;
    }

    public void stop() {
        state = State.IDLE;
    }

    public void tick(MinecraftClient mc) {

        if (state != State.SEND) return;

        if (index >= players.size()) {
            state = State.IDLE;
            return;
        }

        current = players.get(index++);
        CommandQueue.add("playtime " + current);
        state = State.WAIT;
    }

    public boolean handle(String msg) {

        if (msg.contains("---------------------------------------------------")) {
            hide = !hide;
            return true;
        }

        if (hide)
            return true;

        if (state != State.WAIT)
            return false;

        Matcher m = TOTAL.matcher(msg);

        if (m.find()) {

            long sec =
                Integer.parseInt(m.group(1)) * 3600L +
                Integer.parseInt(m.group(2)) * 60L +
                Integer.parseInt(m.group(3));

            data.put(current, new PlayerData(current, sec));
            state = State.SEND;
            return true;
        }

        return false;
    }

    public List<PlayerData> getSorted() {
        List<PlayerData> list = new ArrayList<>(data.values());
        Collections.sort(list);
        return list;
    }

    public boolean scanning() { return state != State.IDLE; }
    public int progress() { return index; }
    public int total() { return players.size(); }
}
