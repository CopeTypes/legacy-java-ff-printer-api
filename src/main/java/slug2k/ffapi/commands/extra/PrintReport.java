package slug2k.ffapi.commands.extra;

import slug2k.ffapi.commands.info.TempInfo;
import slug2k.ffapi.commands.status.EndstopStatus;
import slug2k.ffapi.commands.status.PrintStatus;

public class PrintReport {
    public LayerProgress layerProgress;
    public String extruderTemp;
    public String bedTemp;

    public String currentFile;

    public PrintReport(EndstopStatus endstopStatus, PrintStatus printStatus, TempInfo tempInfo) {
        layerProgress = printStatus.layerProgress;
        extruderTemp = tempInfo.extruderTemp;
        bedTemp = tempInfo.bedTemp;
        currentFile = endstopStatus.currentFile;
    }

}
