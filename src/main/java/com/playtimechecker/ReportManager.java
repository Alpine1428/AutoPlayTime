package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.*;

public class ReportManager {

    private static boolean scanning = false;
    private static boolean waitingForGui = false;
    private static boolean pageScanned = false;
    private static int tick = 0;
    private static int waitTick = 0;

    private static final Set<String> reportedNicks = new LinkedHashSet<>();

    // Key word to find in lore lines
    private static final String SUSPECT_KEY = "\u041f\u043e\u0434\u043e\u0437\u0440\u0435\u0432\u0430\u0435\u043c\u044b\u0439:";

    public static void start() {
        reportedNicks.clear();
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
                sendMsg("\u00a7c\u0422\u0430\u0439\u043c\u0430\u0443\u0442 GUI \u0440\u0435\u043f\u043e\u0440\u0442\u043e\u0432");
            }
            return;
        }

        // Check GUI still open
        if (mc.player.currentScreenHandler == null
                || mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
            scanning = false;
            return;
        }

        // Wait for items to load
        if (++tick < 10) return;

        if (!pageScanned) {
            pageScanned = true;
            scanPage(mc);
        }

        // After scan check next page
        if (tick == 12) {
            int slotCount = mc.player.currentScreenHandler.slots.size();

            // Large chest = 54 slots (0-53)
            // Reports in slots 0-44
            // If slot 44 not empty -> click slot 53 for next page
            if (slotCount >= 54) {
                ItemStack slot44 = mc.player.currentScreenHandler.getSlot(44).getStack();

                if (slot44 != null && !slot44.isEmpty()) {
                    ItemStack slot53 = mc.player.currentScreenHandler.getSlot(53).getStack();
                    if (slot53 != null && !slot53.isEmpty()) {
                        // Click next page
                        mc.interactionManager.clickSlot(
                                mc.player.currentScreenHandler.syncId,
                                53, 0, SlotActionType.PICKUP, mc.player
                        );
                        tick = 0;
                        pageScanned = false;
                        waitingForGui = true;
                        waitTick = 0;
                        return;
                    }
                }
            }

            // No more pages
            mc.player.closeScreen();
            scanning = false;
            sendMsg("\u00a7a\u0420\u0435\u043f\u043e\u0440\u0442\u044b: " + reportedNicks.size() + " \u0438\u0433\u0440\u043e\u043a\u043e\u0432");
        }
    }

    private static void scanPage(MinecraftClient mc) {
        int slotCount = mc.player.currentScreenHandler.slots.size();
        int maxSlot = Math.min(45, slotCount);

        for (int i = 0; i < maxSlot; i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack == null || stack.isEmpty()) continue;

            List<Text> lore = stack.getTooltip(mc.player, TooltipContext.Default.BASIC);

            for (Text t : lore) {
                String line = t.getString();

                // Find line containing suspect key
                int idx = line.indexOf(SUSPECT_KEY);
                if (idx >= 0) {
                    // Extract nick after key
                    String after = line.substring(idx + SUSPECT_KEY.length()).trim();
                    // Nick is the first word (before space or parenthesis)
                    String nick = after.split("[\\s(]")[0].trim();
                    if (!nick.isEmpty()) {
                        reportedNicks.add(nick);
                    }
                }
            }
        }
    }

    private static void sendMsg(String text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(text), false);
        }
    }

    public static Set<String> getReportedNicks() {
        return reportedNicks;
    }

    public static boolean isScanning() {
        return scanning;
    }

    public static boolean hasReport(String nick) {
        return reportedNicks.contains(nick);
    }
}
