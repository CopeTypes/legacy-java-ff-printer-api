package slug2k.ffapi.commands.info;

public class LocationInfo {

    public String x;
    public String y;
    public String z;

    public LocationInfo(String replay) {
        //Logger.log("Raw PrinterLocationInfo replay:\n" + replay);
        String[] data = replay.split("\n");
        String[] locData = data[1].split(" ");
        x = locData[0].replace("X:", "").trim();
        y = locData[1].replace("Y:", "").trim();
        z = locData[2].replace("Z:", "").trim();
        //A = data[3]
        //B = data[4]
    }

    @Override
    public String toString() {
        return "X: " + x + " Y: " + y + " Z: " + z;
    }
}
