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

    private int EXTRUDER_MAX = 250;
    private int BED_MAX = 100;

    private PrinterClient client;

    public boolean safe = true;

    /**
     * Creates a ThermalSafety instance with the default values<br>
     * 250C Max for the extruder, and 100C for the print bed
     * @param client PrinterClient instance
     */
    public ThermalSafety(PrinterClient client) {
        this.client = client;
    }

    /**
     * Creates a ThermalSafety instance with custom max temps<br>
     * It should be considered unsafe to exceed the default values by +5C
     * @param client PrinterClient instance
     * @param EXTRUDER_MAX Maximum allowed extruder temp
     * @param BED_MAX Maximum allowed bed temp
     */
    public ThermalSafety(PrinterClient client, int EXTRUDER_MAX, int BED_MAX) {
        this.client = client;
        this.EXTRUDER_MAX = EXTRUDER_MAX;
        this.BED_MAX = BED_MAX;
    }

    /**
     * Checks if the current printer temps are safe
     * @return Boolean
     * @throws PrinterException Communication error with printer
     */
    public boolean areTempsSafe() throws PrinterException {
        TempInfo temps = client.getTempInfo();
        String extruder = temps.extruderTemp.current;
        if (Integer.parseInt(extruder) >= EXTRUDER_MAX) return false;
        String bed = temps.bedTemp.current;
        return Integer.parseInt(bed) < BED_MAX;
    }

}
