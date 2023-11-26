package slug2k.ffapi.commands.status;

import slug2k.ffapi.Logger;
import slug2k.ffapi.commands.extra.LayerProgress;

/**
 * Class for progress information from the current print
 */
public class PrintStatus {

    public String byteProgress;
    /**
     * The layer progress like "12/34"
     */
    public String rawLayerProgress;

    public LayerProgress layerProgress;

    public PrintStatus(String replay) {
        Logger.debug("PrintStatus replay:\n" + replay);
        String[] data = replay.split("\n");
        // this should be safe because it returns default values when not printing
        byteProgress = data[1].replace("SD printing byte ", "").trim();
        String layerData = data[2].replace("Layer: ", "").trim();
        rawLayerProgress = layerData;
        String[] ld = layerData.split("/");
        layerProgress = new LayerProgress(Integer.parseInt(ld[0]), Integer.parseInt(ld[1]));
    }

    /**
     * Gets the current layer being worked on
     * @return Integer
     */
    public Integer currentLayer() {
        return layerProgress.currentLayer;
    }

    /**
     * Gets the total numbers of layers in the current print
     * @return Integer
     */
    public Integer totalLayers() {
        return layerProgress.totalLayers;
    }


}
