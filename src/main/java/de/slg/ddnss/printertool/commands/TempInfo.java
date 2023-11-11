package de.slg.ddnss.printertool.commands;


public class TempInfo {

    public String extruderTemp;
    public String bedTemp;

    public TempInfo(String replay) {
        //Logger.log("Raw replay for PrinterTempInfo:\n" + replay);
        String[] data = replay.split("\n");
        String[] tempData = data[1].split(" ");
        extruderTemp = tempData[0].replace("T0:", "").replace("/0.0", "");
        bedTemp = tempData[2].replace("B:", "").replace("/0.0", "");
    }

}
