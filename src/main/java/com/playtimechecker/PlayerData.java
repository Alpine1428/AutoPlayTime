package com.playtimechecker;

public class PlayerData implements Comparable<PlayerData> {

    public String name;
    public long seconds;
    public String report = null;

    public PlayerData(String name, long sec) {
        this.name = name;
        this.seconds = sec;
    }

    public String format() {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return h + "\u0447 " + m + "\u043C " + s + "\u0441";
    }

    public int getColor() {
        long hours = seconds / 3600;
        if (hours < 1) return 0xFF5555;
        if (hours < 3) return 0xFFFF55;
        if (hours < 10) return 0x55FF55;
        return 0x55FFFF;
    }

    @Override
    public int compareTo(PlayerData o) {
        return Long.compare(this.seconds, o.seconds);
    }
}
