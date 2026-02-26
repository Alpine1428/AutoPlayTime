package com.playtimechecker;

public class PlayerPlayTime implements Comparable<PlayerPlayTime> {
    private final String name;
    private final String totalTimeFormatted;
    private final long totalSeconds;

    public PlayerPlayTime(String name, String totalTimeFormatted, long totalSeconds) {
        this.name = name;
        this.totalTimeFormatted = totalTimeFormatted;
        this.totalSeconds = totalSeconds;
    }

    public String getName() {
        return name;
    }

    public String getTotalTimeFormatted() {
        return totalTimeFormatted;
    }

    public long getTotalSeconds() {
        return totalSeconds;
    }

    @Override
    public int compareTo(PlayerPlayTime other) {
        return Long.compare(this.totalSeconds, other.totalSeconds);
    }
}
