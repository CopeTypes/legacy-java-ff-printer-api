package me.ghost.printmonitor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.ghost.printapi.util.FileUtil;
import slug2k.ffapi.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class for managing settings used in PrintMonitor
 * @author GhostTypes
 */
public class Config {
    /**
     * The URL of the discord webhook for notifications/progress updates, etc.
     */
    public String webhookUrl = null;
    /**
     * The PrintWatch API key
     */
    public String apiKey = null;

    /**
     * Creates a new instance of Config with its values automatically loaded
     */
    public Config() {
        Path config = Paths.get(FileUtil.getExecutionPath().toString(), "config.json");
        if (!Files.exists(config)) {
            Logger.error("Config.json not found, please run the python script first and configure it.");
            System.exit(-1);
        }
        try {
            String data = FileUtil.readFile(config.toString());
            JsonObject obj = JsonParser.parseString(data).getAsJsonObject();
            JsonObject cfg = obj.getAsJsonObject("Config");
            if (cfg.has("webhookUrl")) webhookUrl = cfg.get("webhookUrl").getAsString();
            else Logger.debug("no webhookUrl key in config.json???");
            if (cfg.has("apiKey")) apiKey = cfg.get("apiKey").getAsString();
            else Logger.debug("no apiKey key in config.json???");
        } catch (IOException e) {
            Logger.error("Exception reading config.json: " + e.getMessage());
        }
    }

}
