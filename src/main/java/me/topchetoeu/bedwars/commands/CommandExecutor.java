package me.topchetoeu.bedwars.commands;

import java.util.Map;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.chat.BaseComponent;

public interface CommandExecutor {
    BaseComponent[] execute(CommandSender sender, Command cmd, Map<String, Object> args);
}
