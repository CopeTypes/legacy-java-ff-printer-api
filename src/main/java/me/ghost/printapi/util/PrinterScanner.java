package me.ghost.printapi.util;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Find FlashForge printers on the local network
 */
public class PrinterScanner {
    private String host = "192.168.0.1";

    /**
     * Scans the local network for FlashForge printers
     * @return The IP address of the first online printer found
     */
    public String findPrinter() {
        for (int i = 199; i < 215; i++) {
            String ip = host.substring(0, host.length() - 1) + i;
            if (isPrinter(ip, "8080")) return ip;
        }
        return null;
    }

    /**
     * Cringe way to check for a FlashForge printer at a given IP
     * @param ip IP address to check
     * @param port Port to use (should be 8080)
     *
     */
    private boolean isPrinter(String ip, String port) {
        String streamUrl = "http://" + ip + ":" + port + "/?action=stream";
        try {
            URL url = new URL(streamUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);

            int statusCode = connection.getResponseCode();
            String contentType = connection.getContentType();

            if ("text/html".equals(contentType)) return false;

            return statusCode == 200;
        } catch (IOException e) {
            return false;
        }s
    }

}
