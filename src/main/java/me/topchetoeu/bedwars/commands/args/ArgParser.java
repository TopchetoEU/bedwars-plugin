package me.topchetoeu.bedwars.commands.args;

import java.util.List;

import org.bukkit.command.CommandSender;

public interface ArgParser {
    ArgParserRes parse(CommandSender sender, List<String> remainingArgs);
    void addCompleteSuggestions(CommandSender sender, List<String> args, Suggestions suggestions);
}
