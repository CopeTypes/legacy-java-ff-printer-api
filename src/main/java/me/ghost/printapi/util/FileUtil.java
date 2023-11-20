package me.ghost.printapi.util;

import me.ghost.printapi.Main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    public static Path getExecutionPath() {
        return Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent().toAbsolutePath().normalize();
    }

    public static String readFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

}
