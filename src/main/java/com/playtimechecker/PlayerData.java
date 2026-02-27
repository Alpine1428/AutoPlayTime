
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
        return h + "ч " + m + "м " + s + "с";
    }

    @Override
    public int compareTo(PlayerData o) {
        return Long.compare(this.seconds, o.seconds);
    }
}
