package de.slg.ddnss.printertool;

public class Logger {
    //placeholder for proper logging
    //todo proper logging

    public static void log(String message) {
        System.out.println(message);
    }

    public static void error(String message) {
        log("[Error] " + message);
    }

}
