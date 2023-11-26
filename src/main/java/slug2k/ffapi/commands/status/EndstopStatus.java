package slug2k.ffapi.commands.status;

import slug2k.ffapi.Logger;
import slug2k.ffapi.enums.MachineStatus;
import slug2k.ffapi.enums.MoveMode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gets the current MachineStatus, MoveMode, Led state, and current file name (if printing)
 * @author GhostTypes
 */
public class EndstopStatus {

    public Endstop endstop;
    public MachineStatus machineStatus;
    public MoveMode moveMode;

    public Status status;

    public boolean ledEnabled;

    public String currentFile;

    /**
     * Creates an EndstopStatus instance from a M119 command replay
     * @param replay M119 command replay
     */
    public EndstopStatus(String replay) {
        Logger.debug("PrinterEndstopStatus replay:\n" + replay);
        String[] data = replay.split("\n");
        //todo this threw ArrayIndexOutOfBounds (2 out of bounds for length 1) and not sure why
        //made a temporary fix in PrinterClient but should really figure out why that happens/happened
        endstop = new Endstop(data[1]);
        String machineStatus = data[2].replace("MachineStatus: ", "").trim();
        if (machineStatus.contains("BUILDING_FROM_SD")) this.machineStatus = MachineStatus.BUILDING_FROM_SD;
        else if (machineStatus.contains("BUILDING_COMPLETED")) this.machineStatus = MachineStatus.BUILDING_COMPLETED;
        else if (machineStatus.contains("READY")) this.machineStatus = MachineStatus.READY;
        else {
            Logger.log("Encountered unknown MachineStatus: " + machineStatus);
            this.machineStatus = MachineStatus.DEFAULT;
        }
        String moveM = data[3].replace("MoveMode: ", "").trim();
        if (moveM.contains("MOVING")) this.moveMode = MoveMode.MOVING;
        else if (moveM.contains("PAUSED")) this.moveMode = MoveMode.PAUSED;
        else if (moveM.contains("READY")) this.moveMode = MoveMode.READY;
        else {
            Logger.log("Encountered unknown MoveMode: " + moveM);
            this.moveMode = MoveMode.DEFAULT;
        }
        status = new Status(data[4]);
        int led = Integer.parseInt(data[5].replace("LED: ", "").trim());
        ledEnabled = led == 1;
        currentFile = data[6].replace("CurrentFile: ", "").replace(".gx", "").replace(".gcode", "").stripTrailing();
        if (currentFile.replace(" ", "").isEmpty()) currentFile = "None";
    }

    public static class Status {
        public int S, L, J, F;

        /**
         * Creates a Status instance from M119 data<br>
         * Ex: Status: S:1 L:0 J:0 F:0
         * @param data String from M119 command containing status data
         */
        public Status(String data) {
            Logger.debug("Status(EndstopStatus) data: " + data);
            S = extractValue(data, "S");
            L = extractValue(data, "L");
            J = extractValue(data, "J");
            F = extractValue(data, "F");
        }

        /**
         * Extracts the value of an individual item in a status string
         * @param input A string like:<br>Status: S:1 L:0 J:0 F:0
         * @param key Which part of the status string to extract (ex: "S")
         * @return
         */
        private int extractValue(String input, String key) {
            String pattern = key + ":(\\d+)";
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(input);
            if (matcher.find()) return Integer.parseInt(matcher.group(1));
            else return -1;
        }
    }

    public static class Endstop {
        public int Xmax, Ymax, Zmin; // these seem to just refer to the actual max for the axes
        // some other thing online said these were used to check home positions for an axes, but that's probably for older printers
        // on the 5M series, X & Y max are always 100, and Z-min is always 0

        /**
         * Creates an Endstop instance from M119 data<br>
         * Ex: Endstop: X-max: 110 Y-max: 110 Z-min: 0
         * @param data String from M119 command containing endstop data
         */
        public Endstop(String data) {
            Logger.debug("Endstop(EndstopStatus) data: " + data);
            Xmax = getValue(data, "X-max");
            Ymax = getValue(data, "Y-max");
            Zmin = getValue(data, "Z-min");
        }

        /**
         * Extracts the max (min for z) for a given axes
         * @param input A string like:<br>Endstop: X-max: 110 Y-max: 110 Z-min: 0
         * @param key Which axes to extract (ex: "X-max")
         * @return Integer
         */
        private int getValue(String input, String key) {
            String pattern = key + ": (\\d+)";
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(input);
            if (matcher.find()) return Integer.parseInt(matcher.group(1));
            else return -1;
        }
    }
}
