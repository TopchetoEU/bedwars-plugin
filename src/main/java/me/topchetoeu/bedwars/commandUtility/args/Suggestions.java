package me.topchetoeu.bedwars.commandUtility.args;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Suggestions {
    private final List<String> suggestions = new ArrayList<>();
    private String error = null;

    public boolean addSuggestion(String suggestion) {
        return suggestions.add(suggestion);
    }
    public void addSuggestions(Collection<String> suggestions) {
        for (String suggestion : suggestions) addSuggestion(suggestion);
    }
    public void addSuggestions(String ...suggestions) {
        Collections.addAll(this.suggestions, suggestions);
    }
    public void addSuggestions(Stream<String> suggestions) {
        this.suggestions.addAll(suggestions.toList());
    }
    public boolean hasSuggestion(String suggestion) {
        return suggestions.contains(suggestion);
    }
    public List<String> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }   

    public void error(String error) {
        if (error == null || error.trim().isEmpty()) return;
        this.error = error;
    }

    public boolean hasError() {
        return error != null;
    }
    public String getError() {
        return error;
    }

    public Suggestions(List<String> suggestions) {
        this.suggestions.addAll(suggestions);
    }
    public Suggestions() {
    }
}
