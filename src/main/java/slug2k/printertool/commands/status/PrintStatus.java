package slug2k.printertool.commands.status;

public class PrintStatus {

    public String byteProgress;
    public String layerProgress;

    public PrintStatus(String replay) {
        //Logger.log("Raw PrintStatus replay:\n" + replay);
        String[] data = replay.split("\n");
        // this should be safe because it returns default values when not printing
        byteProgress = data[1].replace("SD printing byte ", "").trim();
        layerProgress = data[2].replace("Layer: ", "").trim();
    }



}
