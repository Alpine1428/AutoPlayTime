package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportManager {

    private static boolean scanning = false;
    private static boolean waitingForGui = false;
    private static boolean pageScanned = false;
    private static int tick = 0;
    private static int waitTick = 0;

    private static final Set<String> reportedNicks = new LinkedHashSet<>();
    private static final Map<String, String> reportDetails = new LinkedHashMap<>();

    // Pattern to find suspect nick in lore
    // Line format: | Podozrevaemyj: NICK (PZH: 0, LZH: 0 | PP: 0, LP: 0)
    private static final Pattern SUSPECT_PATTERN =
            Pattern.compile("\u041f\u043e\u0434\u043e\u0437\u0440\u0435\u0432\u0430\u0435\u043c\u044b\u0439:\\s*(\\S+)");

    // Pattern for title line to get server info
    // Format: Delo igroka NICK (Server)
    private static final Pattern TITLE_PATTERN =
            Pattern.compile("\u0414\u0435\u043b\u043e \u0438\u0433\u0440\u043e\u043a\u0430\\s+(\\S+)");

    public static void start() {
        reportedNicks.clear();
        reportDetails.clear();
        scanning = true;
        waitingForGui = true;
        pageScanned = false;
        waitTick = 0;
        tick = 0;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.networkHandler.sendChatCommand("reportlist");
        }
    }

    public static void stop() {
        scanning = false;
        waitingForGui = false;
    }

    public static void tick() {
        if (!scanning) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        // Wait for GUI to open
        if (waitingForGui) {
            waitTick++;
            if (mc.player.currentScreenHandler != null
                    && mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
                waitingForGui = false;
                pageScanned = false;
                tick = 0;
            }
            if (waitTick > 100) {
                scanning = false;
                waitingForGui = false;
                sendLocalMessage("\u00a7c\u0422\u0430\u0439\u043c\u0430\u0443\u0442 \u043e\u0436\u0438\u0434\u0430\u043d\u0438\u044f GUI \u0440\u0435\u043f\u043e\u0440\u0442\u043e\u0432");
            }
            return;
        }

        // Check if GUI is still open
        if (mc.player.currentScreenHandler == null
                || mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
            scanning = false;
            return;
        }

        // Wait a bit for items to load
        if (++tick < 10) return;

        if (!pageScanned) {
            pageScanned = true;
            scanCurrentPage(mc);
        }

        // After scanning, check if we need next page
        if (tick == 12) {
            int slotCount = mc.player.currentScreenHandler.slots.size();

            // Large chest has 54 slots (0-53)
            // Reports in slots 0-44 (45 items)
            // Slot 44 (45th, index 44) - if not empty, there are more items
            // Slot 53 (54th, index 53) - next page button

            if (slotCount >= 54) {
                ItemStack slot44 = mc.player.currentScreenHandler.getSlot(44).getStack();

                if (slot44 != null && !slot44.isEmpty()) {
                    // There might be more pages, click slot 53 (next page)
                    ItemStack slot53 = mc.player.currentScreenHandler.getSlot(53).getStack();
                    if (slot53 != null && !slot53.isEmpty()) {
                        mc.interactionManager.clickSlot(
                                mc.player.currentScreenHandler.syncId,
                                53, 0, SlotActionType.PICKUP, mc.player
                        );
                        // Reset for next page
                        tick = 0;
                        pageScanned = false;
                        waitingForGui = true;
                        waitTick = 0;
                        return;
                    }
                }
            }

            // No more pages or not a large chest
            mc.player.closeScreen();
            scanning = false;
            sendLocalMessage("\u00a7a\u0420\u0435\u043f\u043e\u0440\u0442\u044b \u0437\u0430\u0433\u0440\u0443\u0436\u0435\u043d\u044b: " + reportedNicks.size() + " \u0438\u0433\u0440\u043e\u043a\u043e\u0432");
        }
    }

    private static void scanCurrentPage(MinecraftClient mc) {
        int slotCount = mc.player.currentScreenHandler.slots.size();
        int maxSlot = Math.min(45, slotCount); // slots 0-44

        for (int i = 0; i < maxSlot; i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack == null || stack.isEmpty()) continue;

            List<Text> lore = stack.getTooltip(mc.player, TooltipContext.Default.BASIC);

            String suspect = null;
            StringBuilder detail = new StringBuilder();

            for (Text t : lore) {
                String line = t.getString();

                // Find suspect nick
                Matcher suspectM = SUSPECT_PATTERN.matcher(line);
                if (suspectM.find()) {
                    suspect = suspectM.group(1);
                }

                // Collect detail info
                if (!line.isEmpty()) {
                    if (detail.length() > 0) detail.append(" ");
                    detail.append(line.trim());
                }
            }

            if (suspect != null && !suspect.isEmpty()) {
                reportedNicks.add(suspect);
                reportDetails.put(suspect, detail.toString());
            }
        }
    }

    private static void sendLocalMessage(String text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(text), false);
        }
    }

    public static Set<String> getReportedNicks() {
        return reportedNicks;
    }

    public static Map<String, String> getReportDetails() {
        return reportDetails;
    }

    public static boolean isScanning() {
        return scanning;
    }

    public static boolean hasReport(String nick) {
        return reportedNicks.contains(nick);
    }
}
