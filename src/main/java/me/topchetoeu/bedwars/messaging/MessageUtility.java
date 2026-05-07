package me.topchetoeu.bedwars.messaging;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

public class MessageUtility {
    private static YamlConfiguration config;

    private MessageUtility() {

    }

    public static void load(File file) throws IOException {
        config = YamlConfiguration.loadConfiguration(file);
        config.save(file);
    }

    public static MessageParser parser(String path) {
        Object obj = config.get(path);
        if (obj == null) return new MessageParser(null);
        else return new MessageParser(obj.toString());
    }
}
