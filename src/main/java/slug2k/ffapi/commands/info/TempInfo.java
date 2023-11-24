package slug2k.ffapi.commands.info;


import me.ghost.printapi.util.Utils;

public class TempInfo {

    //public String extruderTemp;
    //public String bedTemp;

    public TempData extruderTemp;
    public TempData bedTemp;

    public TempInfo(String replay) {
        //Logger.log("Raw replay for PrinterTempInfo:\n" + replay);
        String[] data = replay.split("\n");
        String[] tempData = data[1].split(" ");
        String extruderd = tempData[0].replace("T0:", "").replace("/0.0", "");
        String bedd = tempData[2].replace("B:", "").replace("/0.0", "");
        extruderTemp = new TempData(extruderd);
        bedTemp = new TempData(bedd);
    }

    public class TempData {
        public String current = "";
        public String set = "";

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
         * @return
         */
        private String parseTdata(String data) {
            if (data.contains(".")) data = data.split("\\.")[0].trim();
            return Utils.roundString(data);
        }

        public String getFull() {
            if (set == null) return current;
            return current + "/" + set;
        }
    }
}
