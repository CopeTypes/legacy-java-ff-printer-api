package me.ghost.printapi.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import slug2k.ffapi.Logger;
import slug2k.ffapi.commands.extra.PrintReport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkUtil {

    /**
     * Download an image from a given URL
     * @param url URL for the image to download
     * @param path Path for the image to download to
     * @return boolean
     */
    public static boolean downloadImage(String url, String path) {
        try {
            URL uri = new URL(url);
            try (InputStream is = uri.openStream()) {
                Path outpath = Path.of(path);
                Files.copy(is, outpath, StandardCopyOption.REPLACE_EXISTING);
                return true;
            } catch (IOException e) {
                Logger.error("downloadImage IOException with url: " + url + "\n" + e.getMessage());
                return false;
            }
        } catch (MalformedURLException e) {
            Logger.error("downloadImage bad url: " + url + "\n" + e.getMessage());
            return false;
        }
    }

    // todo impl color args / enum for it
    public static boolean sendImageToWebhook(String webhook, String title, String message, File image, String color) {
        WebhookClient wh = WebhookClient.withUrl(webhook);
        WebhookEmbed embed = new WebhookEmbedBuilder().setTitle(new WebhookEmbed.EmbedTitle(title, null)).setDescription(message).setImageUrl("attachment://capture.jpg").setColor(Integer.valueOf(color)).build();
        WebhookMessage msg = new WebhookMessageBuilder().addFile("capture.jpg", image).addEmbeds(embed).build();
        AtomicBoolean ret = new AtomicBoolean(false);
        wh.send(msg).thenAccept((msgg) -> ret.set(true));
        return ret.get();
    }

    public static boolean sendPrintReport(String webhook, PrintReport report, File image) {
        WebhookClient wh = WebhookClient.withUrl(webhook);
        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setTitle(new WebhookEmbed.EmbedTitle("Print Status Report", null))
                .setDescription("Stats on the current print job")
                .addField(new WebhookEmbed.EmbedField(false, "Current File", report.currentFile))
                .addField(new WebhookEmbed.EmbedField(false, "Print Progress", report.layerProgress.progress + "%"))
                .addField(new WebhookEmbed.EmbedField(false, "Extruder Temp", report.extruderTemp))
                .addField(new WebhookEmbed.EmbedField(false, "Bed Temp", report.bedTemp))
                .build();
        WebhookMessage message = new WebhookMessageBuilder().addFile("capture.jpg", image).addEmbeds(embed).build();
        AtomicBoolean ret = new AtomicBoolean(false);
        wh.send(message).thenAccept((msgg) -> ret.set(true));
        return ret.get();
    }

}
