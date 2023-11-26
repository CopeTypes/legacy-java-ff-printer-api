package slug2k.ffapi;

/**
 * A very basic class for logging to stdout
 * @author GhostTypes
 */
public class Logger {

    public static boolean isDebug = false;
    //placeholder for proper logging
    //todo proper logging

    /**
     * Log a general message to stdout
     * @param message The message
     */
    public static void log(String message) { System.out.println(message); }

    /**
     * Log an error message to stdout
     * @param message The message
     */
    public static void error(String message) { log("[Error] " + message); }

    /**
     * Log a debug message to stdout<br>
     * Only prints if isDebug is true
     * @param message The message
     */
    public static void debug(String message) {
        if (!isDebug) return;
        log("[DEBUG] " + message);
    }
}
