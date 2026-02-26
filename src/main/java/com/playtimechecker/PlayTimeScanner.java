
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

    private int currentIndex = 0;
    private int tickCounter = 0;
    private String waitingFor = null;

    private static final Pattern TIME =
            Pattern.compile("(\\d+)ч.*, (\\d+)м.*, (\\d+)с");

    public void startScan(MinecraftClient mc) {
        if (scanning || mc.player == null) return;

        players.clear();
        results.clear();
        currentIndex = 0;
        waitingFor = null;

        for (PlayerListEntry e : mc.getNetworkHandler().getPlayerList())
            players.add(e.getProfile().getName());

        scanning = true;

        mc.player.sendMessage(
                Text.literal("§aScan started. Players: " + players.size()),
                false
        );
    }

    public void stopScan() {
        scanning = false;
        players.clear();
        waitingFor = null;
    }

    public void tick(MinecraftClient mc) {
        if (!scanning || mc.player == null) return;

        if (++tickCounter < PlayTimeConfig.getInstance().getDelayTicks())
            return;

        tickCounter = 0;

        if (waitingFor != null) return;

        if (currentIndex >= players.size()) {
            scanning = false;
            mc.player.sendMessage(Text.literal("§aScan finished."), false);
            return;
        }

        waitingFor = players.get(currentIndex++);
        mc.player.networkHandler.sendChatCommand("playtime " + waitingFor);
    }

    // ✅ ВЕРНУЛИ handleChat
    public boolean handleChat(String msg) {

        if (waitingFor == null) return false;

        Matcher m = TIME.matcher(msg);

        if (m.find()) {
            int h = Integer.parseInt(m.group(1));
            int min = Integer.parseInt(m.group(2));
            int s = Integer.parseInt(m.group(3));

            long total = h * 3600L + min * 60L + s;

            results.add(new PlayerPlayTime(
                    waitingFor,
                    h + "h " + min + "m " + s + "s",
                    total
            ));

            waitingFor = null;
            return true; // скрываем сообщение
        }

        return false;
    }

    public boolean isScanning() {
        return scanning;
    }

    public int getScanProgress() {
        return currentIndex;
    }

    public int getScanTotal() {
        return players.size();
    }

    public List<PlayerPlayTime> getResults() {
        return results;
    }
}
