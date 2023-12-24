package me.ghost.printmonitor;

import me.ghost.printapi.util.PrinterScanner;
import slug2k.ffapi.Logger;
import slug2k.ffapi.clients.PrinterClient;
import slug2k.ffapi.exceptions.PrinterException;

public class Main {
    // java -jar jar_name.jar printer_ip (debug)
    public static void main(String[] args) {
        String printerIp;
        if (args.length < 1) { // scan for printers if no IP is provided
            // this won't let the user use additional args like normal
            // todo see if we care about this
            Logger.log("No printer ip provided, scanning for printers...");
            PrinterScanner scanner = new PrinterScanner();
            printerIp = scanner.findPrinter();
            if (printerIp == null) {
                Logger.log("No online FlashForge printer found.");
                System.exit(-1);
            }
            Logger.log("Found printer  at " + printerIp);
            //Logger.log("You need to provide the printer ip");
            //System.exit(-1);
        } else {
            printerIp = args[0];
        }
        Config config = new Config();
        if (config.apiKey == null || config.webhookUrl == null) {
            Logger.error("Invalid config.json");
            System.exit(-1);
        }
        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("debug")) Logger.isDebug = true;
            else if (args[1].equalsIgnoreCase("dump")) {
                doDump(args[0]);
                System.exit(0);
            }
            else if (args[1].equalsIgnoreCase("dump_layer")) {
                dumpLayerData(args[0]);
                System.exit(0);
            }
        }
        try {
            PrintMonitor monitor = new PrintMonitor(printerIp, config.webhookUrl);
            monitor.start();
        } catch (PrinterException | InterruptedException e) {
            Logger.error("Unable to start PrintMonitor: " + e.getMessage());
            System.exit(-1);
        }
    }

    /**
     * Retrieves all current printer info, and dumps it to console
     * @param printerIp The ip of the printer
     */
    private static void doDump(String printerIp) {
        PrinterClient client = new PrinterClient(printerIp);
        Logger.isDebug = true;
        try {
            client.getMoveStatus();
            Thread.sleep(1000);
            client.getMachineStatus();
            Thread.sleep(1000);
            client.getEndstopStatus();
            Thread.sleep(1000);
            client.getPrinterInfo();
            Thread.sleep(1000);
            client.getPrintStatus();
            Thread.sleep(1000);
            client.getTempInfo();
            Thread.sleep(1000);
            client.getLocationInfo();
        } catch (PrinterException | InterruptedException ignored) {}
    }

    private static void dumpLayerData(String printerIp) {
        PrinterClient client = new PrinterClient(printerIp);
        Logger.isDebug = true;
        try {
            client.getPrintStatus();
        } catch (PrinterException ignored) {}
    }
}
