package me.topchetoeu.bedwars.commandUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import me.topchetoeu.bedwars.commandUtility.args.ArgParser;
import me.topchetoeu.bedwars.commandUtility.args.ArgParserRes;
import me.topchetoeu.bedwars.commandUtility.args.CollectionArgParser;
import me.topchetoeu.bedwars.commandUtility.args.CollectionProvider;
import me.topchetoeu.bedwars.commandUtility.args.EnumArgParser;
import me.topchetoeu.bedwars.commandUtility.args.IntArgParser;
import me.topchetoeu.bedwars.commandUtility.args.LiteralArgParser;
import me.topchetoeu.bedwars.commandUtility.args.LocationArgParser;
import me.topchetoeu.bedwars.commandUtility.args.PlayerArgParser;
import me.topchetoeu.bedwars.commandUtility.args.StringArgParser;
import me.topchetoeu.bedwars.commandUtility.args.Suggestions;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Command {
    private String name;
    private String helpMessage;
    private CommandExecutor executor;
    private HashSet<Command> children = new HashSet<>();
    private ArgParser parser;
    private boolean recursive = false;
    
    private Set<JavaPlugin> parents = new HashSet<>();
        
    public String getName() {
        return name;
    }
    
    public boolean attachedToAnyPlugin() {
        return parents.size() > 0;
    }
    public Set<JavaPlugin> getParentPlugins() {
        return Collections.unmodifiableSet(parents);
    }
    
    public ArgParser getParser() {
        return parser;
    }

    public String getHelpMessage() {
        return helpMessage;
    }
    public Command setHelpMessage(String val) {
        helpMessage = val;
        return this;
    }
    
    public CommandExecutor getExecutor() {
        return executor;
    }
    public Command setExecutor(CommandExecutor val) {
        executor = val;
        return this;
    }
    
    public Command addChild(Command cmd) {
        if (cmd == null) throw new RuntimeException("cmd is null");
        children.add(cmd);
        return cmd;
    }
    public Command removeChild(Command cmd) {
        if (cmd == null) throw new RuntimeException("cmd is null");
        children.remove(cmd);
        return this;
    }
    public boolean hasChild(Command cmd) {
        if (cmd == null) return false;
        return children.contains(cmd);
    }
    public Set<Command> getChildren() {
        return Collections.unmodifiableSet(children);
    }
    
    public Command setRecursive(boolean val) {
        recursive = val;
        return this;
    }
    public boolean isRecursive() {
        return recursive;
    }

    @SuppressWarnings("unchecked")
    public void execute(CommandSender sender, String[] _args) {
        Command toExecute = this;
        
        Hashtable<String, Object> newArgs = new Hashtable<>();

        List<String> args = new ArrayList<>();
        Collections.addAll(args, _args);

        String err = null;

        while (args.size() > 0) {
            Command newCmd = null;
            Set<Command> children = toExecute.getChildren();
            if (toExecute.isRecursive()) children = Collections.singleton(toExecute);
            for (Command cmd : children) {
                ArgParser parser = cmd.getParser();
                ArgParserRes res = parser.parse(sender, args);

                if (res.hasError()) err = res.getError();
                else if (res.hasSucceeded()) {
                    for (int i = 0; i < res.getTakenCount(); i++) {
                        if (args.size() == 0) break;
                        args.remove(0);
                    }

                    if (res.hasResult()) {
                        if (cmd.recursive) {
                            if (!newArgs.containsKey(cmd.name)) newArgs.put(cmd.name, new ArrayList<>());
                            ((List<Object>)newArgs.get(cmd.name)).add(res.getResult());
                        }
                        else newArgs.put(cmd.name, res.getResult());
                    }
                    newCmd = cmd;
                    break;
                }
            }

            if (newCmd == null) {
                toExecute = null;
                break;
            }
            toExecute = newCmd;
        }

        if (toExecute == null) err = "Invalid command syntax.";
        else if (toExecute.getExecutor() == null)  err = "Incomplete command.";

        if (err != null) sender.spigot().sendMessage(new ComponentBuilder()
            .append("Error: ")
            .color(ChatColor.DARK_RED)
            .append(err)
            .color(ChatColor.RED)
            .create()
        );
        else {
            if (toExecute.isRecursive() && !newArgs.containsKey(toExecute.name)) newArgs.put(toExecute.name, new ArrayList<>());
            BaseComponent[] _err = toExecute.getExecutor().execute(sender, this, newArgs);
            if (_err != null && _err.length > 0) sender.spigot().sendMessage(new ComponentBuilder()
                .append("Error: ")
                .color(ChatColor.DARK_RED)
                .append(err)
                .reset()
                .create()
            );
        }
    }

    public List<String> tabComplete(CommandSender sender, String[] _args) {
        Command toComplete = this;
        
        List<String> args = new ArrayList<>();
        Collections.addAll(args, _args);

        int index = 1;

        while (args.size() > 0) {
            boolean found = false;
            index++;
            for (Command cmd : toComplete.children) {
                ArgParser parser = cmd.getParser();
                ArgParserRes res = parser.parse(sender, args);
                if (res.hasSucceeded()) {
                    for (int i = 0; i < res.getTakenCount(); i++) {
                        if (args.size() == 0) break;
                        args.remove(0);
                    }
                    toComplete = cmd;
                    found = true;
                    break;
                }
            }
            if (!found) break;
        }

        if (args.size() == 0) return null;

        Suggestions suggestions = new Suggestions();

        for (Command cmd : toComplete.children) {
            cmd.getParser().addCompleteSuggestions(sender, args, suggestions);
        }

        if (suggestions.hasError() && !suggestions.getSuggestions().contains(args.get(0))) {
            sender.spigot().sendMessage(new ComponentBuilder()
                .append("Error (argument %d): ".formatted(index + 1))
                .color(ChatColor.DARK_RED)
                .append(suggestions.getError())
                .color(ChatColor.RED)
                .create()
            );
            return null;
        }
        else {
            List<String> _suggestions = new ArrayList<>(suggestions.getSuggestions());
            return _suggestions;
        }
    }
    
    public Command register(JavaPlugin pl) {
        if (!(parser instanceof LiteralArgParser))
            throw new IllegalArgumentException("Only a command with a literal parser may be registered.");
        if (parents.contains(pl)) throw new IllegalArgumentException("The command is already attached to the given plugin");
        if (pl == null) throw new RuntimeException("pl is null");
        parents.add(pl);

        LiteralArgParser parser = (LiteralArgParser)this.parser;
        String name = parser.getLiteral();

        pl.getCommand(name).setAliases(parser.getAliases());
        pl.getCommand(name).setExecutor((sender, cmd, alias, args) -> {
            execute(sender, args);
            return true;
        });
        pl.getCommand(name).setTabCompleter((sender, cmd, alias, args) -> {
            return tabComplete(sender, args);
        });

        return this;
    }

    public Command(String name, ArgParser parser) {
        this.name = name;
        this.parser = parser;
    }

    public Command literal(String name) {
        Command cmd = createLiteral(name);
        addChild(cmd);
        return cmd;
    }
    public Command literal(String name, String ...aliases) {
        Command cmd = createLiteral(name, aliases);
        addChild(cmd);
        return cmd;
    }
    public Command location(String name) {
        Command cmd = createLocation(name);
        addChild(cmd);
        return cmd;
    }
    public Command _enum(String name, Class<? extends Enum<?>> enumType, boolean caseInsensitive) {
        Command cmd = createEnum(name, enumType, caseInsensitive);
        addChild(cmd);
        return cmd;
    }
    public Command player(String name, boolean caseInsensitive) {
        Command cmd = createPlayer(name, caseInsensitive);
        addChild(cmd);
        return cmd;
    }
    public Command string(String name, boolean greedy) {
        Command cmd = createString(name, greedy);
        addChild(cmd);
        return cmd;
    }
    public Command _int(String name) {
        Command cmd = createInt(name);
        addChild(cmd);
        return cmd;
    }
    public Command collection(String name, SimpleCollectionQuery query, boolean caseInsensitive) {
        Command cmd = createCollection(name, query, caseInsensitive);
        addChild(cmd);
        return cmd;
    }
    public Command collection(String name, CollectionProvider provider, boolean caseInsensitive) {
        Command cmd = createCollection(name, provider, caseInsensitive);
        addChild(cmd);
        return cmd;
    }

    public static Command createCollection(String name, SimpleCollectionQuery query, boolean caseInsensitive) {
        return new Command(name, new CollectionArgParser(
            () -> query.get().stream().collect(Collectors.toMap(v->v, v->v)),
            caseInsensitive)
        );
    }
    public static Command createCollection(String name, CollectionProvider provider, boolean caseInsensitive) {
        return new Command(name, new CollectionArgParser(provider, caseInsensitive));
    }
    public static Command createPlayer(String name, boolean caseInsensitive) {
        return new Command(name, new PlayerArgParser(caseInsensitive));
    }
    public static Command createEnum(String name, Class<? extends Enum<?>> enumType, boolean caseInsensitive) {
        return new Command(name, new EnumArgParser(enumType, caseInsensitive));
    }
    public static Command createString(String name, boolean greedy) {
        return new Command(name, new StringArgParser(greedy));
    }
    public static Command createInt(String name) {
        return new Command(name, new IntArgParser());
    }
    public static Command createLocation(String name) {
        return new Command(name, new LocationArgParser());
    }
    public static Command createLiteral(String lit) {
        return new Command(lit, new LiteralArgParser(lit));
    }
    public static Command createLiteral(String lit, String ...aliases) {
        return new Command(lit, new LiteralArgParser(lit, aliases));
    }

    public interface SimpleCollectionQuery {
        Collection<String> get();
    }
}
