package slug2k.printertool.commands.extra;

import slug2k.printertool.commands.info.TempInfo;
import slug2k.printertool.commands.status.EndstopStatus;
import slug2k.printertool.commands.status.PrintStatus;

public class PrintReport {
    public String layerProgress;
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
