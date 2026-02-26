package com.playtimechecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class PlayTimeConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
        FabricLoader.getInstance().getConfigDir().toFile(),
        "playtime-checker.json"
    );

    private static PlayTimeConfig INSTANCE;

    private int delayTicks = 5;

    public static PlayTimeConfig getInstance() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public int getDelayTicks() {
        return delayTicks;
    }

    public void setDelayTicks(int delayTicks) {
        if (delayTicks < 1) delayTicks = 1;
        if (delayTicks > 200) delayTicks = 200;
        this.delayTicks = delayTicks;
        save();
    }

    public static void load() {
        try {
            if (CONFIG_FILE.exists()) {
                FileReader reader = new FileReader(CONFIG_FILE);
                INSTANCE = GSON.fromJson(reader, PlayTimeConfig.class);
                reader.close();
                if (INSTANCE == null) {
                    INSTANCE = new PlayTimeConfig();
                }
            } else {
                INSTANCE = new PlayTimeConfig();
                save();
            }
        } catch (Exception e) {
            INSTANCE = new PlayTimeConfig();
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(CONFIG_FILE);
            GSON.toJson(INSTANCE, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
