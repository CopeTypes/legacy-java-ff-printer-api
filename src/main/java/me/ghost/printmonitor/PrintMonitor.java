package me.ghost.printmonitor;

import me.ghost.printapi.PrintMonitorApi;
import me.ghost.printapi.PrinterWebcam;
import me.ghost.printapi.util.*;
import slug2k.ffapi.Logger;
import slug2k.ffapi.clients.PrinterClient;
import slug2k.ffapi.commands.extra.PrintReport;
import slug2k.ffapi.commands.info.PrinterInfo;
import slug2k.ffapi.commands.status.EndstopStatus;
import slug2k.ffapi.exceptions.PrinterException;
import slug2k.ffapi.safety.ThermalSafety;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static me.ghost.printapi.PrintMonitorApi.DefectStatus;

/**
 * Class for monitoring prints<br>
 * Sends automatic progress updates to discord<br>
 * Automatic defect detection & alerts<br>
 * Automatic print cancellation on failure & alerts<br>
 */
public class PrintMonitor {
    private String webhookUrl;
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

    private int checkFails = 0;

    SystemTimer lastSync = new SystemTimer();
    SystemTimer lastDiscordSync = new SystemTimer();
    SystemTimer lastTempCheck = new SystemTimer();
    SystemTimer lastFailCheck = new SystemTimer();

    private final ExecutorService thread = Executors.newSingleThreadExecutor();
    private final ExecutorService defectThread = Executors.newSingleThreadExecutor();

    /**
     * Creates a new PrintMonitor
     * @param printerIp The ip of the printer
     * @param webhookUrl The webhook url for notifications etc.
     * @throws PrinterException Communication error with the printer
     * @throws InterruptedException Failed to sleep between action(s)
     */
    public PrintMonitor(String printerIp, String webhookUrl) throws PrinterException, InterruptedException {
        this.webhookUrl = webhookUrl;
        client = new PrinterClient(printerIp);
        webcam = new PrinterWebcam(printerIp);
        thermalSafety = new ThermalSafety(client);

        long t = System.currentTimeMillis() - 300000;
        lastSync.setTime(t);
        lastDiscordSync.setTime(t);
        lastTempCheck.setTime(t);
        lastFailCheck.setTime(t);

        init();
    }

    /**
     * Starts the print monitor, which will automatically exit when the print completes
     * @throws PrinterException Communication error with the printer
     * @throws InterruptedException Failed to sleep between action(s)
     */
    public void start() throws PrinterException, InterruptedException {
        Logger.debug("start()");
        doSync();
        Logger.log("Connected to " + printerInfo.getName() + " (fw " + printerInfo.getFirmwareVersion() + ")");
        if (Logger.isDebug) {
            Logger.debug("Serial Number: " + printerInfo.getSerialNumber());
            Logger.debug("MAC Address: " + printerInfo.getSerialNumber());
            Logger.debug("Print Dimensions: " + printerInfo.getDimensions());
        }
        if (!isPrinting) {
            Logger.log("Waiting for print job to start...");
            waitForPrintJob();
        }
        sendImageToWebhook("Monitoring " + printerInfo.getName(), "Job: " + getJobName(), EmbedColors.BLUE);
        Logger.debug("Generating new PrinterId and TickedId");
        PrintMonitorApi.refreshUUIDs();
        sleep(500);
        thread.execute(this::checkThread);
        defectThread.execute(this::checkDefectThread);
    }

    /**
     * Thread for checking the current print for defects/failure(s)
     */
    private void checkDefectThread() {
        Logger.debug("checkDefectThread()");
        while (isPrinting) { if (shouldCheckDefect()) defectCheck(); }
    }

    /**
     * Thread for syncing printer info, checking temps, and sending progress updates to Discord
     */
    private void checkThread() {
        Logger.debug("checkThread()");
        while (isPrinting) {
            //if (shouldCheckDefect()) defectCheck();
            if (shouldSync()) doSync();
            if (!isPrinting) break;
            if (shouldCheckTemps()) tempCheck();
            if (shouldSyncDiscord()) sendDiscordReport();
            sleep(1000);
        }
        Logger.log("Print completed, sending notification to discord and quitting.");
        sendImageToWebhook("Print complete!", "Your print has finished!", EmbedColors.GREEN);
        sleep(1000);
        defectThread.shutdownNow();
        sleep(1000);
        //todo figure out why it's not exiting on it's own after
        System.exit(0);
    }

    /**
     * Checks if the printers current temps are within a safe range<br>
     * Automatically cancels the current print if not
     */
    private void tempCheck() {
        Logger.debug("tempCheck()");
        try {
            if (!thermalSafety.areTempsSafe()) thermalShutdown();
        } catch (PrinterException e) {
            Logger.error("Error while checking printer temps: " + e.getMessage());
            int tries = 0;
            while (tries <= 5) {
                sleep(1000);
                try {
                    if (!thermalSafety.areTempsSafe()) thermalShutdown();
                } catch (PrinterException ignored) {
                    tries++;
                }
            }
        }
    }

    private void sendMessage(String title, String message, String color) {
        //todo handle sending failure
        NetworkUtil.sendWebhookMessage(webhookUrl, title, message, color);
    }

