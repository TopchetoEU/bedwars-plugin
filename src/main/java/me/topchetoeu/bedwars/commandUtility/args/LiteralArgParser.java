package me.topchetoeu.bedwars.commandUtility.args;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.CommandSender;

public class LiteralArgParser implements ArgParser {
    private final String literal;
    private final List<String> aliases = new ArrayList<>();

    public String getLiteral() {
        return literal;
    }
    public List<String> getAliases() {
        return Collections.unmodifiableList(aliases);
    }

    @Override
    public ArgParserRes parse(CommandSender sender, List<String> remainingArgs) {
        if (!remainingArgs.get(0).equals(literal) && !aliases.contains(remainingArgs.get(0))) return ArgParserRes.fail();
        else return ArgParserRes.takenOne();
    }

    @Override
    public void addCompleteSuggestions(CommandSender sender, List<String> args, Suggestions elements) {
        String arg = args.get(0);

        elements.addSuggestions(aliases.stream().filter(v -> v.startsWith(arg)));
        if (literal.startsWith(arg)) elements.addSuggestion(literal);
    }

    public LiteralArgParser(String lit) {
        this.literal = lit;
    }
    public LiteralArgParser(String lit, String ...aliases) {
        this.literal = lit;
        Collections.addAll(this.aliases, aliases);
    }
    public LiteralArgParser(String lit, Collection<String> aliases) {
        this.literal = lit;
        this.aliases.addAll(aliases);
    }
}
