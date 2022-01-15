package me.topchetoeu.bedwars.commandUtility;

import java.util.Map;

import org.bukkit.command.CommandSender;

public interface CommandExecutor {
    String execute(CommandSender sender, Command cmd, Map<String, Object> args);
}
