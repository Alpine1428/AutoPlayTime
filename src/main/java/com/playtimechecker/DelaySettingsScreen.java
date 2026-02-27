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
        int cy = height / 2 - 30;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("-1"),
                b -> changeDelay(-1)
        ).dimensions(cx - 100, cy, 40, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("+1"),
                b -> changeDelay(1)
        ).dimensions(cx + 60, cy, 40, 20).build());

        int ay = cy + 50;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("-5"),
                b -> changeActivity(-5)
        ).dimensions(cx - 100, ay, 40, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("+5"),
                b -> changeActivity(5)
        ).dimensions(cx + 60, ay, 40, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("\u041d\u0430\u0437\u0430\u0434"),
                b -> MinecraftClient.getInstance().setScreen(new PlayTimeScreen())
        ).dimensions(cx - 40, ay + 50, 80, 20).build());
    }

    private void changeDelay(int delta) {
        delay += delta;
        if (delay < 1) delay = 1;
        if (delay > 200) delay = 200;

        PlayTimeConfig.get().delayTicks = delay;
        PlayTimeConfig.save();
    }

    private void changeActivity(int delta) {
        activity += delta;
        if (activity < 1) activity = 1;
        if (activity > 3600) activity = 3600;

        PlayTimeConfig.get().activitySeconds = activity;
        PlayTimeConfig.save();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx);

        int cx = width / 2;
        int cy = height / 2 - 30;

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("\u00a76\u00a7l\u041d\u0430\u0441\u0442\u0440\u043e\u0439\u043a\u0438"),
                cx, cy - 30, 0xFFFFFF);

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430: " + delay + " \u0442\u0438\u043a\u043e\u0432"),
                cx, cy - 15, 0xFFFFFF);

        int ay = cy + 50;

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("\u041f\u043e\u0440\u043e\u0433 \u0430\u043a\u0442\u0438\u0432\u043d\u043e\u0441\u0442\u0438: " + activity + " \u0441\u0435\u043a."),
                cx, ay - 15, 0xFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
