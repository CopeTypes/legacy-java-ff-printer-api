package slug2k.ffapi.commands.status;

import slug2k.ffapi.Logger;

/**
 * Class for progress information from the current print
 */
public class PrintStatus {

    private final String SD_CURRENT;
    private final String SD_TOTAL;
    private final String LAYER_CURRENT;
    private final String LAYER_TOTAL;

    /**
     * Creates a PrintStatus instance from an M27 command replay
     * @param replay M27 command replay
     */
    public PrintStatus(String replay) {
        Logger.debug("PrintStatus replay:\n" + replay);
        String[] data = replay.split("\n");
        // this should be safe because it returns default values when not printing

        String sdProgress = data[1].replace("SD printing byte ", "").trim();
        String[] sdProgressData = sdProgress.split("/");
        SD_CURRENT = sdProgressData[0].trim();
        SD_TOTAL = sdProgressData[1].trim();

        String layerProgress = data[2].replace("Layer: ", "").trim();
        String[] lpData = layerProgress.split("/");
        LAYER_CURRENT = lpData[0].trim();
        LAYER_TOTAL = lpData[1].trim();
    }

    public int getPrintPercent() {
        int current = Integer.parseInt(SD_CURRENT);
        int total = Integer.parseInt(SD_TOTAL);
        double perc = (current / (double) total) * 100;
        return (int) Math.round(perc);
    }

    public boolean isComplete() {
        if (SD_CURRENT.equalsIgnoreCase(SD_TOTAL)) return true;
        return LAYER_CURRENT.equalsIgnoreCase(LAYER_TOTAL);
    }




}
