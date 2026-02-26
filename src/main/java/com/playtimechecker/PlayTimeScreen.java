package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayTimeScreen extends Screen {

    private List<PlayerPlayTime> sortedResults = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 24;
    private static final int PADDING_TOP = 50;
    private static final int PADDING_BOTTOM = 40;

    public PlayTimeScreen() {
        super(Text.literal("PlayTime Checker"));
    }

    @Override
    protected void init() {
        super.init();
        sortedResults = new ArrayList<>(PlayTimeScanner.getInstance().getResults());
        Collections.sort(sortedResults); // от маленького к большому
        scrollOffset = 0;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Затемнение фона
        renderBackground(context);

        int centerX = this.width / 2;

        // Заголовок
        String title = "§6§l✦ PlayTime Checker ✦";
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(title), centerX, 10, 0xFFFFFF);

        // Статус сканирования
        PlayTimeScanner scanner = PlayTimeScanner.getInstance();
        if (scanner.isScanning()) {
            String status = "§eСканирование: " + scanner.getScanProgress() + " / " + scanner.getScanTotal();
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(status), centerX, 25, 0xFFFF55);
        } else {
            String status = "§aНайдено игроков: " + sortedResults.size();
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(status), centerX, 25, 0x55FF55);
        }

        // Подсказка
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("§7Нажмите на ник чтобы скопировать"), centerX, 37, 0xAAAAAA);

        // Область отрисовки записей
        int visibleAreaTop = PADDING_TOP;
        int visibleAreaBottom = this.height - PADDING_BOTTOM;
        int visibleCount = (visibleAreaBottom - visibleAreaTop) / ENTRY_HEIGHT;

        // Ограничиваем scrollOffset
        int maxScroll = Math.max(0, sortedResults.size() - visibleCount);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;

        // Рисуем записи
        int boxWidth = 340;
        int boxLeft = centerX - boxWidth / 2;
        int boxRight = centerX + boxWidth / 2;

        for (int i = 0; i < visibleCount && (i + scrollOffset) < sortedResults.size(); i++) {
            int index = i + scrollOffset;
            PlayerPlayTime ppt = sortedResults.get(index);
            int y = visibleAreaTop + i * ENTRY_HEIGHT;

            boolean hovered = mouseX >= boxLeft && mouseX <= boxRight && mouseY >= y && mouseY < y + ENTRY_HEIGHT - 2;

            // Фон записи
            int bgColor = hovered ? 0x80555555 : 0x80333333;
            context.fill(boxLeft, y, boxRight, y + ENTRY_HEIGHT - 2, bgColor);

            // Рамка при наведении
            if (hovered) {
                // верх
                context.fill(boxLeft, y, boxRight, y + 1, 0xFFFFAA00);
                // низ
                context.fill(boxLeft, y + ENTRY_HEIGHT - 3, boxRight, y + ENTRY_HEIGHT - 2, 0xFFFFAA00);
                // лево
                context.fill(boxLeft, y, boxLeft + 1, y + ENTRY_HEIGHT - 2, 0xFFFFAA00);
                // право
                context.fill(boxRight - 1, y, boxRight, y + ENTRY_HEIGHT - 2, 0xFFFFAA00);
            }

            // Номер
            String numberStr = "§7" + (index + 1) + ". ";

            // Цвет ника в зависимости от плейтайма
            String nameColor;
            if (ppt.getTotalSeconds() < 3600) {
                nameColor = "§c"; // красный - менее часа
            } else if (ppt.getTotalSeconds() < 10800) {
                nameColor = "§e"; // жёлтый - менее 3 часов
            } else if (ppt.getTotalSeconds() < 36000) {
                nameColor = "§a"; // зелёный - менее 10 часов
            } else {
                nameColor = "§b"; // голубой - 10+ часов
            }

            String entryText = numberStr + nameColor + ppt.getName() + " §8| §f" + ppt.getTotalTimeFormatted();

            context.drawTextWithShadow(this.textRenderer, Text.literal(entryText), boxLeft + 5, y + 5, 0xFFFFFF);
        }

        // Скроллбар
        if (sortedResults.size() > visibleCount) {
            int scrollBarHeight = Math.max(20, (int) ((float) visibleCount / sortedResults.size() * (visibleAreaBottom - visibleAreaTop)));
            int scrollBarY = visibleAreaTop + (int) ((float) scrollOffset / maxScroll * (visibleAreaBottom - visibleAreaTop - scrollBarHeight));
            context.fill(boxRight + 3, scrollBarY, boxRight + 6, scrollBarY + scrollBarHeight, 0xAAFFAA00);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int boxWidth = 340;
            int boxLeft = centerX - boxWidth / 2;
            int boxRight = centerX + boxWidth / 2;
            int visibleAreaTop = PADDING_TOP;
            int visibleAreaBottom = this.height - PADDING_BOTTOM;
            int visibleCount = (visibleAreaBottom - visibleAreaTop) / ENTRY_HEIGHT;

            if (mouseX >= boxLeft && mouseX <= boxRight) {
                for (int i = 0; i < visibleCount && (i + scrollOffset) < sortedResults.size(); i++) {
                    int y = visibleAreaTop + i * ENTRY_HEIGHT;
                    if (mouseY >= y && mouseY < y + ENTRY_HEIGHT - 2) {
                        int index = i + scrollOffset;
                        PlayerPlayTime ppt = sortedResults.get(index);
                        // Копируем ник в буфер обмена
                        MinecraftClient.getInstance().keyboard.setClipboard(ppt.getName());
                        if (MinecraftClient.getInstance().player != null) {
                            MinecraftClient.getInstance().player.sendMessage(
                                    Text.literal("§e[PlayTime] §aНик §f" + ppt.getName() + " §aскопирован!"), false
                            );
                        }
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        scrollOffset -= (int) amount * 3;
        int visibleAreaTop = PADDING_TOP;
        int visibleAreaBottom = this.height - PADDING_BOTTOM;
        int visibleCount = (visibleAreaBottom - visibleAreaTop) / ENTRY_HEIGHT;
        int maxScroll = Math.max(0, sortedResults.size() - visibleCount);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
        if (scrollOffset < 0) scrollOffset = 0;
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
