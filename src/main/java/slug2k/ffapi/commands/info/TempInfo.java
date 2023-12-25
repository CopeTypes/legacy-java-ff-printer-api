package slug2k.ffapi.commands.info;

import me.ghost.printapi.util.Utils;
import slug2k.ffapi.Logger;

/**
 * Class for handling the printer's temps
 */
public class TempInfo {

    public TempData extruderTemp;
    public TempData bedTemp;

    /**
     * Creates a TempInfo instance from a M105 command replay
     * @param replay M105 command replay
     */
    public TempInfo(String replay) {
        Logger.debug("TempInfo replay:\n" + replay);
        String[] data = replay.split("\n");
        String[] tempData = data[1].split(" ");
        String extruderd = tempData[0].replace("T0:", "").replace("/0.0", "");
        String bedd = tempData[2].replace("B:", "").replace("/0.0", "");
        extruderTemp = new TempData(extruderd);
        bedTemp = new TempData(bedd);
        Logger.debug("Extruder temp is " + extruderTemp.getFull());
        Logger.debug("Bed temp is " + bedTemp.getFull());
    }

    /**
     * Checks if the printer has cooled down enough for part removal
     */
    public boolean isCooled() {
        return bedTemp.getCurrent() <= 40.0D && extruderTemp.getCurrent() <= 200.0D;
    }

    public class TempData {
        public String current = "";
        public String set = "";

        /**
         * Get extruder/bed temps from a M105 command response
         * @param data M105 command response
         */
        public TempData(String data) {
            if (data.contains("/")) { // replay has current/set temps
                String[] splitTemps = data.split("/");
                current = parseTdata(splitTemps[0].trim());
                set = parseTdata(splitTemps[1].trim());
            } else { // replay only has current temp (when printer is idle)
                current = parseTdata(data);
                set = null;
            }
        }

        /**
         * Parses the last part of a temp data string into 'proper' format
         * @param data a temp data string like "123.45/678.90"
         * @return String
         */
        private String parseTdata(String data) {
            if (data.contains(".")) data = data.split("\\.")[0].trim();
            return Utils.roundString(data);
        }

        /**
         * Gets the 'full' temp (current/set)<br>
         * Returns only the temp if there's no set one
         * @return String
         */
        public String getFull() {
            if (set == null) return current;
            return current + "/" + set;
        }

        public double getCurrent() { return Double.parseDouble(current); }
        public double getSet() { return Double.parseDouble(set); }
    }
}
