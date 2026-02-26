package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.*;
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

    // Pattern для "Активность <nick>"
    private static final Pattern ACTIVITY_PATTERN = Pattern.compile("Активность\s+(\S+)");
    // Pattern для "Общее время в игре: Xч., Yм., Zс."
    private static final Pattern TIME_PATTERN = Pattern.compile("Общее время в игре:\s*(\d+)ч\.?,?\s*(\d+)м\.?,?\s*(\d+)с\.");
    // Разделитель
    private static final String SEPARATOR = "---";

    private int scanProgress = 0;
    private int scanTotal = 0;

    public void startScan(MinecraftClient client) {
        if (scanning) {
            if (client.player != null) {
                client.player.sendMessage(Text.literal("§e[PlayTime] §cСканирование уже идёт..."), false);
            }
            return;
        }

        queue.clear();
        results.clear();
        responseLines.clear();
        collectingResponse = false;
        waitingForResponse = false;
        currentName = null;

        // Собираем всех игроков из TAB-листа
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
                client.player.sendMessage(Text.literal("§e[PlayTime] §cНет игроков онлайн!"), false);
            }
            return;
        }

        scanTotal = queue.size();
        scanProgress = 0;
        scanning = true;
        tickCounter = 0;

        if (client.player != null) {
            client.player.sendMessage(Text.literal("§e[PlayTime] §aНачинаю сканирование " + scanTotal + " игроков..."), false);
        }
    }

    public void tick(MinecraftClient client) {
        if (!scanning) return;
        if (client.player == null) return;

        // Если ждём ответ, просто ждём (таймаут 100 тиков = 5 секунд)
        if (waitingForResponse) {
            tickCounter++;
            if (tickCounter > 100) {
                // Таймаут — пропускаем игрока
                waitingForResponse = false;
                collectingResponse = false;
                responseLines.clear();
                tickCounter = 0;
            }
            return;
        }

        // Минимальная задержка между командами (2 тика)
        tickCounter++;
        if (tickCounter < 2) return;
        tickCounter = 0;

        if (queue.isEmpty()) {
            // Сканирование завершено
            scanning = false;
            Collections.sort(results);
            client.player.sendMessage(Text.literal("§e[PlayTime] §aСканирование завершено! Нажмите §6K §aчтобы открыть меню."), false);
            return;
        }

        // Отправляем команду для следующего игрока
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

    /**
     * Вызывается из mixin'а при получении сообщения.
     * Возвращает true, если сообщение нужно скрыть.
     */
    public boolean onChatMessage(String message) {
        if (!scanning && !waitingForResponse) return false;

        // Начало блока PlayTimeAPI
        if (message.contains("PlayTimeAPI")) {
            collectingResponse = true;
            responseLines.clear();
            responseLines.add(message);
            return true; // скрываем
        }

        if (collectingResponse) {
            responseLines.add(message);

            // Конец блока — строка с "---" после начала
            if (responseLines.size() > 1 && message.contains("---")) {
                // Парсим собранный ответ
                parseResponse();
                collectingResponse = false;
                waitingForResponse = false;
                responseLines.clear();
                tickCounter = 0;
                return true;
            }

            return true; // скрываем все строки внутри блока
        }

        // Если пришло "Игрок не найден" или подобное
        if (waitingForResponse && (message.contains("не найден") || message.contains("not found") || message.contains("Неизвестная команда"))) {
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
            // Ищем имя игрока
            Matcher actMatcher = ACTIVITY_PATTERN.matcher(line);
            if (actMatcher.find()) {
                playerName = actMatcher.group(1);
            }

            // Ищем "Общее время в игре:" — берём первое вхождение (глобальное)
            Matcher timeMatcher = TIME_PATTERN.matcher(line);
            if (timeMatcher.find() && firstTotalTime) {
                int hours = Integer.parseInt(timeMatcher.group(1));
                int minutes = Integer.parseInt(timeMatcher.group(2));
                int seconds = Integer.parseInt(timeMatcher.group(3));
                totalSec = hours * 3600L + minutes * 60L + seconds;
                totalTimeStr = hours + "ч. " + minutes + "м. " + seconds + "с.";
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
