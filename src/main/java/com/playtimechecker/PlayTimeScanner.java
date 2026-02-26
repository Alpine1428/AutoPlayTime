
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import java.util.*;
import java.util.regex.*;

public class PlayTimeScanner {

    private static final PlayTimeScanner INSTANCE = new PlayTimeScanner();
    public static PlayTimeScanner getInstance() { return INSTANCE; }

    private boolean scanning = false;
    private final List<String> players = new ArrayList<>();
    private final List<PlayerPlayTime> results = new ArrayList<>();

    private int index = 0;
    private int tick = 0;
    private String waitingFor = null;

    private static final Pattern TIME =
            Pattern.compile("(\\d+)ч.*, (\\d+)м.*, (\\d+)с");

    public void start(MinecraftClient mc) {
        if (scanning || mc.player == null) return;

        players.clear();
        results.clear();
        index = 0;

        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList())
            players.add(e.getProfile().getName());

        scanning = true;

        mc.player.sendMessage(Text.literal("§aScan started. Players: " + players.size()), false);
    }

    public void stop() {
        scanning = false;
        players.clear();
        waitingFor = null;
    }

    public void tick(MinecraftClient mc) {
        if (!scanning || mc.player == null) return;

        if (++tick < PlayTimeConfig.get().delayTicks) return;
        tick = 0;

        if (waitingFor != null) return;

        if (index >= players.size()) {
            scanning = false;
            mc.player.sendMessage(Text.literal("§aScan finished."), false);
            return;
        }

        waitingFor = players.get(index++);
        mc.player.networkHandler.sendChatCommand("playtime " + waitingFor);
    }

    public boolean handleChat(String msg) {

        if (waitingFor == null) return false;

        Matcher m = TIME.matcher(msg);

        if (m.find()) {
            int h = Integer.parseInt(m.group(1));
            int min = Integer.parseInt(m.group(2));
            int s = Integer.parseInt(m.group(3));

            long total = h * 3600L + min * 60L + s;
            results.add(new PlayerPlayTime(waitingFor,
                    h + "h " + min + "m " + s + "s",
                    total));

            waitingFor = null;
            return true;
        }

        return false;
    }

    public List<PlayerPlayTime> getResults() { return results; }
    public boolean isScanning() { return scanning; }
}
