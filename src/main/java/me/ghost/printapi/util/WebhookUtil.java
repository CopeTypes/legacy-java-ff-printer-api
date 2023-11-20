package me.ghost.printapi.util;

import slug2k.ffapi.Logger;
import slug2k.ffapi.commands.extra.PrintReport;

import java.awt.*;
import java.io.IOException;

public class WebhookUtil {

    private String webhookUrl = "";

    public WebhookUtil(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public void sendPrintReport(PrintReport printReport) {
        DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle("Print Progress Update")
                .setDescription("Current file: " + printReport.currentFile)
                .addField("Print progress", printReport.layerProgress.progress + "%", false)
                .addField("Extruder Temp", printReport.extruderTemp, false)
                .addField("Bed Temp", printReport.bedTemp, false)
        );
        try {
            webhook.execute();
        } catch (IOException e) {
            Logger.error("Error sending print report : " + e.getMessage());
        }
    }

    public void sendMessage(String title, String message, String color) {
        DiscordWebhook webhook = new DiscordWebhook(webhookUrl);
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle(title)
                .setDescription(message)
                .setColor(Color.decode(color))
        );
        try {
            webhook.execute();
        } catch (IOException e) {
            Logger.error("Error sending webhook message : " + e.getMessage());
        }
    }

    public String getUrl() {
        return webhookUrl;
    }
}
