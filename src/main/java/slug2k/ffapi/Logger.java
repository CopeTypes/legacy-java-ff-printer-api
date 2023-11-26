package slug2k.ffapi;

public class Logger {

    public static boolean isDebug = false;
    //placeholder for proper logging
    //todo proper logging

    public static void log(String message) {
        System.out.println(message);
    }

    public static void error(String message) {
        log("[Error] " + message);
    }
    public static void debug(String message) {
        if (!isDebug) return;
        log("[DEBUG] " + message);
    }
}
