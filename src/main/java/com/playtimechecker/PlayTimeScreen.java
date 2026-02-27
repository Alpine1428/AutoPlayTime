package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayTimeScreen extends Screen {

    private int scrollOffset = 0;
    private List<PlayerEntry> entries = new ArrayList<>();

    public PlayTimeScreen() {
        super(Text.literal("PlayTime Checker"));
    }

    @Override
    protected void init() {
        int cx = width / 2;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("\u041f\u0440\u043e\u0432\u0435\u0440\u0438\u0442\u044c \u0432\u0441\u0435\u0445"),
                b -> PlayTimeScanner.get().start(MinecraftClient.getInstance())
        ).dimensions(cx - 230, 20, 110, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("\u0421\u0442\u043e\u043f"),
                b -> {
                    PlayTimeScanner.get().stop();
                    ModerationHandler.stop();
                    ReportManager.stop();
                }
        ).dimensions(cx - 110, 20, 50, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("\u0420\u0435\u043f\u043e\u0440\u0442\u044b"),
                b -> ReportManager.start()
        ).dimensions(cx - 50, 20, 80, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438"),
                b -> MinecraftClient.getInstance().setScreen(new DelaySettingsScreen())
        ).dimensions(cx + 40, 20, 80, 20).build());
    }

    private void rebuildEntries() {
        entries.clear();
        for (PlayerData p : PlayTimeScanner.get().getSorted()) {
            entries.add(new PlayerEntry(p));
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);

        int cx = width / 2;
        int y = 50;

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("\u00a76\u00a7lPlayTime Checker"), cx, y, 0xFFFFFF);
        y += 15;

        PlayTimeScanner scanner = PlayTimeScanner.get();
        if (scanner.scanning()) {
            String progress = "\u0421\u043a\u0430\u043d: "
                    + scanner.progress() + "/" + scanner.total();
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("\u00a7e" + progress), cx, y, 0xFFFFFF);
            y += 12;
        }

        if (ReportManager.isScanning()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("\u00a7e\u0420\u0435\u043f\u043e\u0440\u0442\u044b..."), cx, y, 0xFFFFFF);
            y += 12;
        }

        if (ModerationHandler.isActive()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("\u00a7b\u041c\u043e\u0434\u0435\u0440\u0430\u0446\u0438\u044f: "
                            + ModerationHandler.getTargetNick()),
                    cx, y, 0xFFFFFF);
            y += 12;
        }

        y += 5;

        rebuildEntries();

        Map<String, String> reports = ReportManager.getReports();
        int rowHeight = 14;
        int maxVisible = (height - y - 10) / rowHeight;
        int start = scrollOffset;
        int end = Math.min(entries.size(), start + maxVisible);

        for (int i = start; i < end; i++) {
            PlayerEntry entry = entries.get(i);
            PlayerData p = entry.data;
            int color = p.getColor();

            String info = p.name + " - " + p.format();
            ctx.drawTextWithShadow(textRenderer, Text.literal(info),
                    cx - 200, y, color);

            int btnX = cx + 50;
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal("\u00a7b[\u041f\u0440\u043e\u0432\u0435\u0440\u043a\u0430]"),
                    btnX, y, 0x55FFFF);

            if (reports.containsKey(p.name)) {
                ctx.drawTextWithShadow(textRenderer,
                        Text.literal("\u00a7c[REPORT] \u00a77" + reports.get(p.name)),
                        btnX + 70, y, 0xAAAAAA);
            }

            entry.y = y;
            y += rowHeight;
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int cx = width / 2;

        for (PlayerEntry entry : entries) {
            if (entry.y < 0) continue;

            if (mouseX >= cx - 200 && mouseX < cx + 45
                    && mouseY >= entry.y && mouseY < entry.y + 12) {
                MinecraftClient.getInstance().keyboard.setClipboard(entry.data.name);
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null) {
                    mc.player.sendMessage(
                            Text.literal("\u00a7a\u041d\u0438\u043a \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d: " + entry.data.name), false);
                }
                return true;
            }

            int btnX = cx + 50;
            if (mouseX >= btnX && mouseX < btnX + 65
                    && mouseY >= entry.y && mouseY < entry.y + 12) {
                ModerationHandler.startModeration(entry.data.name);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scrollOffset -= (int) amount;
        if (scrollOffset < 0) scrollOffset = 0;

        int maxScroll = Math.max(0, entries.size() - 10);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;

        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class PlayerEntry {
        PlayerData data;
        int y = -1;

        PlayerEntry(PlayerData data) {
            this.data = data;
        }
    }
}
