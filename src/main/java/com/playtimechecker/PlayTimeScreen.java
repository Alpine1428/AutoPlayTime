
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

        int cx = width / 2;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§aStart"),
                b -> PlayTimeScanner.get().start(MinecraftClient.getInstance())
        ).dimensions(cx - 150, 20, 80, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§cStop"),
                b -> PlayTimeScanner.get().stop()
        ).dimensions(cx - 60, 20, 80, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§eReports"),
                b -> {
                    showReports = !showReports;
                    if (showReports)
                        ReportManager.start();
                }
        ).dimensions(cx + 40, 20, 100, 20).build());
    }

    private void refresh() {
        sorted = new ArrayList<>(PlayTimeScanner.get().getPlaytimes().entrySet());
        sorted.sort(Comparator.comparingLong(Map.Entry::getValue));
    }

    private int getColor(long sec) {
        if (sec < 3600) return 0xFF5555;       // красный <1ч
        if (sec < 10800) return 0xFFFF55;      // жёлтый <3ч
        if (sec < 36000) return 0x55FF55;      // зелёный <10ч
        return 0x55FFFF;                       // голубой 10ч+
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

        if (PlayTimeScanner.get().isScanning()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§eScanning: "
                            + PlayTimeScanner.get().getProgress()
                            + "/" + PlayTimeScanner.get().getTotal()),
                    cx, 50, 0xFFFF55);
        }

        for (int i = scroll; i < sorted.size() && i < scroll + 12; i++) {

            Map.Entry<String, Long> e = sorted.get(i);
            String name = e.getKey();
            long sec = e.getValue();

            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(name + " - " + sec + "s"),
                    cx - 150, y,
                    getColor(sec));

            if (showReports && ReportManager.getReports().containsKey(name)) {
                ctx.drawTextWithShadow(textRenderer,
                        Text.literal("§c" + ReportManager.getReports().get(name)),
                        cx - 150, y + 10, 0xFF5555);
            }

            ctx.drawTextWithShadow(textRenderer,
                    Text.literal("§a[Проверить]"),
                    cx + 120, y, 0x00FF00);

            y += 20;
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

            if (mouseX >= cx - 150 && mouseX <= cx - 20 &&
                mouseY >= y && mouseY <= y + 15) {

                MinecraftClient.getInstance().keyboard.setClipboard(name);
                return true;
            }

            if (mouseX >= cx + 120 && mouseX <= cx + 200 &&
                mouseY >= y && mouseY <= y + 15) {

                ModerationManager.start(name);
                return true;
            }

            y += 20;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
