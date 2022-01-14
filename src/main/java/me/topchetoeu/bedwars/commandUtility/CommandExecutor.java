package me.topchetoeu.bedwars.commandUtility;

import org.bukkit.command.CommandSender;

public interface CommandExecutor {
	void execute(CommandSender sender, Command cmd, String alias, String[] args);
}
