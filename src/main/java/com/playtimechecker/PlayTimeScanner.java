package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayTimeScanner {

    private static final PlayTimeScanner INSTANCE = new PlayTimeScanner();

    public static PlayTimeScanner getInstance() {
        return INSTANCE;
    }

    private boolean scanning = false;
    private List<String> playerNames = new ArrayList<>();
    private List<PlayerPlayTime> results = new ArrayList<>();
    private int currentIndex = 0;
    private int tickCounter = 0;
    private String pendingPlayer = null;

    private static final Pattern PLAYTIME_PATTERN =
            Pattern.compile("(\d+)\s*\u0434.*?(\d+)\s*\u0447.*?(\d+)\s*\u043c.*?(\d+)\s*\u0441");

    private static final Pattern PLAYTIME_SIMPLE_PATTERN =
            Pattern.compile("(\d+)\s*\u0447.*?(\d+)\s*\u043c.*?(\d+)\s*\u0441");

    public void startScan(MinecraftClient client) {
        if (client.player == null) return;
        if (scanning) {
            client.player.sendMessage(Text.literal("\u00a7e[PlayTime] \u00a7c\u0421\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 \u0443\u0436\u0435 \u0438\u0434\u0451\u0442!"), false);
            return;
        }

        results.clear();
        playerNames.clear();
        currentIndex = 0;
        tickCounter = 0;
        pendingPlayer = null;

        Collection<PlayerListEntry> entries = client.player.networkHandler.getPlayerList();
        for (PlayerListEntry entry : entries) {
            String name = entry.getProfile().getName();
            if (name != null && !name.isEmpty()) {
                playerNames.add(name);
            }
        }

        if (playerNames.isEmpty()) {
            client.player.sendMessage(Text.literal("\u00a7e[PlayTime] \u00a7c\u041d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u043e \u0438\u0433\u0440\u043e\u043a\u043e\u0432!"), false);
            return;
        }

        scanning = true;
        client.player.sendMessage(
                Text.literal("\u00a7e[PlayTime] \u00a7a\u0421\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 \u043d\u0430\u0447\u0430\u0442\u043e: \u00a7f" + playerNames.size() + " \u00a7a\u0438\u0433\u0440\u043e\u043a\u043e\u0432"),
                false
        );
    }

    public void tick(MinecraftClient client) {
        if (!scanning) return;
        if (client.player == null) return;

        tickCounter++;

        int delay = PlayTimeConfig.get().delayTicks;
        if (tickCounter < delay) return;
        tickCounter = 0;

        if (currentIndex >= playerNames.size()) {
            scanning = false;
            client.player.sendMessage(
                    Text.literal("\u00a7e[PlayTime] \u00a7a\u0421\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u043e! \u00a7f\u041d\u0430\u0439\u0434\u0435\u043d\u043e: " + results.size() + " \u00a7a\u0438\u0433\u0440\u043e\u043a\u043e\u0432. \u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u00a7fK \u00a7a\u0434\u043b\u044f \u043f\u0440\u043e\u0441\u043c\u043e\u0442\u0440\u0430."),
                    false
            );
            return;
        }

        pendingPlayer = playerNames.get(currentIndex);
        client.player.networkHandler.sendChatCommand("playtime " + pendingPlayer);
        currentIndex++;
    }

    public boolean onChatMessage(String message) {
        if (!scanning && pendingPlayer == null) return false;

        if (pendingPlayer != null && message.toLowerCase().contains(pendingPlayer.toLowerCase())) {

            Matcher m = PLAYTIME_PATTERN.matcher(message);
            if (m.find()) {
                int days = Integer.parseInt(m.group(1));
                int hours = Integer.parseInt(m.group(2));
                int minutes = Integer.parseInt(m.group(3));
                int seconds = Integer.parseInt(m.group(4));

                long totalSeconds = days * 86400L + hours * 3600L + minutes * 60L + seconds;
                String formatted = days + "\u0434 " + hours + "\u0447 " + minutes + "\u043c " + seconds + "\u0441";

                results.add(new PlayerPlayTime(pendingPlayer, formatted, totalSeconds));
                pendingPlayer = null;
                return true;
            }

            Matcher m2 = PLAYTIME_SIMPLE_PATTERN.matcher(message);
            if (m2.find()) {
                int hours = Integer.parseInt(m2.group(1));
                int minutes = Integer.parseInt(m2.group(2));
                int seconds = Integer.parseInt(m2.group(3));

                long totalSeconds = hours * 3600L + minutes * 60L + seconds;
                String formatted = hours + "\u0447 " + minutes + "\u043c " + seconds + "\u0441";

                results.add(new PlayerPlayTime(pendingPlayer, formatted, totalSeconds));
                pendingPlayer = null;
                return true;
            }
        }

        if (pendingPlayer != null && (message.contains("\u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d") || message.contains("\u043d\u0435 \u0441\u0443\u0449\u0435\u0441\u0442\u0432\u0443\u0435\u0442") || message.contains("never played"))) {
            pendingPlayer = null;
            return true;
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
        return playerNames.size();
    }

    public List<PlayerPlayTime> getResults() {
        return results;
    }
}
