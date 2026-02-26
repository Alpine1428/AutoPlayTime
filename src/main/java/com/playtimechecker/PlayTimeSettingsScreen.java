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

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Кнопка -10
        this.addDrawableChild(ButtonWidget.builder(Text.literal("--10"), button -> {
            changeDelay(-10);
        }).dimensions(centerX - 155, centerY - 10, 40, 20).build());

        // Кнопка -5
        this.addDrawableChild(ButtonWidget.builder(Text.literal("-5"), button -> {
            changeDelay(-5);
        }).dimensions(centerX - 110, centerY - 10, 35, 20).build());

        // Кнопка -1
        this.addDrawableChild(ButtonWidget.builder(Text.literal("-1"), button -> {
            changeDelay(-1);
        }).dimensions(centerX - 70, centerY - 10, 30, 20).build());

        // Кнопка +1
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+1"), button -> {
            changeDelay(1);
        }).dimensions(centerX + 40, centerY - 10, 30, 20).build());

        // Кнопка +5
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+5"), button -> {
            changeDelay(5);
        }).dimensions(centerX + 75, centerY - 10, 35, 20).build());

        // Кнопка +10
        this.addDrawableChild(ButtonWidget.builder(Text.literal("+10"), button -> {
            changeDelay(10);
        }).dimensions(centerX + 115, centerY - 10, 40, 20).build());

        // Кнопка "Готово"
        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u0413\u043e\u0442\u043e\u0432\u043e"), button -> {
            close();
        }).dimensions(centerX - 50, centerY + 50, 100, 20).build());

        // Пресеты
        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7aBezDelay (1)"), button -> {
            currentDelay = 1;
            PlayTimeConfig.getInstance().setDelayTicks(currentDelay);
        }).dimensions(centerX - 155, centerY + 25, 75, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7eFast (3)"), button -> {
            currentDelay = 3;
            PlayTimeConfig.getInstance().setDelayTicks(currentDelay);
        }).dimensions(centerX - 75, centerY + 25, 70, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u00a76Normal (5)"), button -> {
            currentDelay = 5;
            PlayTimeConfig.getInstance().setDelayTicks(currentDelay);
        }).dimensions(centerX, centerY + 25, 75, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("\u00a7cSafe (20)"), button -> {
            currentDelay = 20;
            PlayTimeConfig.getInstance().setDelayTicks(currentDelay);
        }).dimensions(centerX + 80, centerY + 25, 75, 20).build());
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

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Заголовок
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("\u00a76\u00a7l\u2699 PlayTime Settings \u2699"),
            centerX, centerY - 60, 0xFFFFFF
        );

        // Описание
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("\u00a77Delay \u043c\u0435\u0436\u0434\u0443 \u043a\u043e\u043c\u0430\u043d\u0434\u0430\u043c\u0438 /playtime (\u0432 \u0442\u0438\u043a\u0430\u0445)"),
            centerX, centerY - 40, 0xAAAAAA
        );

        // Подсказка
        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("\u00a7820 \u0442\u0438\u043a\u043e\u0432 = 1 \u0441\u0435\u043a\u0443\u043d\u0434\u0430"),
            centerX, centerY - 28, 0x888888
        );

        // Текущее значение
        String delayColor;
        if (currentDelay <= 2) {
            delayColor = "\u00a7a";
        } else if (currentDelay <= 5) {
            delayColor = "\u00a7e";
        } else if (currentDelay <= 15) {
            delayColor = "\u00a76";
        } else {
            delayColor = "\u00a7c";
        }

        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal(delayColor + "\u00a7l" + currentDelay + " ticks"),
            centerX, centerY - 6, 0xFFFFFF
        );

        // Расчёт времени
        double seconds = currentDelay / 20.0;
        PlayTimeScanner scanner = PlayTimeScanner.getInstance();
        int totalPlayers = scanner.getScanTotal();
        if (totalPlayers == 0) totalPlayers = 100;

        double totalTime = totalPlayers * seconds;
        int totalMin = (int) (totalTime / 60);
        int totalSec = (int) (totalTime % 60);

        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("\u00a77\u0417\u0430\u0434\u0435\u0440\u0436\u043a\u0430: " + String.format("%.2f", seconds) + " \u0441\u0435\u043a"),
            centerX, centerY + 75, 0x999999
        );

        context.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("\u00a77\u041f\u0440\u0438\u043c\u0435\u0440\u043d\u043e\u0435 \u0432\u0440\u0435\u043c\u044f \u043d\u0430 " + totalPlayers + " \u0438\u0433\u0440\u043e\u043a\u043e\u0432: " + totalMin + "\u043c " + totalSec + "\u0441"),
            centerX, centerY + 88, 0x777777
        );

        // Предупреждение при низком delay
        if (currentDelay <= 2) {
            context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("\u00a7c\u26a0 \u041c\u043e\u0436\u0435\u0442 \u043a\u0438\u043a\u043d\u0443\u0442\u044c \u0437\u0430 \u0441\u043f\u0430\u043c!"),
                centerX, centerY + 105, 0xFF5555
            );
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
