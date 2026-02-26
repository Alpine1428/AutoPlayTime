
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.*;

public class PlayTimeScreen extends Screen {

    private List<Map.Entry<String, Long>> sorted = new ArrayList<>();
    private int scroll = 0;
    private boolean showReports = false;

    public PlayTimeScreen() {
        super(Text.literal("PlayTime Checker"));
    }

    @Override
    protected void init() {
        super.init();

        refresh();

        int cx = width / 2;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§aStart Scan"),
                b -> PlayTimeScanner.get().start(MinecraftClient.getInstance())
        ).dimensions(cx - 150, 20, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§cStop"),
                b -> PlayTimeScanner.get().stop()
        ).dimensions(cx - 40, 20, 80, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§eReports"),
                b -> {
                    showReports = !showReports;
                    if (showReports)
                        ReportManager.start();
                }
        ).dimensions(cx + 50, 20, 100, 20).build());
    }

    private void refresh() {
        sorted = new ArrayList<>(PlayTimeScanner.get().getPlaytimes().entrySet());
        sorted.sort(Comparator.comparingLong(Map.Entry::getValue));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);
        refresh();

        int y = 60;
        int cx = width / 2;

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§6§lPlayTime Checker"),
                cx, 40, 0xFFFFFF);

        for (int i = scroll; i < sorted.size() && i < scroll + 12; i++) {

            Map.Entry<String, Long> e = sorted.get(i);
            String name = e.getKey();
            long time = e.getValue();

            String line = name + " §7- §f" + time + "s";

            if (showReports && ReportManager.getReports().containsKey(name)) {
                line += " §c[REPORT]";
            }

            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(line),
                    cx - 150, y, 0xFFFFFF);

            // Кнопка вызова
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal("§a[Проверить]"),
                    cx + 120, y, 0x00FF00);

            y += 15;
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        int cx = width / 2;
        int y = 60;

        for (int i = scroll; i < sorted.size() && i < scroll + 12; i++) {

            Map.Entry<String, Long> e = sorted.get(i);
            String name = e.getKey();

            // Проверка клика по нику (копирование)
            if (mouseX >= cx - 150 && mouseX <= cx - 20 &&
                mouseY >= y && mouseY <= y + 12) {

                MinecraftClient.getInstance().keyboard.setClipboard(name);
                return true;
            }

            // Проверка кнопки "Проверить"
            if (mouseX >= cx + 120 && mouseX <= cx + 200 &&
                mouseY >= y && mouseY <= y + 12) {

                ModerationManager.start(name);
                return true;
            }

            y += 15;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scroll -= amount;
        if (scroll < 0) scroll = 0;
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
