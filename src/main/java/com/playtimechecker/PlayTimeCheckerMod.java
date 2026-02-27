
package com.playtimechecker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class PlayTimeCheckerMod implements ClientModInitializer {

    private KeyBinding open;

    @Override
    public void onInitializeClient() {

        PlayTimeConfig.load();

        open = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("Open PlayTime GUI",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_K,
                        "PlayTime Checker"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null) return;

            while (open.wasPressed())
                client.setScreen(new PlayTimeScreen());

            PlayTimeScanner.get().tick(client);
            CommandQueue.tick();
            ReportManager.tick();
        });
    }
}
