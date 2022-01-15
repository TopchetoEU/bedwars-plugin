package me.topchetoeu.bedwars;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import me.topchetoeu.bedwars.commandUtility.Command;
import me.topchetoeu.bedwars.engine.BedwarsPlayer;
import me.topchetoeu.bedwars.engine.Config;
import me.topchetoeu.bedwars.engine.Game;
import me.topchetoeu.bedwars.engine.Team;
import me.topchetoeu.bedwars.engine.TeamColor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class Commands {
    private static File confFile = new File(Main.getInstance().getDataFolder(), "config.yml");
    
    @SuppressWarnings("unchecked")
    public static Command kill(Command cmd) {
        return cmd.player("players", false).setRecursive(true).setExecutor((sender, _cmd, args) -> {
            if (Game.isStarted()) {
                for (Player p : (List<Player>)args.get("players")) {
                    BedwarsPlayer bwp = Game.instance.getPlayer(p);
                    if (bwp != null) {
                        bwp.kill(bwp.getPlayer().getName() + " definitely died with no admin intervention.");
                    }
                    else return "Player is not in game!";
                }
                return null;
            }
            else return "The game isn't started yet";
        });
    }
    @SuppressWarnings("unchecked")
    public static Command revive(Command cmd) {
        return cmd.player("players", false).setRecursive(true).setExecutor((sender, _cmd, args) -> {
            if (Game.isStarted()) {
                for (Player p : (List<Player>)args.get("players")) {
                    BedwarsPlayer bwp = Game.instance.getPlayer(p);
                    if (bwp != null) {
                        bwp.revive();
                        Bukkit.broadcastMessage("Player " + p.getName() + " revived!");
                        return null;
                    }
                    else return "Player is not in game!";
                }
                return null;
            }
            else return "The game isn't started yet";
        });
    }
    
    public static Command start(Command cmd) {
        return cmd.setExecutor((sender, _cmd, args) -> {
            if (!Game.isStarted()) {
                Game.start();
                sender.sendMessage("Started the game!");
                return null;
            }
            else return "The game is already started";
        });
    }
    public static Command stop(Command cmd) {
        return cmd.setExecutor((sender, _cmd, args) -> {
            if (Game.isStarted()) {
                Game.stop();
                sender.sendMessage("Stopped the game!");
                return null;
            }
            else return "The game is not started";
        });
    }

    private static Command basesArg() {
        return Command.createCollection("team", () -> Config.instance.getColors()
            .stream()
            .collect(Collectors.toMap(
                TeamColor::getName,
                v -> v
            )), false
        );
    }

    public static Command baseAdd(Command cmd) {
        return cmd.string("name", false)._int("red")._int("green")._int("blue")._enum("color", org.bukkit.ChatColor.class, true)._enum("wool", Material.class, true)
        .setExecutor((sender, _cmd, args) -> {
            if (!Game.isStarted()) {
                String name = args.get("name").toString().toLowerCase();
                Material wool = (Material)args.get("wool");
                org.bukkit.ChatColor bukkitChatColor = (org.bukkit.ChatColor)args.get("color");
                int r = (int)args.get("red"),
                    g = (int)args.get("green"),
                    b = (int)args.get("blue");

                ChatColor chatColor = Utility.bukkitToBungeeColor(bukkitChatColor);

                if (Config.instance.getColor(name.toLowerCase()) == null) {
                    Config.instance.getColors().add(new TeamColor(name, wool, Color.fromRGB(r, g, b), chatColor));
                    Config.instance.save(confFile);
                    sender.sendMessage("New base was created!");
                    return null;
                }
                else return "Base with this name already exists!";
            }
            else return "Can't make modifications to the map while a game is ongoing!";
        });
    }

    public static Command baseRemove(Command cmd) {
        return cmd
            .addChild(basesArg())
            .setExecutor((sender, _cmd, args) -> {
                TeamColor color = (TeamColor)args.get("team");
                Config.instance.getColors().remove(color);
                Config.instance.save(confFile);
                sender.sendMessage("Base removed!");
                return null;
            });
    }
    public static Command baseSetSpawn(Command cmd) {
        return cmd
            .addChild(basesArg())
            .location("location")
            .setExecutor((sender, _cmd, args) -> {
                TeamColor color = (TeamColor)args.get("team");
                Location loc = (Location)args.get("location");

                color.setSpawnLocation(loc);
                Config.instance.save(confFile);
                sender.sendMessage("Base spawn set");

                return null;
            });
    }
    public static Command baseSetGenerator(Command cmd) {
        return cmd
            .addChild(basesArg())
            .location("location")
            .setExecutor((sender, _cmd, args) -> {
                TeamColor color = (TeamColor)args.get("team");
                Location loc = (Location)args.get("location");

                color.setGeneratorLocation(loc);
                Config.instance.save(confFile);
                sender.sendMessage("Base generator set");

                return null;
            });
    }
    public static Command baseSetBed(Command cmd) {
        return cmd
            .addChild(basesArg())
            .location("location")
            .setExecutor((sender, _cmd, args) -> {
                TeamColor color = (TeamColor)args.get("team");
                Location loc = (Location)args.get("location");

                color.setBedLocation(loc);
                Config.instance.save(confFile);
                sender.sendMessage("Base bed set");

                return null;
            });
            // );
    }
    public static Command baseList(Command cmd) {
        return cmd
            .setExecutor((sender, _cmd, args) -> {
                List<TeamColor> colors = Config.instance.getColors();
                
                if (colors.size() != 0) {
                    sender.sendMessage("Bases:");
                    for (TeamColor color : colors) {
                        ComponentBuilder cb = new ComponentBuilder().append(color.getName()).color(color.getChatColor());
                        if (!color.isFullySpecified()) cb.append(" (not fully specified)").reset().italic(true);
                        sender.spigot().sendMessage(cb.create());
                    }
                }
                else sender.sendMessage("No bases found.");

                return null;
            });
    }
    
    @SuppressWarnings("unchecked")
    public static Command breakBed(Command cmd) {
        return cmd
            .addChild(basesArg()).setRecursive(true)
            .setExecutor((sender, _cmd, args) -> {
                if (!Game.isStarted()) return "A game hasn't been started yet.";
                
                List<TeamColor> colors = (List<TeamColor>)args.get("team");
                ArrayList<Team> teams = new ArrayList<>();
                for (TeamColor color : colors) {
                    Team team = Game.instance.getTeam(color);
                    
                    if (team == null) {
                        return "The team color %s isn't in the game.".formatted(color.getName());
                    }
                    
                    teams.add(team);
                }
                
                for (Team team : teams) {
                    team.destroyBed(null);
                }

                return null;
            });
    }
    
    public static Command createDiamondGen(Command cmd) {
        return cmd.location("location").setExecutor((sender, _cmd, args) -> {
            Location loc = (Location)args.get("location");
            Config.instance.getDiamondGenerators().add(loc);
            Config.instance.save(confFile);
            sender.sendMessage("§aGenerator added!");
            return null;
        });
    }
    public static Command createEmeraldGen(Command cmd) {
        return cmd.location("location").setExecutor((sender, _cmd, args) -> {
            Location loc = (Location)args.get("location");
            Config.instance.getEmeraldGenerators().add(loc);
            Config.instance.save(confFile);
            sender.sendMessage("§aGenerator added!");
            return null;
        });
    }
    public static Command clearGens(Command cmd) {
        return cmd.setExecutor((sender, _cmd, args) -> {
            Config.instance.getEmeraldGenerators().clear();
            Config.instance.save(confFile);
            sender.sendMessage("§aGenerators cleared!");
            return null;
        });
    }
}
