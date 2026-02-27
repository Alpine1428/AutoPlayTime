
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;

public class ModerationManager {

    public static void start(String nick) {

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        CommandQueue.add("hm spy " + nick);
        CommandQueue.add("find " + nick);
    }
}
