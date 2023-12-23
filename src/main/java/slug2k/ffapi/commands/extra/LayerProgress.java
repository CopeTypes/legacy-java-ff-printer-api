package slug2k.ffapi.commands.extra;

import slug2k.ffapi.Logger;

/**
 * Class for handling the current layer progress
 * @see slug2k.ffapi.commands.status.PrintStatus
 */
public class LayerProgress {
    public int currentLayer, totalLayers;
    public double progress;

    public LayerProgress(int currentLayer, int totalLayers) {
        if (currentLayer == 0 || currentLayer == -1 || totalLayers == 0 || totalLayers == -1) {
            Logger.debug("Caught bad LayerProgress");
            this.currentLayer = -1;
            this.totalLayers = -1;
            this.progress = 0.0D;
            return;
        }
        this.currentLayer = currentLayer;
        this.totalLayers = totalLayers;
        progress = ((double) currentLayer / totalLayers) * 100;
        adjustProgress();

    }

    private void adjustProgress() {
        if (progress >= 4.0D && progress <= 25.0D) progress -= 3.0D; //25.0D might need to be tweaked
    }
}
