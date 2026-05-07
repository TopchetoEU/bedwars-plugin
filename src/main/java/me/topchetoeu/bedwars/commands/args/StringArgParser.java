package me.topchetoeu.bedwars.commands.args;

import java.util.List;

import org.bukkit.command.CommandSender;

public class StringArgParser implements ArgParser {
    private final boolean greeedy;

    public boolean isGreedy() {
        return greeedy;
    }

    @Override
    public ArgParserRes parse(CommandSender sender, List<String> remainingArgs) {
        if (greeedy) return ArgParserRes.takenMany(remainingArgs.size(), String.join(" ", remainingArgs));
        else return ArgParserRes.takenOne(remainingArgs.get(0));
    }

    @Override
    public void addCompleteSuggestions(CommandSender sender, List<String> args, Suggestions suggestions) {
    }
    
    public StringArgParser(boolean greedy) {
        greeedy = greedy;
    }
}
