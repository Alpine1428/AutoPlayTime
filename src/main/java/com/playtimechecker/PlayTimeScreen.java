
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.*;

public class PlayTimeScreen extends Screen {

    public PlayTimeScreen() {
        super(Text.literal("PlayTime Checker"));
    }

    @Override
    protected void init() {

        int cx = width / 2;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Проверить всех"),
                b -> PlayTimeScanner.get().start(MinecraftClient.getInstance())
        ).dimensions(cx - 200, 20, 120, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Проверить репорты"),
                b -> ReportManager.start()
        ).dimensions(cx - 60, 20, 150, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("Настройки"),
                b -> MinecraftClient.getInstance().setScreen(new DelaySettingsScreen())
        ).dimensions(cx + 110, 20, 120, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {

        renderBackground(ctx);

        int y = 70;
        int cx = width / 2;

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§6§lPlayTime Checker"),
                cx, 40, 0xFFFFFF);

        for (PlayerData p : PlayTimeScanner.get().getSorted()) {

            String line = p.name + " - " + p.format();

            if (ReportManager.getReports().containsKey(p.name)) {
                line += " §c[REPORT]";
            }

            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(line),
                    cx - 150, y, 0xFFFFFF);

            y += 20;
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
