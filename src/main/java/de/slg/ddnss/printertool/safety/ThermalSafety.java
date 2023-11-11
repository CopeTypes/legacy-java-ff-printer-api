package de.slg.ddnss.printertool.safety;

import de.slg.ddnss.printertool.clients.AdventurerClient;
import de.slg.ddnss.printertool.commands.TempInfo;
import de.slg.ddnss.printertool.exceptions.FlashForgePrinterException;
import me.ghost.printercontrol.util.WebhookUtil;

/**
 * External temperature safety checks.
 * Stops the print automatically if extruder or bed temps exceed a safe limit
 * @author GhostTypes
 */
public class ThermalSafety {

    private final int EXTRUDER_MAX = 250;
    private final int BED_MAX = 100;

    private AdventurerClient client;

    public ThermalSafety() {}

    public void run() throws FlashForgePrinterException {
        if (!areTempsSafe()) {
            //todo would probably be safer to immediately stop the printer and/or cut off power
            //need to see if MCode for either of those things is supported by Flashforge's firmware.
            WebhookUtil.sendMessage("Thermal Safety Alert", "One or more of the printer's temperature values exceeded a safe limit, the current print is being cancelled now.");
            client.stopPrint();
        }
    }

    private boolean areTempsSafe() throws FlashForgePrinterException {
        TempInfo temps = client.getTempInfo();
        if (Integer.parseInt(temps.extruderTemp) >= EXTRUDER_MAX) return false;
        return Integer.parseInt(temps.bedTemp) < BED_MAX;
    }

}
