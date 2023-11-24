package me.ghost.printapi;

import me.ghost.printapi.util.NetworkUtil;

public class PrinterWebcam {

    private String streamURL;
    private String captureURL;

    public PrinterWebcam(String printerIP) {
        streamURL = getBaseUrl(printerIP) + "stream";
        captureURL = getBaseUrl(printerIP) + "snapshot";
    }

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
