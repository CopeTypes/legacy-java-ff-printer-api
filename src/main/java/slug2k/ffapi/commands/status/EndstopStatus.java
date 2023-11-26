package slug2k.ffapi.commands.status;

import slug2k.ffapi.Logger;
import slug2k.ffapi.enums.MachineStatus;
import slug2k.ffapi.enums.MoveMode;

/**
 * Gets the current MachineStatus, MoveMode, Led state, and current file name (if printing)
 * @author GhostTypes
 */
public class EndstopStatus {

    //Endstop: stuff about x/y max and zmin that is useless
    public MachineStatus machineStatus;
    //public String moveMode;
    public MoveMode moveMode;

    public boolean ledEnabled;

    public String currentFile;

    public EndstopStatus(String replay) {
        Logger.debug("PrinterEndstopStatus replay:\n" + replay);
        String[] data = replay.split("\n");
        //todo this threw ArrayIndexOutOfBounds (2 out of bounds for length 1) and not sure why
        //made a temporary fix in PrinterClient but should really figure out why that happens/happened
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
        //status = data[4] useless
        int led = Integer.parseInt(data[5].replace("LED: ", "").trim());
        ledEnabled = led == 1;
        currentFile = data[6].replace("CurrentFile: ", "").replace(".gx", "").replace(".gcode", "").stripTrailing();
        if (currentFile.replace(" ", "").isEmpty()) currentFile = "None";
    }
}