    /**
     * Check the current print for defects/failure(s)
     */
    private void defectCheck() {
        Logger.debug("defectCheck()");
        try {
            DefectStatus status = PrintMonitorApi.getDefectStatus();
            if (status == null) {
                checkFails++; // failsafe triggered after 5 failed defect checks in a row
                Logger.debug("Got null DefectStatus, checkFails is: " + checkFails);
                if (checkFails >= 5) {
                    sendMessage("Detection Failure", "There have been multiple failed defect checks in a short period of time, aborting the print.", EmbedColors.RED);
                    failureShutdown();
                }
                return;
            }
            if (checkFails > 0) checkFails--;
            if (status.defect) {
                Logger.error("Defect detected!");
                if (!sendImageToWebhook("Print Defect Detected", "Defect detected in the last print check", EmbedColors.RED)) {
                    Logger.error("Defect detected, unable to get webcam capture.");
                    sendMessage("Print Defect Detected", "There was a defect detected in the last print check, but capturing the webcam failed.", EmbedColors.RED);
                }
                if (status.score >= 0.6F) {
                    Logger.error("Failure detected!");
                    if (!sendImageToWebhook("Print Failure Detected", "The current print has failed, aborting now", EmbedColors.RED)) {
                        Logger.error("Failure detected, unable to get webcam capture.");
                        sendMessage("Print Failure Detected", "Aborting print now (unable to capture webcam)", EmbedColors.RED);
                    }
                    failureShutdown();
                }
            }
        } catch (IOException e) {
            Logger.error("Failed to check print defect status: " + e.getMessage());
        }
    }

    private void doSync() {
        Logger.debug("doSync()");
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
                sendMessage("Sync failed", "Unable to sync with printer, attempting to abort current job.", EmbedColors.RED);
                shutdownFailsafe();
            }
        }
    }

    private void sendDiscordReport() {
        Logger.debug("sendDiscordReport()");
        try {
            String out = Paths.get(FileUtil.getExecutionPath().toString(), "capture.jpg").toString();
            if (!saveImageFromWebcam(out)) {
                Logger.error("Unable to send print report to discord, failed to save image from printer webcam.");
                return;
            }
            PrintReport report = client.getPrintReport();
            if (!NetworkUtil.sendPrintReport(webhookUrl, report, new File(out)))
                Logger.error("Failed to send print report to discord webhook.");
        } catch (PrinterException e) {
            Logger.error("Error getting print report for sendDiscordReport: " + e);
        }
    }

    private void thermalShutdown() {
        Logger.error("Unsafe printer temps detected, aborting print.");
        failureShutdown();
        System.exit(-1);
    }

    /**
     * Used to cancel the current print if an error occurs
     */
    private void failureShutdown() {
        Logger.debug("failureShutdown()");
        try {
            Logger.debug("Waiting for print to stop");
            client.stopPrint();
            Logger.debug("Print stopped");
            sendMessage("Print stopped", "Print aborted due to failure", EmbedColors.GREEN);
        } catch (PrinterException e) { // this *should* never happen, but it's better safe than sorry
            Logger.error("Error while trying to abort print failure: " + e);
            sendMessage("Aborting print failed", "There was an error trying to abort the print, starting failsafe.", EmbedColors.RED);
            shutdownFailsafe();
            sendMessage("Success", "Successfully aborted print", EmbedColors.GREEN);
        }
    }

    /**
     * Used as a failsafe if failureShutdown() fails to stop the current print
     */
    private void shutdownFailsafe() {
        Logger.debug("shutdownFailsafe()");
        boolean stopped = false;
        while (!stopped) {
            try { stopped = client.isPrinting(); } catch (PrinterException ignored) {}
            try { client.stopPrint(); } catch (PrinterException ignored) {}
        }
    }

    private boolean saveImageFromWebcam(String out) {
        if (!webcam.getCapture(out)) {
            Logger.error("sendImageToWebhook failed to get capture from printer webcam.");
            return false;
        }
        return true;
    }

    private boolean sendImageToWebhook(String title, String message, String color) {
        String out = Paths.get(FileUtil.getExecutionPath().toString(), "capture.jpg").toString();
        if (!saveImageFromWebcam(out)) return false;
        NetworkUtil.sendImageToWebhook(webhookUrl, title, message, new File(out), color);
        return true;
    }

    private void sleep(long millis) {
        try { Thread.sleep(millis); } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     * Gets the printer ready for monitoring
     * @throws PrinterException Communication error with the printer
     * @throws InterruptedException Failed to sleep between action(s)
     */
    private void init() throws PrinterException, InterruptedException {
        printerInfo = client.getPrinterInfo();
        Thread.sleep(500);
        EndstopStatus es = client.getEndstopStatus();
        if (!es.ledEnabled) { // make sure the lights are on
            Thread.sleep(500);
            client.setLed(true);
        }
    }

    /**
     * Waits until the printer's job has started (checks every 2.5s)
     * @throws PrinterException Communication error with the printer
     */
    private void waitForPrintJob() throws PrinterException {
        while (!isPrinting) {
            isPrinting = client.isPrinting();
            sleep(2500);
        }
    }

    /**
     * Gets the file name of the current print job
     * @return The file name of the current print job
     * @throws PrinterException Communication error with the printer
     */
    private String getJobName() throws PrinterException {
        EndstopStatus es = client.getEndstopStatus();
        sleep(500);
        return es.currentFile;
    }

    private boolean shouldCheckDefect() {
        if (lastFailCheck.hasPassed(SystemTimer.FIVE_SECS)) {
            lastFailCheck.reset();
            return true;
        }
        return false;
    }

    private boolean shouldSync() {
        if (lastSync.hasPassed(SystemTimer.FIVE_SECS)) {
            lastSync.reset();
            return true;
        }
        return false;
    }

    private boolean shouldSyncDiscord() {
        if (lastDiscordSync.hasPassed(SystemTimer.FIVE_MINS)) {
            lastDiscordSync.reset();
            return true;
        }
        return false;
    }

    private boolean shouldCheckTemps() {
        if (lastTempCheck.hasPassed(SystemTimer.TEN_SECS)) {
            lastTempCheck.reset();
            return true;
        }
        return false;
    }

}
