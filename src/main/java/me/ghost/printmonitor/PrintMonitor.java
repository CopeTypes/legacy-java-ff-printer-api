package me.ghost.printmonitor;

import me.ghost.printapi.PrinterWebcam;
import me.ghost.printapi.util.*;
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
import slug2k.ffapi.safety.ThermalSafety;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.ghost.printapi.util.PrintMonitorApi.DefectStatus;

public class PrintMonitor {
    //private String printerIp; don't really think we'll need the ip in this for anything?
    //private String webhookUrl;
    private WebhookUtil webhook;
    private PrinterClient client;
    private PrinterWebcam webcam;
    private ThermalSafety thermalSafety;

    //private TempInfo tempInfo;
    //private PrintStatus printStatus;
    //private EndstopStatus endstopStatus;
    //private MachineStatus machineStatus;
    //private MoveMode moveStatus;
    private PrinterInfo printerInfo;

    private boolean isPrinting = true;
    private boolean commandLock = false;
    private boolean syncing = false;

    private int checkFails = 0;

    SystemTimer lastSync = new SystemTimer();
    SystemTimer lastDiscordSync = new SystemTimer();
    SystemTimer lastTempCheck = new SystemTimer();
    SystemTimer lastFailCheck = new SystemTimer();

    private final ExecutorService thread = Executors.newSingleThreadExecutor();

    public PrintMonitor(String printerIp, String webhookUrl) throws PrinterException, InterruptedException {
        webhook = new WebhookUtil(webhookUrl);
        client = new PrinterClient(printerIp);
        webcam = new PrinterWebcam(printerIp);
        thermalSafety = new ThermalSafety(client, webhookUrl);

        long t = System.currentTimeMillis() - 30000;
        lastSync.setTime(t);
        lastDiscordSync.setTime(t);
        lastTempCheck.setTime(t);
        lastFailCheck.setTime(t);

        init();
    }

    public void start() throws PrinterException, InterruptedException {
        doSync();
        if (!isPrinting) {
            Logger.log("Waiting for print job to start...");
            waitForPrintJob();
        }
        sendImageToWebhook("Monitoring " + printerInfo.getName(), "Job: " + getJobName(), EmbedColors.BLUE);
        PrintMonitorApi.refreshUUIDs();
        sleep(500);
        thread.execute(this::checkThread);
    }

    private void checkThread() {
        while (isPrinting) {
            if (lastFailCheck.hasPassed(5000)) { // Check for print defects/failure every 5sec
                lastFailCheck.reset();
                defectCheck();
            }
            if (lastSync.hasPassed(5000)) { // Sync necessary monitoring info with the printer every 5sec
                lastFailCheck.reset();
                doSync();
            }
            if (!isPrinting) break;
            if (lastTempCheck.hasPassed(10000)) { // Thermal runaway failsafe check every 10sec
                lastTempCheck.reset();
                if (!thermalSafety.safe) {
                    failureShutdown();
                    System.exit(-1);
                }
            }
            if (lastDiscordSync.hasPassed(30000)) { // Sync job info & picture with discord webhook every 5 minutes
                lastDiscordSync.reset();
                sendDiscordReport();
            }
            sleep(1000);
        }
        Logger.log("Print completed, sending notification to discord and quitting.");
        sendImageToWebhook("Print complete!", "Your print has finished!", EmbedColors.GREEN);
        //System.exit(0);
    }

