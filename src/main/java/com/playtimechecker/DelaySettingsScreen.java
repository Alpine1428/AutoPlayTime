package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DelaySettingsScreen extends Screen {

    private int delay;
    private int activity;

    public DelaySettingsScreen() {
        super(Text.literal("Settings"));
    }

    @Override
    protected void init() {
        delay = PlayTimeConfig.get().delayTicks;
        activity = PlayTimeConfig.get().activitySeconds;

        int cx = width / 2;
        int cy = height / 2 - 40;

        // Delay controls
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7c-5"),
                b -> changeDelay(-5)).dimensions(cx - 110, cy, 40, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7c-1"),
                b -> changeDelay(-1)).dimensions(cx - 65, cy, 40, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7a+1"),
                b -> changeDelay(1)).dimensions(cx + 25, cy, 40, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7a+5"),
                b -> changeDelay(5)).dimensions(cx + 70, cy, 40, 20).build());

        // Activity controls
        int ay = cy + 55;
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7c-10"),
                b -> changeActivity(-10)).dimensions(cx - 110, ay, 40, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7c-1"),
                b -> changeActivity(-1)).dimensions(cx - 65, ay, 40, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7a+1"),
                b -> changeActivity(1)).dimensions(cx + 25, ay, 40, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7a+10"),
                b -> changeActivity(10)).dimensions(cx + 70, ay, 40, 20).build());

        // Back button
        addDrawableChild(ButtonWidget.builder(
                Text.literal("\u00a77\u2190 \u041d\u0430\u0437\u0430\u0434"),
                b -> MinecraftClient.getInstance().setScreen(new PlayTimeScreen())
        ).dimensions(cx - 50, ay + 55, 100, 20).build());
    }

    private void changeDelay(int d) {
        delay = Math.max(1, Math.min(200, delay + d));
        PlayTimeConfig.get().delayTicks = delay;
        PlayTimeConfig.save();
    }

    private void changeActivity(int d) {
        activity = Math.max(1, Math.min(3600, activity + d));
        PlayTimeConfig.get().activitySeconds = activity;
        PlayTimeConfig.save();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);

        int cx = width / 2;
        int cy = height / 2 - 40;

        // Panel
        int pL = cx - 140;
        int pR = cx + 140;
        int pT = cy - 45;
        int pB = cy + 130;
        ctx.fill(pL, pT, pR, pB, 0xCC000000);
        // Border
        ctx.fill(pL, pT, pR, pT + 2, 0xFF4488BB);
        ctx.fill(pL, pB - 1, pR, pB, 0xFF333333);
        ctx.fill(pL, pT, pL + 1, pB, 0xFF333333);
        ctx.fill(pR - 1, pT, pR, pB, 0xFF333333);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("\u00a76\u00a7l\u2699 \u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438"),
                cx, pT + 8, 0xFFAA00);

        // Divider
        ctx.fill(pL + 10, pT + 22, pR - 10, pT + 23, 0xFF333333);

        // Delay label with value
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("\u00a7f\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430: \u00a7b" + delay + " \u00a77\u0442\u0438\u043a\u043e\u0432"),
                cx, cy - 15, 0xFFFFFF);

        // Value bar for delay
        int barL = cx - 100;
        int barR = cx + 100;
        int barY = cy + 22;
        ctx.fill(barL, barY, barR, barY + 4, 0xFF222222);
        float delayPct = (float) delay / 200f;
        ctx.fill(barL, barY, barL + (int)(200 * delayPct), barY + 4, 0xFF55FFFF);

        // Activity label
        int ay = cy + 55;
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("\u00a7f\u041f\u043e\u0440\u043e\u0433 \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u0438: \u00a7b" + activity + " \u00a77\u0441\u0435\u043a."),
                cx, ay - 15, 0xFFFFFF);

        // Value bar for activity
        int barY2 = ay + 22;
        ctx.fill(barL, barY2, barR, barY2 + 4, 0xFF222222);
        float actPct = Math.min(1f, (float) activity / 120f);
        ctx.fill(barL, barY2, barL + (int)(200 * actPct), barY2 + 4, 0xFF55FF55);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
