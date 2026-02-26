package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.*;

public class ReportManager {

    private static boolean scanning = false;
    private static int tickDelay = 0;

    public static Map<String, String> reports = new HashMap<>();

    public static void startScan() {
        reports.clear();
        scanning = true;
        MinecraftClient.getInstance().player.networkHandler.sendChatCommand("reportlist");
    }

    public static void tick() {

        if (!scanning) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (mc.player.currentScreenHandler == null) return;

        if (tickDelay++ < 5) return;
        tickDelay = 0;

        for (int i = 0; i < 27; i++) {

            ItemStack stack =
                    mc.player.currentScreenHandler.getSlot(i).getStack();

            if (!stack.isEmpty()) {

                List<Text> lore = stack.getTooltip(
                        mc.player,
                        mc.options.advancedItemTooltips
                                ? TooltipContext.Default.ADVANCED
                                : TooltipContext.Default.NORMAL
                );

                String suspect = null;
                String comment = null;

                for (Text t : lore) {

                    String line = t.getString();

                    if (line.contains("Подозреваемый:"))
                        suspect = line.replace("Подозреваемый:", "").trim();

                    if (line.contains("Комментарий:"))
                        comment = line.replace("Комментарий:", "").trim();
                }

                if (suspect != null && comment != null)
                    reports.put(suspect, comment);
            }
        }

        // Проверка последней страницы (27 слот)
        if (mc.player.currentScreenHandler.getSlot(27).getStack().isEmpty()) {
            scanning = false;
            mc.player.closeScreen();
            return;
        }

        // Листаем (звезда незера — 36 слот)
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                36,
                0,
                SlotActionType.PICKUP,
                mc.player
        );
    }

    public static boolean hasReport(String nick) {
        return reports.containsKey(nick);
    }

    public static String getReport(String nick) {
        return reports.getOrDefault(nick, "");
    }
}
