package slug2k.ffapi.safety;

import slug2k.ffapi.clients.PrinterClient;
import slug2k.ffapi.commands.info.TempInfo;
import slug2k.ffapi.exceptions.PrinterException;

/**
 * External temperature safety checks.
 * Stops the print automatically if extruder or bed temps exceed a safe limit
 * @author GhostTypes
 */
public class ThermalSafety {

    private final int EXTRUDER_MAX = 250;
    private final int BED_MAX = 100;

    private PrinterClient client;

    public boolean safe = true;

    public ThermalSafety(PrinterClient client) {
        this.client = client;
    }

    public boolean areTempsSafe() throws PrinterException {
        TempInfo temps = client.getTempInfo();
        String extruder = temps.extruderTemp.current;
        //if (extruder.contains("/")) extruder = extruder.split("/")[0].trim();
        //if (extruder.contains(".")) extruder = extruder.split("\\.")[0].trim();
        if (Integer.parseInt(extruder) >= EXTRUDER_MAX) return false;
        String bed = temps.bedTemp.current;
        //if (bed.contains("/")) bed = bed.split("/")[0].trim();
        //if (bed.contains(".")) bed = bed.split("\\.")[0].trim();
        return Integer.parseInt(bed) < BED_MAX;
    }

}
