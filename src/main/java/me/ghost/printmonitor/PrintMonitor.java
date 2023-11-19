package me.ghost.printmonitor;

import me.ghost.printapi.PrinterWebcam;
import me.ghost.printapi.util.EmbedColors;
import me.ghost.printapi.util.FileUtil;
import me.ghost.printapi.util.NetworkUtil;
import me.ghost.printapi.util.WebhookUtil;
import slug2k.ffapi.Logger;
import slug2k.ffapi.clients.PrinterClient;
import slug2k.ffapi.commands.extra.PrintReport;
import slug2k.ffapi.commands.info.PrinterInfo;
import slug2k.ffapi.commands.info.TempInfo;
import slug2k.ffapi.commands.status.EndstopStatus;
import slug2k.ffapi.commands.status.PrintStatus;
import slug2k.ffapi.enums.MachineStatus;
import slug2k.ffapi.enums.MoveMode;
import slug2k.ffapi.exceptions.PrinterException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PrintMonitor {
    private String printerIp;
    //private String webhookUrl;
    private WebhookUtil webhook;
    private PrinterClient client;
    private PrinterWebcam webcam;

    private TempInfo tempInfo;
    private PrintStatus printStatus;
    private EndstopStatus endstopStatus;
    private MachineStatus machineStatus;
    private MoveMode moveStatus;
    private PrinterInfo printerInfo;

    private boolean isPrinting = true;
    private boolean commandLock = false;

    private Executor executor = Executors.newCachedThreadPool();

    public PrintMonitor(String printerIp, String webhookUrl) throws PrinterException, InterruptedException {
        this.printerIp = printerIp;
        //this.webhookUrl = webhookUrl;
        webhook = new WebhookUtil(webhookUrl);
        client = new PrinterClient(printerIp);
        webcam = new PrinterWebcam(printerIp);
        init();
    }

    public void start() throws PrinterException, InterruptedException {
        sync();
        if (!isPrinting) {
            Logger.log("Waiting for print job to start...");
            waitForPrintJob();
        }
        executor.execute(this::discordThread);
        executor.execute(this::syncThread);
    }


    private void discordThread() {
        Logger.log("Discord thread started");
        while (isPrinting) {
            sleep(30000);
            if (commandLock) sleep(5000);
            try {
                String out = FileUtil.getExecutionPath().resolve("capture.jpg").toString();
                if (!webcam.getCapture(out)) {
                    Logger.error("Failed to get webcam capture on discordThread()");
                    return;
                }
                PrintReport report = client.getPrintReport();
                boolean sent = sendImageToWebhook("Print Progress Update", "Current progress of your print", EmbedColors.BLUE);
                if (!sent) Logger.error("Error while sending image to webhook, image not sent.");
                //NetworkUtil.sendImageToWebhook(webhook.getUrl(), "Print Progress Update", "Current progress of your print", new File(out), EmbedColors.BLUE);
                webhook.sendPrintReport(report);
            } catch (PrinterException e) {
                Logger.error("Error getting print report on discordThread(): " + e);
            }
        }
        sendImageToWebhook("Print Complete!", "Your print has finished!", EmbedColors.GREEN);
    }

    private void syncThread() {
        Logger.log("Sync thread started");
        while (isPrinting) {
            sleep(30000); //todo mess around with the interval
            try {
                sync();
            } catch (PrinterException e) { // todo error handling?
                Logger.error("Sync failure: " + e.getMessage());
            } catch (InterruptedException ignored) {}
        }
    }

    private boolean sendImageToWebhook(String title, String message, String color) {
        String out = FileUtil.getExecutionPath().resolve("capture.jpg").toString();
        if (!webcam.getCapture(out)) {
            Logger.error("sendImageToWebhook failed to get capture from printer webcam.");
            return false;
        }
        return NetworkUtil.sendImageToWebhook(webhook.getUrl(), title, message, new File(out), color);
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (Exception ignored) {}
    }

    private void init() throws PrinterException, InterruptedException {
        commandLock = true;
        printerInfo = client.getPrinterInfo();
        Thread.sleep(500);
        commandLock = false;
        //todo is there anything else that should be done here?
    }

    private void sync() throws PrinterException, InterruptedException {
        commandLock = true;
        tempInfo = client.getTempInfo();
        Thread.sleep(500);
        printStatus = client.getPrintStatus();
        Thread.sleep(500);
        endstopStatus = client.getEndstopStatus();
        Thread.sleep(500);
        machineStatus = client.getMachineStatus();
        Thread.sleep(500);
        moveStatus = client.getMoveStatus();
        Thread.sleep(500);
        isPrinting = client.isPrinting();
        Thread.sleep(500);
        commandLock = false;
    }

    private void waitForPrintJob() throws PrinterException {
        commandLock = true;
        while (!isPrinting) {
            isPrinting = client.isPrinting();
            sleep(2500);
        }
    }
}
