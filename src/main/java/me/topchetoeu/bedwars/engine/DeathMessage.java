package me.topchetoeu.bedwars.engine;

import java.io.File;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import me.topchetoeu.bedwars.Main;

public class DeathMessage {
    public static String getMsg(OfflinePlayer offended, OfflinePlayer offender, Map<String, String> messages, String msg) {
        return String.format(messages.get(msg), offended.getName(), offender == null ? "" : offender.getName());
    }
    
    public static String getMessage(DamageCause cause, OfflinePlayer offended, OfflinePlayer offender, Map<String, String> messages) {
        String name = "generic";
        
        switch (cause) {
        case BLOCK_EXPLOSION:
        case ENTITY_EXPLOSION:
            name = "explosion";
            break;
        case DROWNING:
            name = "drowned";
            break;
        case FALL:
            name = "fall";
            break;
        case FIRE:
            name = "fire";
            break;
        case LAVA:
            name = "lava";
            break;
        case MAGIC:
            name = "magic";
            break;
        case POISON:
            name = "poison";
            break;
        case PROJECTILE:
            name = "projectile";
            break;
        case VOID:
            name = "void";
            break;
        default:
            name = "generic";
            break;
        }
        
        if (offender == null) return getMsg(offended, offender, messages, name);
        else if (offender == offended) return getMsg(offended, offender, messages, name + "-suicide");
        else return getMsg(offended, offender, messages, name + "-byPlayer");
    }
    
    public static Map<String, String> getMessages(OfflinePlayer player) {
        return YamlConfiguration
            .loadConfiguration(new File(Main.getInstance().getDataFolder(), "death-messages.yml"))
            .getValues(true)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(v -> v.getKey(), v -> (String)v.getValue()));
    }
}
