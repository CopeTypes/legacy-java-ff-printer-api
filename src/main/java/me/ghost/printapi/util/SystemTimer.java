package me.ghost.printapi.util;

public class SystemTimer {
    private long time;

    public SystemTimer() {
        time = System.currentTimeMillis();
    }

    public boolean hasPassed(double ms) {
        return System.currentTimeMillis() - time >= ms;
    }

    public void reset() {
        time = System.currentTimeMillis();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }


    public static final long FIVE_SECS = 5000;
    public static final long TEN_SECS = 10000;
    public static final long FIVE_MINS = 300000;
}
