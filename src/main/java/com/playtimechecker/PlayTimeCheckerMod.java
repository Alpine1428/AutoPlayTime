package com.playtimechecker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PlayTimeCheckerMod implements ClientModInitializer {

    public static KeyBinding scanKey;
    public static KeyBinding menuKey;
    public static KeyBinding settingsKey;

    @Override
    public void onInitializeClient() {

        // Загружаем конфиг
        PlayTimeConfig.load();

        // ===== Клавиша J — начать сканирование =====
        scanKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Scan PlayTime (J)",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "PlayTime Checker"
        ));

        // ===== Клавиша K — открыть меню =====
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open PlayTime Menu (K)",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "PlayTime Checker"
        ));

        // ===== Клавиша L — настройки =====
        settingsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open Settings (L)",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                "PlayTime Checker"
        ));

        // ===== Основной тик клиента =====
        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null) return;

            // J — начать сканирование
            while (scanKey.wasPressed()) {
                PlayTimeScanner.getInstance().startScan(client);
            }

            // K — открыть меню результатов
            while (menuKey.wasPressed()) {
                client.setScreen(new PlayTimeScreen());
            }

            // L — открыть настройки
            while (settingsKey.wasPressed()) {
                client.setScreen(new PlayTimeSettingsScreen());
            }

            // ===== Тики логики =====
            PlayTimeScanner.getInstance().tick(client);
            ReportManager.tick();
        });
    }
}
