
package com.playtimechecker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class PlayTimeCheckerMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        PlayTimeConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {

            if (client.player == null) return;

            PlayTimeScanner.get().tick(client);
            CommandQueue.tick();
        });
    }
}
