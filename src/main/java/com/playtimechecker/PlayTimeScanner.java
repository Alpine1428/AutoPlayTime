
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.*;

public class PlayTimeScanner {

    private static final PlayTimeScanner INSTANCE = new PlayTimeScanner();
    public static PlayTimeScanner getInstance() { return INSTANCE; }

    private boolean scanning = false;
    private final List<String> players = new ArrayList<>();
    private final List<PlayerPlayTime> results = new ArrayList<>();

    private int currentIndex = 0;
    private int tickCounter = 0;

    public void startScan(MinecraftClient mc) {
        if (scanning || mc.player == null) return;

        players.clear();
        results.clear();
        currentIndex = 0;

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
    }

    public void tick(MinecraftClient mc) {
        if (!scanning || mc.player == null) return;

        if (++tickCounter < PlayTimeConfig.getInstance().getDelayTicks())
            return;

        tickCounter = 0;

        if (currentIndex >= players.size()) {
            scanning = false;
            mc.player.sendMessage(Text.literal("§aScan finished."), false);
            return;
        }

        mc.player.networkHandler.sendChatCommand(
                "playtime " + players.get(currentIndex++)
        );
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
