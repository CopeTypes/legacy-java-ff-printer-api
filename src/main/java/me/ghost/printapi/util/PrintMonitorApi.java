package me.ghost.printapi.util;

import slug2k.ffapi.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrintMonitorApi {

    private static String scriptPath = Paths.get(FileUtil.getExecutionPath().toString(), "config.json").toString();

    public static class DefectStatus {
        public boolean defect;
        public float score;

        public DefectStatus(boolean defect, float score) {
            this.defect = defect;
            this.score = score;
        }

    }

    /**
     * Gets the DefectStatus for the current print job
     * @return DefectStatus
     * @see me.ghost.printapi.util.PrintMonitorApi.DefectStatus
     * @throws IOException Error with python script
     */
    //todo need to implement this python script side
    //todo need to add python script to repo
    public static DefectStatus getDefectStatus() throws IOException {
        if (!checkScriptPath()) throw new IOException("Cannot check for defect, PrintMonitor.py not found.");
        runCommand("check_defect");
        Path resultFile = Paths.get(FileUtil.getExecutionPath().toString(), "defect.txt");
        if (!Files.exists(resultFile)) throw new IOException("Cannot check for defect, result file not found after executing command.");
        String result = Files.readString(resultFile);
        String[] results = result.split("\n");
        return new DefectStatus(Boolean.parseBoolean(results[0]), Float.parseFloat(results[1]));
    }

    private static boolean checkScriptPath() {
        if (!Files.exists(Path.of(scriptPath))) {
            // todo automatically download this once it's open source
            Logger.error("PrintMonitorApi error, PrintMonitor.py not found at: " + scriptPath);
            Logger.log("Place PrintMonitor.py at the given path above, and make sure to configure the settings.");
            return false;
        }
        return true;
    }

    private static boolean runCommand(String command) {
        if (!checkScriptPath()) return false;
        ProcessBuilder pb = new ProcessBuilder("python", scriptPath.toString(), command);
        try {
            Process p = pb.start();
            return p.waitFor() == 0;
        } catch (IOException | InterruptedException e) {
            Logger.error("PrintMonitorApi error trying to run command: " + command);
            Logger.error(e.getMessage());
            Logger.log("scriptPath=" + scriptPath);
            return false;
        }
    }
}
