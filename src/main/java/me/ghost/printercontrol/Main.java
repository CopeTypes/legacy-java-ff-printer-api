package me.ghost.printercontrol;

import de.slg.ddnss.printertool.Logger;
import de.slg.ddnss.printertool.clients.*;
import de.slg.ddnss.printertool.commands.PrinterInfo;
import de.slg.ddnss.printertool.exceptions.FlashForgePrinterException;
import me.ghost.printercontrol.util.WebhookUtil;

public class Main {

    //ff-print-control.jar 192.168.0.204 command_here
    public static void main(String[] args) {
        if (args.length == 0 || args.length == 1) {
            System.out.println("Invalid syntax.");
            System.out.println("Usage: ff-print-control.jar printer_ip command_here");
            System.exit(0);
        }
        try (AdventurerClient client = new AdventurerClient(args[0])) {
            PrinterInfo printerInfo = client.getPrinterInfo();
            Logger.log("Connected to " + printerInfo.getMashineType() + " on firmware " + printerInfo.getFirmwareVersion());
            String command = args[1];
            if (command.startsWith("M")) {
                Logger.log("Sending MCode: " + command);
                if (!command.contains("~")) command = command.replace("M", "~M");
                runMCode(client, command);
                System.exit(0);
            }
            switch (command) {
                case "stop_print" -> {
                    client.stopPrint();
                    System.exit(0);
                }
                case "send_report" -> {
                    if (args.length > 2) {
                        WebhookUtil whUtil = new WebhookUtil(args[2]);
                        whUtil.sendPrintReport(client.getPrintReport());
                        System.exit(0);
                    } else {
                        Logger.error("Invalid syntax, provide the webhook url after send_report command.");
                        System.exit(-1);
                    }
                }
            }
        } catch (FlashForgePrinterException e) {
            Logger.error(e.getMessage());
        }
    }

    private static void runMCode(AdventurerClient client, String mcode) {
        try {
            String response = client.sendCommand(mcode);
            Logger.log("Response:\n" + response);
        } catch (FlashForgePrinterException e) {
            e.printStackTrace();
        }
    }
}
