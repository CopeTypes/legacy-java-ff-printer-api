package me.ghost.printapi.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {

    /**
     * Gets the folder where the jar was launched from
     * @return File
     */
    public static File getExecutionPath() {
        try { return new File(FileUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile(); } catch (URISyntaxException e) { return null; }
    }

    /**
     * Reads the provided file to a string (if it's a text file)
     * @param path The path to the target file
     * @return String
     * @throws IOException Unable to read file/Error reading file
     */
    public static String readFile(String path) throws IOException { return new String(Files.readAllBytes(Paths.get(path))); }


}
