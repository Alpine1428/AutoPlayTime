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
    private static int tick = 0;
    private static int waitTick = 0;

    private static final Map<String, String> reports = new LinkedHashMap<>();

    public static void start() {
        reports.clear();
        scanning = true;
        waitingForGui = true;
        waitTick = 0;
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

        if (waitingForGui) {
            waitTick++;
            if (mc.player.currentScreenHandler != null
                    && mc.player.currentScreenHandler != mc.player.playerScreenHandler) {
                waitingForGui = false;
                tick = 0;
            }
            if (waitTick > 100) {
                scanning = false;
                waitingForGui = false;
            }
            return;
        }

        if (mc.player.currentScreenHandler == null
                || mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
            scanning = false;
            return;
        }

        if (++tick < 5) return;
        tick = 0;

        int slotCount = mc.player.currentScreenHandler.slots.size();
        int containerSize = Math.min(27, slotCount);

        for (int i = 0; i < containerSize; i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (stack == null || stack.isEmpty()) continue;

            List<Text> lore = stack.getTooltip(mc.player, TooltipContext.Default.BASIC);

            String suspect = null;
            String comment = null;

            for (Text t : lore) {
                String line = t.getString();
                if (line.contains("\u041f\u043e\u0434\u043e\u0437\u0440\u0435\u0432\u0430\u0435\u043c\u044b\u0439:"))
                    suspect = line.replaceAll(".*\u041f\u043e\u0434\u043e\u0437\u0440\u0435\u0432\u0430\u0435\u043c\u044b\u0439:\\s*", "").trim();
                if (line.contains("\u041a\u043e\u043c\u043c\u0435\u043d\u0442\u0430\u0440\u0438\u0439:"))
                    comment = line.replaceAll(".*\u041a\u043e\u043c\u043c\u0435\u043d\u0442\u0430\u0440\u0438\u0439:\\s*", "").trim();
            }

            if (suspect != null && comment != null)
                reports.put(suspect, comment);
        }

        if (slotCount > 27) {
            ItemStack nextPage = mc.player.currentScreenHandler.getSlot(slotCount - 9).getStack();
            if (nextPage == null || nextPage.isEmpty()) {
                mc.player.closeScreen();
                scanning = false;
                return;
            }

            mc.interactionManager.clickSlot(
                    mc.player.currentScreenHandler.syncId,
                    slotCount - 9, 0, SlotActionType.PICKUP, mc.player
            );
            tick = 0;
        } else {
            mc.player.closeScreen();
            scanning = false;
        }
    }

    public static Map<String, String> getReports() {
        return reports;
    }

    public static boolean isScanning() {
        return scanning;
    }
}
