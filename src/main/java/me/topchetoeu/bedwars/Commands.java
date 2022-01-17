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

import me.topchetoeu.bedwars.commands.Command;
import me.topchetoeu.bedwars.engine.BedwarsPlayer;
import me.topchetoeu.bedwars.engine.Config;
import me.topchetoeu.bedwars.engine.Game;
import me.topchetoeu.bedwars.engine.Team;
import me.topchetoeu.bedwars.engine.TeamColor;
import me.topchetoeu.bedwars.messaging.MessageParser;
import me.topchetoeu.bedwars.messaging.MessageUtility;
import net.md_5.bungee.api.ChatColor;

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
                    else return MessageUtility.parser("commands.not-in-game").variable("player", p.getDisplayName()).parse();
                }
                return null;
            }
            else return MessageUtility.parser("commands.game-not-started").parse();
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
                    else return MessageUtility.parser("commands.not-in-game").variable("player", p.getDisplayName()).parse();
                }
                return null;
            }
            else return MessageUtility.parser("commands.game-not-started").parse();
        });
    }
    
    public static Command start(Command cmd) {
        return cmd.setExecutor((sender, _cmd, args) -> {
            if (!Game.isStarted()) {
                Game.start();
                MessageUtility.parser("commands.start.success").send(sender);
                return null;
            }
            else return MessageUtility.parser("commands.start.game-started").parse();
        });
    }
    public static Command stop(Command cmd) {
        return cmd.setExecutor((sender, _cmd, args) -> {
            if (Game.isStarted()) {
                Game.stop();
                MessageUtility.parser("commands.stop.success").send(sender);
                return null;
            }
            else return MessageUtility.parser("commands.start.game-not-started").parse();
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
                    MessageUtility.parser("commands.base-add.success").variable("base", name).send(sender);
                    return null;
                }
                else return MessageUtility.parser("commands.base-add.already-exists").variable("base", name).parse();
            }
            else return MessageUtility.parser("commands.base-add.game-started").parse();
        });
    }

    public static Command baseRemove(Command cmd) {
        return cmd
            .addChild(basesArg())
            .setExecutor((sender, _cmd, args) -> {
                TeamColor color = (TeamColor)args.get("team");
                Config.instance.getColors().remove(color);
                Config.instance.save(confFile);
                MessageUtility.parser("commands.base-remove").variable("base", color.getColorName()).send(sender);
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
                MessageUtility.parser("commands.base-spawn").variable("base", color.getColorName()).send(sender);

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
                MessageUtility.parser("commands.base-generator").variable("base", color.getColorName()).send(sender);

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
                MessageUtility.parser("commands.base-bed").variable("base", color.getColorName()).send(sender);

                return null;
            });
            // );
    }
    public static Command baseList(Command cmd) {
        return cmd
            .setExecutor((sender, _cmd, args) -> {
                List<TeamColor> colors = Config.instance.getColors();
                
                if (colors.size() != 0) {
                    MessageUtility.parser("commands.base-list.title").variable("count", colors.size()).send(sender);
                    for (TeamColor color : colors) {
                        MessageParser parser = MessageUtility.parser("commands.base-list.not-fully-specified");

                        if (color.isFullySpecified()) parser = MessageUtility.parser("commands.base-list.fully-specified");

                        parser
                            .variable("name", color.getColorName())
                            .variable("woolId", color.getWoolMaterial().getKey())
                            .variable("colorRed", color.getColor().getRed())
                            .variable("colorGreen", color.getColor().getGreen())
                            .variable("colorBlue", color.getColor().getBlue())
                            .send(sender);
                    }
                }
                else MessageUtility.parser("commands.base-list.no-bases").send(sender);

                return null;
            });
    }
    
    @SuppressWarnings("unchecked")
    public static Command breakBed(Command cmd) {
        return cmd
            .addChild(basesArg()).setRecursive(true)
            .setExecutor((sender, _cmd, args) -> {
                if (!Game.isStarted()) return MessageUtility.parser("commands.game-not-started").parse();
                
                List<TeamColor> colors = (List<TeamColor>)args.get("team");
                ArrayList<Team> teams = new ArrayList<>();
                for (TeamColor color : colors) {
                    Team team = Game.instance.getTeam(color);
                    
                    if (team == null) {
                        return MessageUtility.parser("commands.break-bed.not-in-game").variable("team", color.getColorName()).parse();
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
            MessageUtility.parser("commands.generator-create.diamond").send(sender);
            return null;
        });
    }
    public static Command createEmeraldGen(Command cmd) {
        return cmd.location("location").setExecutor((sender, _cmd, args) -> {
            Location loc = (Location)args.get("location");
            Config.instance.getEmeraldGenerators().add(loc);
            Config.instance.save(confFile);
            MessageUtility.parser("commands.generator-create.emerald").send(sender);
            return null;
        });
    }
    public static Command clearGens(Command cmd) {
        return cmd.setExecutor((sender, _cmd, args) -> {
            Config.instance.getEmeraldGenerators().clear();
            Config.instance.save(confFile);
            MessageUtility.parser("commands.generator-create.clear").send(sender);
            return null;
        });
    }
}
