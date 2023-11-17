package me.ghost.printapi.util;

import me.ghost.printapi.Main;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    public static Path getExecutionPath() {
        return Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent().toAbsolutePath().normalize();
    }

}
