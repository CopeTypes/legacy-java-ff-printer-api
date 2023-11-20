package me.ghost.printmonitor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.ghost.printapi.util.FileUtil;
import slug2k.ffapi.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    public String webhookUrl = null;
    public String apiKey = null;

    public Config() {
        //Path config = FileUtil.getExecutionPath().resolve("config.json");
        Path config = Paths.get(FileUtil.getExecutionPath().toString(), "config.json");
        Logger.log(config.toString());

        if (!Files.exists(config)) {
            Logger.error("Config.json not found, please run the python script first and configure it.");
            System.exit(-1);
        }
        try {
            String data = FileUtil.readFile(config.toString());
            JsonObject obj = JsonParser.parseString(data).getAsJsonObject();
            JsonObject cfg = obj.getAsJsonObject("Config");
            if (cfg.has("webhookUrl")) webhookUrl = cfg.get("webhookUrl").getAsString();
            if (cfg.has("apiKey")) apiKey = cfg.get("apiKey").getAsString();
        } catch (IOException e) {
            Logger.error("Exception reading config.json: " + e.getMessage());
        }
    }

}
