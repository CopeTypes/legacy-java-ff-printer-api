package me.ghost.printapi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

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

    public static String imageToBase64(File imageFile) {
        try {
            FileInputStream fileInputStream = new FileInputStream(imageFile);
            byte[] imageData = new byte[(int) imageFile.length()];
            fileInputStream.read(imageData);
            fileInputStream.close();
            return Base64.getEncoder().encodeToString(imageData);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
