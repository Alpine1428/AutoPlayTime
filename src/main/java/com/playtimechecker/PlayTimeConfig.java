package com.playtimechecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class PlayTimeConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(
            FabricLoader.getInstance().getConfigDir().toFile(),
            "playtime-checker.json"
    );

    public int delayTicks = 5;
    public int activitySeconds = 30;

    private static PlayTimeConfig INSTANCE;

    public static PlayTimeConfig get() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static PlayTimeConfig getInstance() {
        return get();
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    public void setDelayTicks(int ticks) {
        this.delayTicks = ticks;
        save();
    }

    public static void load() {
        try {
            if (FILE.exists()) {
                try (FileReader reader = new FileReader(FILE)) {
                    INSTANCE = GSON.fromJson(reader, PlayTimeConfig.class);
                }
            } else {
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
            try (FileWriter writer = new FileWriter(FILE)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (Exception ignored) {}
    }
}
