
package com.playtimechecker;

public class PlayerData implements Comparable<PlayerData> {

    public String name;
    public long totalSeconds;
    public String reportComment = null;

    public PlayerData(String name, long sec) {
        this.name = name;
        this.totalSeconds = sec;
    }

    public String format() {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return h + "ч " + m + "м " + s + "с";
    }

    @Override
    public int compareTo(PlayerData o) {
        return Long.compare(this.totalSeconds, o.totalSeconds);
    }
}
