package me.ghost.printapi.util;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import slug2k.ffapi.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


/**
 * Class for handling network interactions
 * @author GhostTypes
 */
public class NetworkUtil {

    /**
     * Download an image from a given URL
     *
     * @param url  URL for the image to download
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

    /**
     * Convert a color string to a Color instance
     * @param color Color as a string (hex, etc.)
     * @return Color
     */
    private static Color getColorS(String color) {
        Color c;
        if (color.startsWith("0x")) c = hexToColor(color);
        else c = new Color(Integer.parseInt(color));
        return c;
    }

    /**
     * Executes (sends) the supplied WebhookMessage using the supplied WebhookClient
     * @param client WebhookClient instance
     * @param msg WebhookMessage instance
     * @return boolean
     */
    private static boolean execute(WebhookClient client, WebhookMessage msg) {
        try {
            client.send(msg);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Sends an embed message to the supplied webhook url
     * @param webhook The target webhook url
     * @param title Title of the embed
     * @param message Description (message) of the embed
     * @param color Color for the embed
     * @return boolean
     */
    public static boolean sendWebhookMessage(String webhook, String title, String message, String color) {
        Color c = getColorS(color);
        WebhookClient wh = WebhookClient.withUrl(webhook);
        WebhookEmbed embed = new WebhookEmbedBuilder().setTitle(new WebhookEmbed.EmbedTitle(title, null)).setDescription(message).setColor(c.getRGB()).build();
        WebhookMessage msg = new WebhookMessageBuilder().addEmbeds(embed).build();
        return execute(wh, msg);
    }

    /**
     * Sends an embed message to the supplied webhook url, with an image
     * @param webhook The target webhook url
     * @param title Title of the embed
     * @param message Description (message) of the embed
     * @param image (File) The image to upload
     * @param color Color for the embed
     * @return boolean
     */
    public static boolean sendImageToWebhook(String webhook, String title, String message, File image, String color) {
        Color c = getColorS(color);
        WebhookClient wh = WebhookClient.withUrl(webhook);
        WebhookEmbed embed = new WebhookEmbedBuilder().setTitle(new WebhookEmbed.EmbedTitle(title, null)).setDescription(message).setImageUrl("attachment://capture.jpg").setColor(c.getRGB()).build();
        WebhookMessage msg = new WebhookMessageBuilder().addFile("capture.jpg", image).addEmbeds(embed).build();
        return execute(wh, msg);
    }

    public static boolean sendEmbed(String webhook, WebhookEmbed embed) {
        WebhookClient wh = WebhookClient.withUrl(webhook);
        WebhookMessage message = new WebhookMessageBuilder().addEmbeds(embed).build();
        return execute(wh, message);
    }

    public static boolean sendEmbed(String webhook, WebhookEmbed embed, File image) {
        WebhookClient wh = WebhookClient.withUrl(webhook);
        WebhookMessage message = new WebhookMessageBuilder().addFile("capture.jpg", image).addEmbeds(embed).build();
        return execute(wh, message);
    }

    /**
     * Converts a hex color string to a Color instance
     * @param hexColor Hex color string
     * @return Color
     */
    private static Color hexToColor(String hexColor) {
        if (hexColor.startsWith("0x")) hexColor = hexColor.substring(2);
        long colorValue = Long.parseLong(hexColor, 16);
        int red = (int) ((colorValue >> 16) & 0xFF);
        int green = (int) ((colorValue >> 8) & 0xFF);
        int blue = (int) (colorValue & 0xFF);
        return new Color(red, green, blue);
    }
}
