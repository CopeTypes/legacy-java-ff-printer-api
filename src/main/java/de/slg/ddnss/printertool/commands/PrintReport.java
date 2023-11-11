package de.slg.ddnss.printertool.commands;

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
