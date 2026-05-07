package me.topchetoeu.bedwars.commandUtility.args;

import java.util.List;

import org.bukkit.command.CommandSender;

public class IntArgParser implements ArgParser {
    @Override
    public ArgParserRes parse(CommandSender sender, List<String> remainingArgs) {
        try {
            return ArgParserRes.takenOne(Integer.parseInt(remainingArgs.get(0)));
        }
        catch (NumberFormatException e) {
            return ArgParserRes.error("Invalid number format.");
        }
    }

    @Override
    public void addCompleteSuggestions(CommandSender sender, List<String> args, Suggestions suggestions) {
        suggestions.addSuggestion("0");
    }
}
