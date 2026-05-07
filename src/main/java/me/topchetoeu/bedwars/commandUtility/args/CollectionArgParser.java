package me.topchetoeu.bedwars.commandUtility.args;

import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;

public class CollectionArgParser implements ArgParser {
    private final boolean caseInsensitive;
    private final CollectionProvider provider;

    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }
    public boolean isCaseSensitive() {
        return !caseInsensitive;
    }

    public CollectionProvider getProvider() {
        return provider;
    }

    @Override
    public ArgParserRes parse(CommandSender sender, List<String> remainingArgs) {
        String arg = remainingArgs.get(0);
        Map<String, Object> map = provider.get();
        if (caseInsensitive) arg = arg.toLowerCase();
        if (map.containsKey(arg)) return ArgParserRes.takenOne(map.get(arg));
        else return ArgParserRes.error("Unknown element '" + arg + "'.");
    }

    public CollectionArgParser addElement(String element, Object parsesTo) {
        if (caseInsensitive) element = element.toLowerCase();
        Map<String, Object> map = provider.get();
        map.put(element, parsesTo);
        return this;
    }
    public CollectionArgParser addElements(Map<String, Object> parseTable) {
        for (Map.Entry<String, Object> a : parseTable.entrySet()) {
            if (caseInsensitive) addElement(a.getKey().toLowerCase(), a.getValue());
            else addElement(a.getKey(), a.getValue());
        }
        return this;
    }

    @Override
    public void addCompleteSuggestions(CommandSender sender, List<String> args, Suggestions suggestions) {
        String arg;
        if (caseInsensitive) arg = args.get(0).toLowerCase();
        else arg = args.get(0);
        Map<String, Object> map = provider.get();
        List<String> _suggestions = map.keySet().stream().filter(v -> v.startsWith(arg)).toList();
        suggestions.addSuggestions(_suggestions);
        if (_suggestions.size() == 0) suggestions.error("Unknown element '" + arg + "'.");
    }

    public CollectionArgParser(CollectionProvider provider, boolean caseInsensitive) {
        this.provider = provider;
        this.caseInsensitive = caseInsensitive;
    }
}
