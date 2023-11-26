package slug2k.ffapi.commands.info;

import slug2k.ffapi.Logger;

public class LocationInfo {

    public String x;
    public String y;
    public String z;

    /**
     * Creates a LocationInfo instance from a M114 command replay
     * @param replay The M114 command replay
     */
    public LocationInfo(String replay) {
        Logger.debug("PrinterLocationInfo replay:\n" + replay);
        String[] data = replay.split("\n");
        String[] locData = data[1].split(" ");
        x = locData[0].replace("X:", "").trim();
        y = locData[1].replace("Y:", "").trim();
        z = locData[2].replace("Z:", "").trim();
    }

    @Override
    public String toString() {
        return "X: " + x + " Y: " + y + " Z: " + z;
    }
}
