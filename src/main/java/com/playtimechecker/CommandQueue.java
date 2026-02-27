
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import java.util.LinkedList;
import java.util.Queue;

public class CommandQueue {

    private static final Queue<String> queue = new LinkedList<>();
    private static int tickCounter = 0;

    public static void add(String cmd) {
        queue.add(cmd);
    }

    public static void tick() {

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        tickCounter++;

        if (tickCounter < PlayTimeConfig.get().delayTicks)
            return;

        tickCounter = 0;

        if (!queue.isEmpty()) {
            mc.player.networkHandler.sendChatCommand(queue.poll());
        }
    }
}
