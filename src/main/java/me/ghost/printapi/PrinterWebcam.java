package me.ghost.printapi;

import me.ghost.printapi.util.NetworkUtil;

/**
 * Class for interacting with FlashForge printer cameras
 */
//This *should* be universal across all ff printers with an ip camera
public class PrinterWebcam {

    /**
     * URL for the camera's stream
     */
    private String streamURL;
    /**
     * URL to take a snapshot of the camera's stream
     */
    private String captureURL;

    /**
     * Creates a new PrinterWebcam instance
     * @param printerIP The ip of the printer
     */
    public PrinterWebcam(String printerIP) {
        streamURL = getBaseUrl(printerIP) + "stream";
        captureURL = getBaseUrl(printerIP) + "snapshot";
    }

    /**
     * Gets the base action url for the printer
     * @param printerIP The ip of the printer
     * @return The base action url
     */
    private String getBaseUrl(String printerIP) {
        return "http://" + printerIP + ":8080/?action=";
    }

    /**
     * Saves a snapshot from the printer's webcam
     *
     * @param outpath Where to save the capture
     * @return boolean
     */
    public boolean getCapture(String outpath) {
        return NetworkUtil.downloadImage(captureURL, outpath);
    }


}
