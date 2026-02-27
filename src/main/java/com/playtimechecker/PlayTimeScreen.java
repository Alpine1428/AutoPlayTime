package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PlayTimeScreen extends Screen {

    private int scrollOffset = 0;
    private final List<PlayerEntry> entries = new ArrayList<>();
    private int animTick = 0;

    public PlayTimeScreen() {
        super(Text.literal("PlayTime Checker"));
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int btnY = 8;
        int btnH = 20;
        int gap = 4;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("\u00a7a\u25b6 \u0421\u043a\u0430\u043d"),
                b -> PlayTimeScanner.get().start(MinecraftClient.getInstance())
        ).dimensions(cx - 200, btnY, 90, btnH).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("\u00a7c\u25a0 \u0421\u0442\u043e\u043f"),
                b -> {
                    PlayTimeScanner.get().stop();
                    ModerationHandler.stop();
                    ReportManager.stop();
                }
        ).dimensions(cx - 105, btnY, 70, btnH).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("\u00a7e\u2691 \u0420\u0435\u043f\u043e\u0440\u0442\u044b"),
                b -> {
                    MinecraftClient.getInstance().setScreen(null);
                    ReportManager.start();
                }
        ).dimensions(cx - 30, btnY, 90, btnH).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("\u00a77\u2699 \u041d\u0430\u0441\u0442\u0440."),
                b -> MinecraftClient.getInstance().setScreen(new DelaySettingsScreen())
        ).dimensions(cx + 65, btnY, 80, btnH).build());
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
        animTick++;

        int cx = width / 2;
        int panelLeft = cx - 210;
        int panelRight = cx + 210;
        int panelTop = 32;
        int panelBottom = height - 8;

        // Dark panel background
        ctx.fill(panelLeft, panelTop, panelRight, panelBottom, 0xCC000000);

        // Border
        drawBorder(ctx, panelLeft, panelTop, panelRight, panelBottom, 0xFF444444);

        // Top accent line (animated gradient)
        int accentColor = getAnimatedColor();
        ctx.fill(panelLeft + 1, panelTop, panelRight - 1, panelTop + 2, accentColor);

        int y = panelTop + 8;

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("\u00a76\u00a7l\u2605 PlayTime Checker \u2605"), cx, y, 0xFFAA00);
        y += 14;

        // Divider
        ctx.fill(panelLeft + 10, y, panelRight - 10, y + 1, 0xFF333333);
        y += 6;

        // Status lines
        PlayTimeScanner scanner = PlayTimeScanner.get();
        if (scanner.scanning()) {
            int progress = scanner.progress();
            int total = scanner.total();
            float pct = total > 0 ? (float) progress / total : 0;

            // Progress bar
            int barLeft = panelLeft + 20;
            int barRight = panelRight - 20;
            int barWidth = barRight - barLeft;
            ctx.fill(barLeft, y, barRight, y + 10, 0xFF222222);
            ctx.fill(barLeft, y, barLeft + (int)(barWidth * pct), y + 10, 0xFF55FF55);
            drawBorder(ctx, barLeft, y, barRight, y + 10, 0xFF444444);

            String pctText = progress + "/" + total + " (" + (int)(pct * 100) + "%)";
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(pctText), cx, y + 1, 0xFFFFFF);
            y += 14;
        }

        if (ReportManager.isScanning()) {
            // Animated dots
            String dots = "";
            int d = (animTick / 10) % 4;
            for (int i = 0; i < d; i++) dots += ".";
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("\u00a7e\u2691 \u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430 \u0440\u0435\u043f\u043e\u0440\u0442\u043e\u0432" + dots),
                    cx, y, 0xFFFF55);
            y += 12;
        }

        if (ModerationHandler.isActive()) {
            // Blinking indicator
            String prefix = (animTick / 15 % 2 == 0) ? "\u00a7b\u25cf" : "\u00a73\u25cf";
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(prefix + " \u00a7b\u041c\u043e\u0434\u0435\u0440\u0430\u0446\u0438\u044f: \u00a7f"
                            + ModerationHandler.getTargetNick()),
                    cx, y, 0xFFFFFF);
            y += 12;
        }

        Set<String> reported = ReportManager.getReportedNicks();
        if (!reported.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("\u00a7c\u2620 \u0420\u0435\u043f\u043e\u0440\u0442\u043e\u0432: " + reported.size()),
                    cx, y, 0xFF5555);
            y += 12;
        }

        // Divider
        ctx.fill(panelLeft + 10, y, panelRight - 10, y + 1, 0xFF333333);
        y += 4;

        // Column headers
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("\u00a78\u041d\u0438\u043a"), panelLeft + 15, y, 0x888888);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("\u00a78\u0412\u0440\u0435\u043c\u044f"), cx - 30, y, 0x888888);
        ctx.drawTextWithShadow(textRenderer,
                Text.literal("\u00a78\u0414\u0435\u0439\u0441\u0442\u0432\u0438\u044f"), cx + 50, y, 0x888888);
        y += 11;

        ctx.fill(panelLeft + 10, y, panelRight - 10, y + 1, 0xFF222222);
        y += 3;

        // Player list
        rebuildEntries();

        int rowHeight = 16;
        int listBottom = panelBottom - 5;
        int maxVisible = (listBottom - y) / rowHeight;
        int start = scrollOffset;
        int end = Math.min(entries.size(), start + maxVisible);

        for (int i = start; i < end; i++) {
            PlayerEntry entry = entries.get(i);
            PlayerData p = entry.data;

            boolean hovered = mouseY >= y && mouseY < y + rowHeight
                    && mouseX >= panelLeft + 5 && mouseX <= panelRight - 5;

            // Row background (alternating + hover)
            int rowBg = (i % 2 == 0) ? 0x20FFFFFF : 0x10FFFFFF;
            if (hovered) rowBg = 0x40FFFFFF;
            ctx.fill(panelLeft + 5, y, panelRight - 5, y + rowHeight, rowBg);

            // Color indicator bar
            ctx.fill(panelLeft + 5, y, panelLeft + 8, y + rowHeight, p.getColor() | 0xFF000000);

            // Nick
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(p.name), panelLeft + 15, y + 4, p.getColor());

            // Time
            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(p.format()), cx - 30, y + 4, 0xCCCCCC);

            // Check button
            int btnX = cx + 50;
            int btnW = 70;
            boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW
                    && mouseY >= y + 1 && mouseY < y + rowHeight - 1;
            int btnBg = btnHover ? 0xFF225588 : 0xFF1A3A5C;
            ctx.fill(btnX, y + 1, btnX + btnW, y + rowHeight - 1, btnBg);
            drawBorder(ctx, btnX, y + 1, btnX + btnW, y + rowHeight - 1, 0xFF3388BB);
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("\u00a7b\u25b6 \u041f\u0440\u043e\u0432."),
                    btnX + btnW / 2, y + 4, 0x55FFFF);

            // Report marker
            if (ReportManager.hasReport(p.name)) {
                int repX = btnX + btnW + 5;
                int repW = 55;
                // Pulsing red background
                int pulse = (int)(Math.abs(Math.sin(animTick * 0.1)) * 40) + 30;
                ctx.fill(repX, y + 1, repX + repW, y + rowHeight - 1, (pulse << 24) | 0xFF0000);
                drawBorder(ctx, repX, y + 1, repX + repW, y + rowHeight - 1, 0xFFFF3333);
                ctx.drawCenteredTextWithShadow(textRenderer,
                        Text.literal("\u00a7c\u00a7lREPORT"),
                        repX + repW / 2, y + 4, 0xFF5555);
            }

            entry.y = y;
            y += rowHeight;
        }

        // Scrollbar
        if (entries.size() > maxVisible && maxVisible > 0) {
            int sbX = panelRight - 4;
            int sbTop = panelTop + 80;
            int sbBottom = panelBottom - 5;
            int sbHeight = sbBottom - sbTop;
            ctx.fill(sbX, sbTop, sbX + 3, sbBottom, 0xFF222222);

            float ratio = (float) maxVisible / entries.size();
            int thumbH = Math.max(10, (int)(sbHeight * ratio));
            float scrollRatio = (float) scrollOffset / Math.max(1, entries.size() - maxVisible);
            int thumbY = sbTop + (int)((sbHeight - thumbH) * scrollRatio);
            ctx.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, 0xFF666666);
        }

        // Empty state
        if (entries.isEmpty() && !scanner.scanning()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("\u00a78\u041d\u0430\u0436\u043c\u0438\u0442\u0435 \u0421\u043a\u0430\u043d \u0434\u043b\u044f \u043d\u0430\u0447\u0430\u043b\u0430"),
                    cx, height / 2, 0x888888);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawBorder(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1, y1, x2, y1 + 1, color);
        ctx.fill(x1, y2 - 1, x2, y2, color);
        ctx.fill(x1, y1, x1 + 1, y2, color);
        ctx.fill(x2 - 1, y1, x2, y2, color);
    }

    private int getAnimatedColor() {
        double t = animTick * 0.05;
        int r = (int)(Math.sin(t) * 40 + 80);
        int g = (int)(Math.sin(t + 2) * 40 + 120);
        int b = (int)(Math.sin(t + 4) * 40 + 200);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int cx = width / 2;
        int panelLeft = cx - 210;

        for (PlayerEntry entry : entries) {
            if (entry.y < 0) continue;

            // Click nick -> copy
            if (mouseX >= panelLeft + 10 && mouseX < cx + 45
                    && mouseY >= entry.y && mouseY < entry.y + 16) {
                MinecraftClient.getInstance().keyboard.setClipboard(entry.data.name);
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null) {
                    mc.player.sendMessage(
                            Text.literal("\u00a7a\u2714 " + entry.data.name + " \u0441\u043a\u043e\u043f\u0438\u0440\u043e\u0432\u0430\u043d"), false);
                }
                return true;
            }

            // Click check button
            int btnX = cx + 50;
            int btnW = 70;
            if (mouseX >= btnX && mouseX < btnX + btnW
                    && mouseY >= entry.y + 1 && mouseY < entry.y + 15) {
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
        PlayerEntry(PlayerData data) { this.data = data; }
    }
}
