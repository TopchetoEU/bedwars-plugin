package me.topchetoeu.bedwars.commandUtility;

import java.util.ArrayList;
import java.util.List;


import me.topchetoeu.bedwars.commandUtility.args.LiteralArgParser;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class CommandExecutors {
    // private static List<String> getSyntaxes(Command cmd) {
    //     ArrayList<String> syntaxes = new ArrayList<>();

    //     for (Command child : cmd.getChildren()) {
    //         ArgParser parser = child.getParser();

    //         if (parser instanceof LiteralArgParser) {

    //         }
    //     }

    //     return null;
    // }
    
    @SuppressWarnings("unchecked")
    public static CommandExecutor help() {
        return (sender, cmd, _args) -> {
            List<String> args = (List<String>)_args.get("args");
            if (args == null) args = new ArrayList<>();

            String path = "/" + cmd.getName();
            Command currCmd = cmd;

            while (args.size() > 0) {
                Command next = null;

                for (Command child : currCmd.getChildren()) {
                    if (child.getParser() instanceof LiteralArgParser && child.getParser().parse(sender, args).hasSucceeded()) {
                        next = child;
                        break;
                    }
                }

                if (next == null) break;

                currCmd = next;
                path += " " + args.get(0);
                args.remove(0);
            }
            
            cmd = currCmd;
            LiteralArgParser cmdParser = (LiteralArgParser)cmd.getParser();

            sender.spigot().sendMessage(new ComponentBuilder()
                .append("Help for %s: ".formatted(path))
                .bold(true)
                .create()
            );
            if (cmd.getHelpMessage() != null) sender.spigot().sendMessage(new ComponentBuilder()
                .append("  " + cmd.getHelpMessage())
                .create()
            );
            sender.spigot().sendMessage(new ComponentBuilder()
                .append("  Aliases: ")
                .bold(true)
                .append(String.join(", ", cmdParser.getAliases()))
                .bold(false)
                .create()
            );
            sender.spigot().sendMessage(new ComponentBuilder()
                .append("  Subcommands: ")
                .bold(true)
                .create()
            );

            for (Command child : currCmd.getChildren()) {
                if (child.getParser() instanceof LiteralArgParser) {
                    sender.spigot().sendMessage(new ComponentBuilder()
                        .append("    %s: ".formatted(((LiteralArgParser)child.getParser()).getLiteral()))
                        .bold(true)
                        .append(child.getHelpMessage() != null ? child.getHelpMessage() : "No help provided.")
                        .bold(false)
                        .create()
                    );
                }
            }

            return null;
        };
    }
    public static CommandExecutor message(String msg) {
        return (sender, cmd, args) -> {
            sender.sendMessage(msg);
            return null;
        };
    }
}
