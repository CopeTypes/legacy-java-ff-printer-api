package me.ghost.printmonitor;

import slug2k.ffapi.Logger;
import slug2k.ffapi.exceptions.PrinterException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            Logger.log("You need to provide the printer ip");
            System.exit(-1);
        }
        Config config = new Config();
        if (config.apiKey == null || config.webhookUrl == null) {
            Logger.error("Invalid config.json");
            System.exit(-1);
        }
        try {
            PrintMonitor monitor = new PrintMonitor(args[0], config.webhookUrl);
            monitor.start();
        } catch (PrinterException e) {
            Logger.error("Unable to start PrintMonitor: " + e.getMessage());
            System.exit(-1);
        } catch (InterruptedException e) {
            Logger.error("Unable to start PrintMonitor: " + e.getMessage());
        }
    }
}
