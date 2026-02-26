
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import java.util.*;

public class ReportManager {

    public enum State { IDLE, OPENING, PARSING }
    private static State state = State.IDLE;
    private static int tick = 0;

    private static final Map<String, String> reports = new HashMap<>();

    public static void start() {
        reports.clear();
        state = State.OPENING;
        MinecraftClient.getInstance().player.networkHandler.sendChatCommand("reportlist");
    }

    public static void tick() {
        if (state == State.IDLE) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.player.currentScreenHandler == null) return;

        if (++tick < 5) return;
        tick = 0;

        state = State.PARSING;

        for (int i = 0; i < 27; i++) {
            ItemStack stack = mc.player.currentScreenHandler.getSlot(i).getStack();
            if (!stack.isEmpty()) {

                List<Text> lore = stack.getTooltip(mc.player, null);

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

        // Проверка последней страницы
        if (mc.player.currentScreenHandler.getSlot(27).getStack().isEmpty()) {
            mc.player.closeScreen();
            state = State.IDLE;
            return;
        }

        // Листаем звезду незера (36 слот)
        mc.interactionManager.clickSlot(
                mc.player.currentScreenHandler.syncId,
                36, 0, SlotActionType.PICKUP, mc.player
        );
    }

    public static Map<String, String> getReports() {
        return reports;
    }
}
