package slug2k.ffapi.safety;

import me.ghost.printapi.util.EmbedColors;
import slug2k.ffapi.clients.PrinterClient;
import slug2k.ffapi.commands.info.TempInfo;
import slug2k.ffapi.exceptions.PrinterException;
import me.ghost.printapi.util.WebhookUtil;

/**
 * External temperature safety checks.
 * Stops the print automatically if extruder or bed temps exceed a safe limit
 * @author GhostTypes
 */
public class ThermalSafety {

    private final int EXTRUDER_MAX = 250;
    private final int BED_MAX = 100;

    private PrinterClient client;
    private WebhookUtil whUtil;

    public boolean safe = true;

    public ThermalSafety(PrinterClient client, String webhookUrl) {
        this.client = client;
        whUtil = new WebhookUtil(webhookUrl);
    }

    public void run() throws PrinterException {
        if (!areTempsSafe()) {
            safe = false;
            //todo would probably be safer to immediately stop the printer and/or cut off power
            //need to see if MCode for either of those things is supported by Flashforge's firmware.
            whUtil.sendMessage("Thermal Safety Alert", "One or more of the printer's temperature values exceeded a safe limit, the current print is being cancelled now.", EmbedColors.RED);
            client.stopPrint();
        }
    }

    private boolean areTempsSafe() throws PrinterException {
        TempInfo temps = client.getTempInfo();
        String extruder = temps.extruderTemp;
        if (extruder.contains("/")) extruder = extruder.split("/")[0].trim();
        if (extruder.contains(".")) extruder = extruder.split("\\.")[0].trim();
        if (Integer.parseInt(extruder) >= EXTRUDER_MAX) return false;
        String bed = temps.bedTemp;
        if (bed.contains("/")) bed = bed.split("/")[0].trim();
        if (bed.contains(".")) bed = bed.split("\\.")[0].trim();
        return Integer.parseInt(bed) < BED_MAX;
    }

}
