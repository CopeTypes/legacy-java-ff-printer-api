package slug2k.ffapi.commands.extra;

public class LayerProgress {
    public int currentLayer, totalLayers;
    public double progress;

    public LayerProgress(int currentLayer, int totalLayers) {
        this.currentLayer = currentLayer;
        this.totalLayers = totalLayers;
        progress = ((double) currentLayer / totalLayers) * 100;
    }
}
