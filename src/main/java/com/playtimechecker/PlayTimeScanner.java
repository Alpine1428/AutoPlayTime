package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayTimeScanner {

    private static final PlayTimeScanner INSTANCE = new PlayTimeScanner();

    public static PlayTimeScanner getInstance() {
        return INSTANCE;
    }

    private final List<String> queue = new ArrayList<>();
    private final List<PlayerPlayTime> results = new CopyOnWriteArrayList<>();

    private boolean scanning = false;
    private boolean waitingForResponse = false;
    private String currentName = null;
    private int tickCounter = 0;
    private boolean collectingResponse = false;
    private final List<String> responseLines = new ArrayList<>();

    private static final Pattern ACTIVITY_PATTERN =
        Pattern.compile("\u0410\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u044c\\s+(\\S+)");

    private static final Pattern TIME_PATTERN =
        Pattern.compile("\u041e\u0431\u0449\u0435\u0435 \u0432\u0440\u0435\u043c\u044f \u0432 \u0438\u0433\u0440\u0435:\\s*(\\d+)\u0447\\.?,?\\s*(\\d+)\u043c\\.?,?\\s*(\\d+)\u0441\\.");

    private int scanProgress = 0;
    private int scanTotal = 0;

    public void startScan(MinecraftClient client) {
        if (scanning) {
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("\u00a7e[PlayTime] \u00a7c\u0421\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 \u0443\u0436\u0435 \u0438\u0434\u0451\u0442..."),
                    false
                );
            }
            return;
        }

        queue.clear();
        results.clear();
        responseLines.clear();
        collectingResponse = false;
        waitingForResponse = false;
        currentName = null;

        if (client.getNetworkHandler() != null) {
            Collection<PlayerListEntry> entries = client.getNetworkHandler().getPlayerList();
            for (PlayerListEntry entry : entries) {
                String name = entry.getProfile().getName();
                if (name != null && !name.isEmpty()) {
                    queue.add(name);
                }
            }
        }

        if (queue.isEmpty()) {
            if (client.player != null) {
                client.player.sendMessage(
                    Text.literal("\u00a7e[PlayTime] \u00a7c\u041d\u0435\u0442 \u0438\u0433\u0440\u043e\u043a\u043e\u0432 \u043e\u043d\u043b\u0430\u0439\u043d!"),
                    false
                );
            }
            return;
        }

        scanTotal = queue.size();
        scanProgress = 0;
        scanning = true;
        tickCounter = 0;

        if (client.player != null) {
            client.player.sendMessage(
                Text.literal("\u00a7e[PlayTime] \u00a7a\u041d\u0430\u0447\u0438\u043d\u0430\u044e \u0441\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 " + scanTotal + " \u0438\u0433\u0440\u043e\u043a\u043e\u0432... (delay: " + PlayTimeConfig.getInstance().getDelayTicks() + " ticks)"),
                false
            );
        }
    }

    public void tick(MinecraftClient client) {
        if (!scanning) return;
        if (client.player == null) return;

        if (waitingForResponse) {
            tickCounter++;
            if (tickCounter > 100) {
                waitingForResponse = false;
                collectingResponse = false;
                responseLines.clear();
                tickCounter = 0;
            }
            return;
        }

        tickCounter++;
        if (tickCounter < PlayTimeConfig.getInstance().getDelayTicks()) return;
        tickCounter = 0;

        if (queue.isEmpty()) {
            scanning = false;
            Collections.sort(results);
            client.player.sendMessage(
                Text.literal("\u00a7e[PlayTime] \u00a7a\u0421\u043a\u0430\u043d\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435 \u0437\u0430\u0432\u0435\u0440\u0448\u0435\u043d\u043e! \u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u00a76K \u00a7a\u0447\u0442\u043e\u0431\u044b \u043e\u0442\u043a\u0440\u044b\u0442\u044c \u043c\u0435\u043d\u044e."),
                false
            );
            return;
        }

        currentName = queue.remove(0);
        scanProgress++;

        String command = "playtime " + currentName;
        if (client.getNetworkHandler() != null) {
            client.getNetworkHandler().sendChatCommand(command);
        }

        waitingForResponse = true;
        collectingResponse = false;
        responseLines.clear();
    }

    public boolean onChatMessage(String message) {
        if (!scanning && !waitingForResponse) return false;

        if (message.contains("PlayTimeAPI")) {
            collectingResponse = true;
            responseLines.clear();
            responseLines.add(message);
            return true;
        }

        if (collectingResponse) {
            responseLines.add(message);

            if (responseLines.size() > 1 && message.contains("---")) {
                parseResponse();
                collectingResponse = false;
                waitingForResponse = false;
                responseLines.clear();
                tickCounter = 0;
                return true;
            }

            return true;
        }

        if (waitingForResponse && (message.contains("\u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d") || message.contains("not found"))) {
            waitingForResponse = false;
            collectingResponse = false;
            responseLines.clear();
            tickCounter = 0;
            return true;
        }

        return false;
    }

    private void parseResponse() {
        String playerName = currentName;
        String totalTimeStr = null;
        long totalSec = 0;
        boolean firstTotalTime = true;

        for (String line : responseLines) {
            Matcher actMatcher = ACTIVITY_PATTERN.matcher(line);
            if (actMatcher.find()) {
                playerName = actMatcher.group(1);
            }

            Matcher timeMatcher = TIME_PATTERN.matcher(line);
            if (timeMatcher.find() && firstTotalTime) {
                int hours = Integer.parseInt(timeMatcher.group(1));
                int minutes = Integer.parseInt(timeMatcher.group(2));
                int seconds = Integer.parseInt(timeMatcher.group(3));
                totalSec = hours * 3600L + minutes * 60L + seconds;
                totalTimeStr = hours + "\u0447. " + minutes + "\u043c. " + seconds + "\u0441.";
                firstTotalTime = false;
            }
        }

        if (playerName != null && totalTimeStr != null) {
            results.add(new PlayerPlayTime(playerName, totalTimeStr, totalSec));
        }
    }

    public List<PlayerPlayTime> getResults() {
        return results;
    }

    public boolean isScanning() {
        return scanning;
    }

    public int getScanProgress() {
        return scanProgress;
    }

    public int getScanTotal() {
        return scanTotal;
    }
}
