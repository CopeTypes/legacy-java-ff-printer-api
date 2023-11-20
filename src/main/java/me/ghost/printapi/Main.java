package me.ghost.printapi;

import me.ghost.printmonitor.Config;
import me.ghost.printmonitor.PrintMonitor;
import slug2k.ffapi.Logger;
import slug2k.ffapi.clients.PrinterClient;
import slug2k.ffapi.commands.info.PrinterInfo;
import slug2k.ffapi.commands.status.PrintStatus;
import slug2k.ffapi.enums.MachineStatus;
import slug2k.ffapi.exceptions.PrinterException;
import me.ghost.printapi.util.WebhookUtil;
import slug2k.ffapi.safety.ThermalSafety;

public class Main {

    //ff-print-control.jar 192.168.0.204 command_here
    //todo need to remake this to behave 'standalone'
    //Need to make a config thing that works with the python script too for stuff like the discord webhook and api key
    //This should be a like a "run and done" thing and take care of itself once the print completes
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

    private static void runMCode(PrinterClient client, String mcode) {
        try {
            String response = client.sendCommand(mcode);
            Logger.log("Response:\n" + response);
        } catch (PrinterException e) {
            e.printStackTrace();
        }
    }
}
