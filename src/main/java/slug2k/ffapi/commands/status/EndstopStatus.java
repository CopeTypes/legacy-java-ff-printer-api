package slug2k.ffapi.commands.status;

import slug2k.ffapi.Logger;
import slug2k.ffapi.enums.MachineStatus;
import slug2k.ffapi.enums.MoveMode;

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
            try {
                String[] sd = data.replace("Status: ", "").split(" ");
                S = Integer.parseInt(sd[0].replace("S:", "").trim());
                L = Integer.parseInt(sd[1].replace("L:", "").trim());
                J = Integer.parseInt(sd[1].replace("J:", "").trim());
                F = Integer.parseInt(sd[1].replace("F:", "").trim());
            } catch (ArrayIndexOutOfBoundsException e) {
                Logger.error("Error parsing status data (EndstopStatus): " + e.getMessage());
                S = -1;
                L = -1;
                J = -1;
                F = -1;
            }
        }
    }

    public static class Endstop {
        public boolean Xmax, Ymax, Zmax;

        /**
         * Creates an Endstop instance from M119 data<br>
         * Ex: Endstop: X-max: 110 Y-max: 110 Z-min: 0
         * @param data String from M119 command containing endstop data
         */
        public Endstop(String data) {
            Logger.debug("Endstop(EndstopStatus) data: " + data);
            try {
                String[] ed = data.replace("Endstop: ", "").split(" ");
                int xm = Integer.parseInt(ed[0].replace("X-max", "").trim());
                int ym = Integer.parseInt(ed[0].replace("Y-max", "").trim());
                int zm = Integer.parseInt(ed[0].replace("Z-max", "").trim());
                //todo need to verify what the values actually are when everything is homed
                Xmax = xm == 1;
                Ymax = ym == 1;
                Zmax = zm == 1;
            } catch (ArrayIndexOutOfBoundsException e) {
                Logger.error("Errpor parsing Endstop data (EndstopStatus): " + e.getMessage());
                Xmax = false;
                Ymax = false;
                Zmax = false;
            }
        }
    }

    /**
     * Checks if the X Axis is at it's home position
     * @return boolean
     */
    public boolean isXHome() { return endstop.Xmax; }
    /**
     * Checks if the Y Axis is at it's home position
     * @return boolean
     */
    public boolean isYHome() { return endstop.Ymax; }
    /**
     * Checks if the Z Axis is at it's home position
     * @return boolean
     */
    public boolean isZHome() { return endstop.Zmax; }
    /**
     * Checks if all the axis are homed
     * @return boolean
     */
    public boolean isHome() { return isXHome() && isYHome() && isZHome();}
}
