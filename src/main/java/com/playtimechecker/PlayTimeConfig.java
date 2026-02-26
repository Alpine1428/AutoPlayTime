
package com.playtimechecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.*;

public class PlayTimeConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE =
            new File(FabricLoader.getInstance().getConfigDir().toFile(),
                    "playtime-checker.json");

    public int delayTicks = 5;
    public int activitySeconds = 30;

    private static PlayTimeConfig INSTANCE;

    public static PlayTimeConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        try {
            if (FILE.exists())
                INSTANCE = GSON.fromJson(new FileReader(FILE), PlayTimeConfig.class);
            else {
                INSTANCE = new PlayTimeConfig();
                save();
            }
        } catch (Exception e) {
            INSTANCE = new PlayTimeConfig();
        }
    }

    public static void save() {
        try {
            FILE.getParentFile().mkdirs();
            GSON.toJson(INSTANCE, new FileWriter(FILE));
        } catch (Exception ignored) {}
    }
}
