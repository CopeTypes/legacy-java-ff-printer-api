package me.ghost.printapi.util;


import slug2k.ffapi.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
        ExecutorService executor = Executors.newFixedThreadPool(8);

        List<Future<String>> futures = new ArrayList<>();

        for (int i = 199; i < 215; i++) {
            String ip = host.substring(0, host.length() - 1) + i;
            futures.add(executor.submit(() -> isPrinter(ip, "8080") ? ip : null));
        }

        executor.shutdown();

        for (Future<String> future : futures) {
            try {
                String result = future.get();
                if (result != null) return result;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
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
            //Logger.log("contentType: " + contentType);
            if (!contentType.contains("multipart/x-mixed-replace")) return false;
            return statusCode == 200;
        } catch (IOException e) {
            return false;
        }
    }

}