    private void defectCheck() {
        Logger.log("Checking the current print for defects");
        try {
            DefectStatus status = PrintMonitorApi.getDefectStatus();
            if (status == null) {
                checkFails++; // failsafe triggered after 5 failed defect checks in a row
                if (checkFails >= 5) {
                    webhook.sendMessage("Detection Failure", "There have been multiple failed defect checks in a short period of time, aborting the print.", EmbedColors.RED);
                    failureShutdown();
                }
                return;
            }
            if (checkFails > 0) checkFails--;
            if (status.defect) {
                if (!sendImageToWebhook("Print Defect Detected", "Defect detected in the last print check", EmbedColors.RED)) {
                    Logger.error("Defect detected, unable to get webcam capture.");
                    webhook.sendMessage("Print Defect Detected", "There was a defect detected in the last print check, but capturing the webcam failed.", EmbedColors.RED);
                }
                if (status.score >= 0.6F) {
                    if (!sendImageToWebhook("Print Failure Detected", "The current print has failed, aborting now", EmbedColors.RED)) {
                        Logger.error("Failure detected, unable to get webcam capture.");
                        webhook.sendMessage("Print Failure Detected", "Aborting print now (unable to capture webcam)", EmbedColors.RED);
                    }
                    failureShutdown();
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to check print defect status: " + e.getMessage());
        }
    }

    private void doSync() {
        try {
            isPrinting = client.isPrinting();
            //sync()
        } catch (PrinterException e) {
            Logger.error("Sync failure: " + e.getMessage());
            sleep(5000);
            try {
                isPrinting = client.isPrinting();
                //sync()
            } catch (Exception ignored) {
                Logger.log("Sync lost with printer, aborting current job for safety.");
                webhook.sendMessage("Sync failed", "Unable to sync with printer, attempting to abort current job.", EmbedColors.RED);
                shutdownFailsafe();
            }
        }
    }

    private void sendDiscordReport() {
        try {
            String out = Paths.get(FileUtil.getExecutionPath().toString(), "capture.jpg").toString();
            if (!saveImageFromWebcam(out)) {
                Logger.error("Unable to send print report to discord, failed to save image from printer webcam.");
                return;
            }
            PrintReport report = client.getPrintReport();
            if (!NetworkUtil.sendPrintReport(webhook.getUrl(), report, new File(out))) Logger.error("Failed to send print report to discord webhook.");
        } catch (PrinterException e) {
            Logger.error("Error getting print report for sendDiscordReport: " + e);
        }
    }

    private void failureShutdown() {
        try {
            client.stopPrint();
            webhook.sendMessage("Print stopped", "Print aborted due to failure", EmbedColors.GREEN);
        } catch (PrinterException e) { // this *should* never happen, but it's better safe than sorry
            Logger.error("Error while trying to abort print failure: " + e);
            webhook.sendMessage("Aborting print failed", "There was an error trying to abort the print, starting failsafe.", EmbedColors.RED);
            shutdownFailsafe();
            webhook.sendMessage("Success", "Successfully aborted print", EmbedColors.GREEN);
        }
    }

    private void shutdownFailsafe() {
        boolean stopped = false;
        while (!stopped) {
            try {
                stopped = client.isPrinting();
            } catch (PrinterException ignored) {}
            try {
                client.stopPrint();
            } catch (PrinterException ignored) {}
        }
    }

    private boolean saveImageFromWebcam(String out) {
        //String out = FileUtil.getExecutionPath().resolve("capture.jpg").toString();
        if (!webcam.getCapture(out)) {
            Logger.error("sendImageToWebhook failed to get capture from printer webcam.");
            return false;
        }
        return true;
    }

    private boolean sendImageToWebhook(String title, String message, String color) {
        //String out = FileUtil.getExecutionPath().resolve("capture.jpg").toString();
        String out = Paths.get(FileUtil.getExecutionPath().toString(), "capture.jpg").toString();
        if (!saveImageFromWebcam(out)) return false;
        NetworkUtil.sendImageToWebhook(webhook.getUrl(), title, message, new File(out), color);
        return true;
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws PrinterException, InterruptedException {
        commandLock = true;
        printerInfo = client.getPrinterInfo();
        Thread.sleep(500);
        EndstopStatus es = client.getEndstopStatus();
        if (!es.ledEnabled) { // make sure the lights are on
            Thread.sleep(500);
            client.setLed(true);
        }
        commandLock = false;
    }

    /*private void sync() throws PrinterException, InterruptedException {
        syncing = true;
        commandLock = true;
        Thread.sleep(500);
        isPrinting = client.isPrinting();
        Thread.sleep(500);
        commandLock = false;
        syncing = false;
    }*/

    private void waitForPrintJob() throws PrinterException {
        commandLock = true;
        while (!isPrinting) {
            isPrinting = client.isPrinting();
            sleep(2500);
        }
    }

    private String getJobName() throws PrinterException {
        commandLock = true;
        EndstopStatus es = client.getEndstopStatus();
        sleep(500);
        commandLock = false;
        return es.currentFile;
    }

}
