package me.ghost.printapi;

import me.ghost.printapi.util.FileUtil;
import slug2k.ffapi.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Class for interacting with the PrintWatch API via it's python api
 * @author GhostTypes
 */

//todo port the python api to java so we don't have to rely on this shit

public class PrintMonitorApi {

    private static final String scriptPath = Paths.get(Objects.requireNonNull(FileUtil.getExecutionPath()).toString(), "PrintMonitor.py").toString();

    /**
     * Defect information from a PrintWatch api response
     */
    public static class DefectStatus {
        /**
         * Whether the print currently has a defect
         */
        public boolean defect;
        /**
         * How likely it is the print is failing (>0.6 is considered failing)
         */
        public float score;

        public DefectStatus(boolean defect, float score) {
            this.defect = defect;
            this.score = score;
        }

        /**
         * Creates a DefectStatus instance with the result of defect.txt
         * @param data The result of defect.txt, as a String
         */
        public DefectStatus(String data) {
            String[] d = data.split("\n");
            try {
                defect = Boolean.parseBoolean(d[0]);
                score = Float.parseFloat(d[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                Logger.error("Error creating DefectStatus from data string: " + e.getMessage());
                defect = false;
                score = -1;
            }
        }

    }

    /**
     * Gets the DefectStatus for the current print job
     *
     * @return DefectStatus
     * @throws IOException Error with python script
     * @see PrintMonitorApi.DefectStatus
     */
    public static DefectStatus getDefectStatus() throws IOException {
        if (!checkScriptPath()) throw new IOException("Cannot check for defect, PrintMonitor.py not found.");
        boolean ran = runCommand("check_defect");
        if (!ran) {
            Logger.error("getDefectStatus() command error");
            return null;
        }
        File resultFile = new File(FileUtil.getExecutionPath(), "defect.txt");
        if (!Files.exists(resultFile.toPath())) throw new IOException("Cannot check for defect, result file not found after executing command."); // this should never happen

        try {
            String data = Files.readString(resultFile.toPath());
            return new DefectStatus(data);
        } catch (Exception e) {
            Logger.error("getDefectStatus() unable to read defect.txt: " + e.getMessage());
            return new DefectStatus(false, -1);
        }

        //String result = Files.readString(resultFile.toPath());
        //String[] results = result.split("\n");
        //try { return new DefectStatus(Boolean.parseBoolean(results[0]), Float.parseFloat(results[1])); }
        //catch (ArrayIndexOutOfBoundsException e) { // this also should never happen
        //    Logger.error("Error getting DefectStatus: " + e.getMessage());
        //    return null;
        //}
    }

    /**
     * Sets the current printer/ticket id used in the python script<br>
     * Should only be used once per session
     *
     * @return boolean
     */
    public static boolean refreshUUIDs() {
        return runCommand("new_uuids");
    }

    /**
     * Check if the python script exists in the same folder as this
     * @return boolean
     */
    private static boolean checkScriptPath() {
        if (!Files.exists(Path.of(scriptPath))) {
            // todo automatically download this once it's open source
            Logger.error("PrintMonitorApi error, PrintMonitor.py not found at: " + scriptPath);
            Logger.log("Place PrintMonitor.py at the given path above, and make sure to configure the settings.");
            return false;
        }
        return true;
    }

    /**
     * Runs a command with the python script, and waits for it to complete
     * @param command The command to run
     * @return boolean
     */
    private static boolean runCommand(String command) {
        if (!checkScriptPath()) return false;
        ProcessBuilder pb = new ProcessBuilder("python", scriptPath, command);
        pb.redirectErrorStream(true);
        try {
            Process p = pb.start();
            try {
                return p.waitFor(25, TimeUnit.SECONDS); //in case the python script hangs
            } catch (InterruptedException e) {
                if (command.equalsIgnoreCase("defect_check")) {
                    Logger.debug("runCommand defect_check timed out, refreshing printer uuid's");
                    refreshUUIDs(); // this *should* help reduce api errors (like 212)
                }
                Logger.error("runCommand timed out with command: " + command);
                Logger.error("Error: " + e.getMessage());
                return false;
            }
        } catch (IOException e) {
            Logger.error("PrintMonitorApi error trying to run command: " + command);
            Logger.error(e.getMessage());
            Logger.log("scriptPath=" + scriptPath);
            return false;
        }
    }
}
