package me.ghost.printapi.util;

/**
 * Simple timer class
 * @author GhostTypes
 */
//todo hasPassed with seconds/minutes using TimeUnit
public class SystemTimer {
    private long time;

    /**
     * Simple timer class
     */
    public SystemTimer() {
        time = System.currentTimeMillis();
    }

    /**
     * Check if x duration has passed
     * @param ms ms duration to check
     * @return boolean
     */
    public boolean hasPassed(double ms) {
        return System.currentTimeMillis() - time >= ms;
    }

    /**
     * Gets the time elapsed since the timer was re/started
     * @return Time duration in long format (ms)
     */
    public long getPassed() {
        return currTime() - time;
    }

    /**
     * Resets the elapsed time back to zero
     */
    public void reset() {
        time = currTime();
    }

    /**
     * Gets the current time in long format for this timer instance
     * @return Time in long format
     */
    public long getTime() {
        return time;
    }

    /**
     * Sets the current time for this timer instance
     * @param time Time in long format
     */
    public void setTime(long time) {
        this.time = time;
    }

    private long currTime() {
        return System.currentTimeMillis();
    }


    public static final long SECONDS_5 = 5000;
    public static final long SECONDS_10 = 10000;
    public static final long SECONDS_15 = 15000;
    public static final long SECONDS_30 = 30000;
    public static final long MINUTES_1 = 60000;
    public static final long MINUTES_5 = 300000;
    public static final long MINUTES_10 = 600000;
    public static final long MINUTES_15 = 900000;
    public static final long MINUTES_30 = 1800000;

}
