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
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (!stack.isEmpty()) {
                List<Text> lore = stack.getTooltip(
                        mc.player,
                        mc.options.advancedItemTooltips
                                ? TooltipContext.Default.ADVANCED
                                : TooltipContext.Default.BASIC
                );
                String suspect = null;
                String comment = null;
                for (Text t : lore) {
                    String line = t.getString();
                    if (line.contains("\u041f\u043e\u0434\u043e\u0437\u0440\u0435\u0432\u0430\u0435\u043c\u044b\u0439:"))
                        suspect = line.replace("\u041f\u043e\u0434\u043e\u0437\u0440\u0435\u0432\u0430\u0435\u043c\u044b\u0439:", "").trim();
                    if (line.contains("\u041a\u043e\u043c\u043c\u0435\u043d\u0442\u0430\u0440\u0438\u0439:"))
                        comment = line.replace("\u041a\u043e\u043c\u043c\u0435\u043d\u0442\u0430\u0440\u0438\u0439:", "").trim();
                }
                if (suspect != null && comment != null)
                    reports.put(suspect, comment);
            }
        }

        if (mc.player.currentScreenHandler.getSlot(27).getStack().isEmpty()) {
            scanning = false;
            mc.player.closeScreen();
            return;
        }

        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                36, 0, SlotActionType.PICKUP, mc.player
        );
    }

    public static boolean hasReport(String nick) { return reports.containsKey(nick); }
    public static String getReport(String nick) { return reports.getOrDefault(nick, ""); }
}
