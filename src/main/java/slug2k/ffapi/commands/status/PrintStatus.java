package slug2k.ffapi.commands.status;

import slug2k.ffapi.commands.extra.LayerProgress;

public class PrintStatus {

    public String byteProgress;
    public String rawLayerProgress;
    public LayerProgress layerProgress;

    public PrintStatus(String replay) {
        //Logger.log("Raw PrintStatus replay:\n" + replay);
        String[] data = replay.split("\n");
        // this should be safe because it returns default values when not printing
        byteProgress = data[1].replace("SD printing byte ", "").trim();
        String layerData = data[2].replace("Layer: ", "").trim();
        rawLayerProgress = layerData;
        String[] ld = layerData.split("/");
        layerProgress = new LayerProgress(Integer.parseInt(ld[0]), Integer.parseInt(ld[1]));
    }



}
