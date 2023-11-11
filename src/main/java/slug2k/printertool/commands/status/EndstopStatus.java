package slug2k.printertool.commands.status;

import slug2k.printertool.Logger;
import slug2k.printertool.enums.MachineStatus;

/**
 * @author GhostTypes
 * Gets the current MachineStatus, MoveMode, Led state, and current file name (if printing)
 */
public class EndstopStatus {

    //Endstop: stuff about x/y max and zmin that is useless
    public MachineStatus machineStatus;
    public String moveMode;

    public boolean ledEnabled;

    public String currentFile;

    public EndstopStatus(String replay) {
        //Logger.log("PrinterEndstopStatus raw replay:\n" + replay);
        String[] data = replay.split("\n");
        //endstop = data[1] useless
        String machineStatus = data[2].replace("MachineStatus: ", "").trim();
        switch (machineStatus) {
            case "BUILDING_FROM_SD" -> this.machineStatus = MachineStatus.BUILDING_FROM_SD;
            case "BUILDING_COMPLETED" -> this.machineStatus = MachineStatus.BUILDING_COMPLETED;
            case "READY" -> this.machineStatus = MachineStatus.READY;
            default -> Logger.log("Encountered MachineStatus not currently in enum: " + machineStatus);
        }
        moveMode = data[3].replace("MoveMode: ", "").trim();
        //status = data[4] useless
        int led = Integer.parseInt(data[5].replace("LED: ", "").trim());
        ledEnabled = led == 1;
        currentFile = data[6].replace("CurrentFile: ", "").stripTrailing();
        if (currentFile.replace(" ", "").isEmpty()) currentFile = "None";
    }
}
