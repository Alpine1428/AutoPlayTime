package com.playtimechecker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PlayTimeCheckerMod implements ClientModInitializer {

    public static KeyBinding scanKey;
    public static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        scanKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Scan PlayTime (All Online)",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "PlayTime Checker"
        ));

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open PlayTime Menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                "PlayTime Checker"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (scanKey.wasPressed()) {
                PlayTimeScanner.getInstance().startScan(client);
            }

            if (menuKey.wasPressed()) {
                client.setScreen(new PlayTimeScreen());
            }

            PlayTimeScanner.getInstance().tick(client);
        });
    }
}
