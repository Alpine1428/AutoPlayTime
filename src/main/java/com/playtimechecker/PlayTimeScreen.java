
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.*;

public class PlayTimeScreen extends Screen {

    private int scroll = 0;

    public PlayTimeScreen() {
        super(Text.literal("PlayTime Checker"));
    }

    @Override
    protected void init() {

        int cx = width / 2;

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§aПроверить всех"),
                b -> PlayTimeScanner.get().start(MinecraftClient.getInstance())
        ).dimensions(cx - 150, 20, 120, 20).build());

        addDrawableChild(ButtonWidget.builder(
                Text.literal("§cСтоп"),
                b -> PlayTimeScanner.get().stop()
        ).dimensions(cx - 20, 20, 80, 20).build());
    }

    private int color(long sec) {
        if (sec < 3600) return 0xFF5555;
        if (sec < 10800) return 0xFFFF55;
        if (sec < 36000) return 0x55FF55;
        return 0x55FFFF;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {

        renderBackground(ctx);

        int y = 60;
        int cx = width / 2;

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§6§lPlayTime Checker"),
                cx, 40, 0xFFFFFF);

        if (PlayTimeScanner.get().scanning()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("§eПрогресс: "
                            + PlayTimeScanner.get().progress()
                            + "/" + PlayTimeScanner.get().total()),
                    cx, 50, 0xFFFF55);
        }

        for (PlayerData p : PlayTimeScanner.get().getSorted()) {

            ctx.drawTextWithShadow(textRenderer,
                    Text.literal(p.name + " - " + p.format()),
                    cx - 150, y, color(p.seconds));

            ctx.drawTextWithShadow(textRenderer,
                    Text.literal("§a[Вызвать]"),
                    cx + 120, y, 0x00FF00);

            y += 20;
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        int cx = width / 2;
        int y = 60;

        for (PlayerData p : PlayTimeScanner.get().getSorted()) {

            // Копирование ника
            if (mouseX >= cx - 150 && mouseX <= cx - 20 &&
                mouseY >= y && mouseY <= y + 15) {

                MinecraftClient.getInstance().keyboard.setClipboard(p.name);
                return true;
            }

            // Вызвать на проверку
            if (mouseX >= cx + 120 && mouseX <= cx + 200 &&
                mouseY >= y && mouseY <= y + 15) {

                CommandQueue.add("hm spy " + p.name);
                CommandQueue.add("find " + p.name);
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
