package me.ghost.printapi.util;

import me.ghost.printapi.Main;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    public static File getExecutionPath() {
        try {
            return new File(FileUtil.class.getProtectionDomain().getCodeSource().getLocation()
                    .toURI()).getParentFile();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

}
