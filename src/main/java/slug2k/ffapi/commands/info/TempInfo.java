package slug2k.ffapi.commands.info;


public class TempInfo {

    public String extruderTemp;
    public String bedTemp;

    public TempInfo(String replay) {
        //Logger.log("Raw replay for PrinterTempInfo:\n" + replay);
        String[] data = replay.split("\n");
        String[] tempData = data[1].split(" ");
        extruderTemp = tempData[0].replace("T0:", "").replace("/0.0", "");
        bedTemp = tempData[2].replace("B:", "").replace("/0.0", "");
        //todo the parsing for this should be done better
        //should have different vars for current/set temp for the bed & extruder
    }

}
