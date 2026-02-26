package com.playtimechecker;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class PlayTimeSettingsScreen extends Screen {

    private int currentDelay;

    public PlayTimeSettingsScreen() {
        super(Text.literal("PlayTime Settings"));
    }

    @Override
    protected void init() {
        super.init();
        currentDelay = PlayTimeConfig.getInstance().getDelayTicks();
        int cx = this.width / 2;
        int cy = this.height / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("--10"), b -> changeDelay(-10))
                .dimensions(cx - 155, cy - 10, 40, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("-5"), b -> changeDelay(-5))
                .dimensions(cx - 110, cy - 10, 35, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("-1"), b -> changeDelay(-1))
                .dimensions(cx - 70, cy - 10, 30, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+1"), b -> changeDelay(1))
                .dimensions(cx + 40, cy - 10, 30, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+5"), b -> changeDelay(5))
                .dimensions(cx + 75, cy - 10, 35, 20).build());
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+10"), b -> changeDelay(10))
                .dimensions(cx + 115, cy - 10, 40, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
                .dimensions(cx - 50, cy + 50, 100, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7aFast (1)"), b -> {
            currentDelay = 1; PlayTimeConfig.getInstance().setDelayTicks(currentDelay);
        }).dimensions(cx - 155, cy + 25, 75, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7eNormal (3)"), b -> {
            currentDelay = 3; PlayTimeConfig.getInstance().setDelayTicks(currentDelay);
        }).dimensions(cx - 75, cy + 25, 70, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u00a76Medium (5)"), b -> {
            currentDelay = 5; PlayTimeConfig.getInstance().setDelayTicks(currentDelay);
        }).dimensions(cx, cy + 25, 75, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7cSafe (20)"), b -> {
            currentDelay = 20; PlayTimeConfig.getInstance().setDelayTicks(currentDelay);
        }).dimensions(cx + 80, cy + 25, 75, 20).build());
    }

    private void changeDelay(int amount) {
        currentDelay += amount;
        if (currentDelay < 1) currentDelay = 1;
        if (currentDelay > 200) currentDelay = 200;
        PlayTimeConfig.getInstance().setDelayTicks(currentDelay);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        int cx = this.width / 2;
        int cy = this.height / 2;

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u00a76\u00a7lPlayTime Settings"), cx, cy - 60, 0xFFFFFF);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u00a77Delay between /playtime commands (ticks)"), cx, cy - 40, 0xAAAAAA);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u00a7820 ticks = 1 second"), cx, cy - 28, 0x888888);

        String delayColor;
        if (currentDelay <= 2) delayColor = "\u00a7a";
        else if (currentDelay <= 5) delayColor = "\u00a7e";
        else if (currentDelay <= 15) delayColor = "\u00a76";
        else delayColor = "\u00a7c";

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal(delayColor + "\u00a7l" + currentDelay + " ticks"), cx, cy - 6, 0xFFFFFF);

        double seconds = currentDelay / 20.0;
        PlayTimeScanner scanner = PlayTimeScanner.getInstance();
        int totalPlayers = scanner.getScanTotal();
        if (totalPlayers == 0) totalPlayers = 100;
        double totalTime = totalPlayers * seconds;
        int totalMin = (int)(totalTime / 60);
        int totalSec = (int)(totalTime % 60);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u00a77Delay: " + String.format("%.2f", seconds) + "s"), cx, cy + 75, 0x999999);

        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("\u00a77Est. time for " + totalPlayers + " players: " + totalMin + "m " + totalSec + "s"),
                cx, cy + 88, 0x777777);

        if (currentDelay <= 2) {
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("\u00a7c\u26a0 May get kicked for spam!"), cx, cy + 105, 0xFF5555);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
