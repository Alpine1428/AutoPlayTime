
package com.playtimechecker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class DelaySettingsScreen extends Screen {

    private int delay;

    public DelaySettingsScreen() {
        super(Text.literal("Delay Settings"));
    }

    @Override
    protected void init() {

        delay = PlayTimeConfig.get().delayTicks;

        int cx = width / 2;
        int cy = height / 2;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("-1"),
                b -> change(-1)
        ).dimensions(cx - 100, cy, 40, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("+1"),
                b -> change(1)
        ).dimensions(cx + 60, cy, 40, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Назад"),
                b -> close()
        ).dimensions(cx - 40, cy + 40, 80, 20).build());
    }

    private void change(int delta) {
        delay += delta;
        if (delay < 1) delay = 1;
        if (delay > 200) delay = 200;

        PlayTimeConfig.get().delayTicks = delay;
        PlayTimeConfig.save();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {

        renderBackground(ctx);

        int cx = width / 2;
        int cy = height / 2;

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Задержка: " + delay + " тиков"),
                cx, cy - 20, 0xFFFFFF);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
