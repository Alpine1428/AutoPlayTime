
package com.playtimechecker;

import net.minecraft.client.MinecraftClient;
import java.util.*;

public class CommandQueue {

    private static final Queue<String> queue = new LinkedList<>();
    private static int tick = 0;

    public static void add(String cmd) {
        queue.add(cmd);
    }

    public static void tick() {

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        tick++;

        if (tick < PlayTimeConfig.get().delayTicks)
            return;

        tick = 0;

        if (!queue.isEmpty())
            mc.player.networkHandler.sendChatCommand(queue.poll());
    }
}
