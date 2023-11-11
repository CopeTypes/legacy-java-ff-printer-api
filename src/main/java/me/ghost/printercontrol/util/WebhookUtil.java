package me.ghost.printercontrol.util;

import de.slg.ddnss.printertool.Logger;
import de.slg.ddnss.printertool.commands.PrintReport;

import java.io.IOException;

public class WebhookUtil {

    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1171639259715883060/DSEqbrsAi-S0dZ7zesRsbqC1a30VtKvdlZqiVFV2rvmgX1wRFMkYSB_PtY_JGswDYD0T";

    public static void sendPrintReport(PrintReport printReport) {
        DiscordWebhook webhook = new DiscordWebhook(WEBHOOK_URL);
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle("Print Progress Update")
                .setDescription("Current file: " + printReport.currentFile)
                .addField("Layer Progress", printReport.layerProgress, true)
                .addField("Extruder Temp", printReport.extruderTemp, true)
                .addField("Bed Temp", printReport.bedTemp, true)
        );
        try {
            webhook.execute();
        } catch (IOException e) {
            Logger.error("Error sending print report : " + e.getMessage());
        }
    }

    public static void sendMessage(String title, String message) {
        DiscordWebhook webhook = new DiscordWebhook(WEBHOOK_URL);
        webhook.addEmbed(new DiscordWebhook.EmbedObject()
                .setTitle(title)
                .setDescription(message)
        );
        try {
            webhook.execute();
        } catch (IOException e) {
            Logger.error("Error sending webhook message : " + e.getMessage());
        }
    }

}
