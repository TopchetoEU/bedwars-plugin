package me.topchetoeu.bedwars;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.topchetoeu.bedwars.commandUtility.Command;
import me.topchetoeu.bedwars.commandUtility.CommandExecutor;
import me.topchetoeu.bedwars.commandUtility.CommandExecutors;
import me.topchetoeu.bedwars.engine.BedwarsPlayer;
import me.topchetoeu.bedwars.engine.Config;
import me.topchetoeu.bedwars.engine.Game;
import me.topchetoeu.bedwars.engine.Team;
import me.topchetoeu.bedwars.engine.TeamColor;

public class Commands {
	private static File confFile = new File(Main.getInstance().getDataFolder(), "config.yml");
	
	@SuppressWarnings("deprecation")
	public static CommandExecutor kill = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		if (Game.isStarted()) {
			for (String arg : args) {
				OfflinePlayer p = Bukkit.getOfflinePlayer(arg);

				if (p != null) {
					BedwarsPlayer bwp = Game.instance.getPlayer(p);
					
					if (bwp != null) {
						bwp.kill(bwp.getPlayer().getName() + " definitely died with no admin intervention.");
					}
					else sender.sendMessage("Player is not in game!");
				}
				else sender.sendMessage("Player doesn't exist!");
			}
		}
		else sender.sendMessage("The game isn't started yet");
	};
	@SuppressWarnings("deprecation")
	public static CommandExecutor revive = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		if (Game.isStarted()) {
			for (String arg : args) {
				OfflinePlayer p = Bukkit.getOfflinePlayer(arg);

				if (p != null) {
					BedwarsPlayer bwp = Game.instance.getPlayer(p);
					
					if (bwp != null) {
						bwp.revive();
						Bukkit.broadcastMessage("Player " + p.getName() + " revived!");
					}
					else sender.sendMessage("Player is not in game!");
				}
				else sender.sendMessage("Player doesn't exist!");
			}
		}
		else sender.sendMessage("The game isn't started yet");
	};
	
	public static CommandExecutor start = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		if (args.length == 0) {
			if (!Game.isStarted()) {
				Game.start();
				sender.sendMessage("Started the game!");	
			}
			else sender.sendMessage("The game is already started");				
		}
		else sender.sendMessage("Invalid command syntax. No parameters required!");
	};
	public static CommandExecutor stop = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		if (args.length == 0) {
			if (Game.isStarted()) {
				Game.stop();
			}
			else sender.sendMessage("The game is not started");				
		}
		else sender.sendMessage("Invalid command syntax. No parameters required!");
	};
	
	public static CommandExecutor baseAdd = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		if (args.length == 6 && args[5].length() == 1) {
			if (!Game.isStarted()) {
				if (Utility.isParsable(args[2])) {
					if (Utility.isParsable(args[3])) {
						if (Utility.isParsable(args[4])) {

							String name = args[0];
							Material wool = Material.getMaterial(args[1].toString());
							char chatId = args[5].charAt(0);
							if (wool != null) {
								if (Config.instance.getColor(name.toLowerCase()) == null) {
									Config.instance.getColors().add(new TeamColor(name.toLowerCase(), wool,
										Color.fromRGB(
											Integer.parseInt(args[2]),
											Integer.parseInt(args[3]),
											Integer.parseInt(args[4])
										),
										chatId
									));
									Config.instance.save(confFile);
									sender.sendMessage("New base was created!");
								}
								else sender.sendMessage("Base with this name already exists!");
							}
							else sender.sendMessage("The material '" + args[1] + " was not found!");
						} else sender.sendMessage("blue must be a valid number between 0 and 255!");
					} else sender.sendMessage("green must be a valid number between 0 and 255!");
				} else sender.sendMessage("red must be a valid number between 0 and 255!");
			}
			else sender.sendMessage("Can't make modifications to the map while a game is ongoing!");				
		}
		else sender.sendMessage("Invalid command syntax. Syntax: /bw conf base new <name> <woolId> <chatId>");
	};
	
	public static CommandExecutor baseRemove = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		if (args.length == 1) {
			TeamColor color = Config.instance.getColor(args[0]);
			if (color != null) {
				Config.instance.getColors().remove(color);
				Config.instance.save(confFile);
				sender.sendMessage("Base removed!");
			}
			else sender.sendMessage("Base doesn't exist!");
		}
		else sender.sendMessage("Invalid syntax! Syntax: /bw conf base del <name>");
	};
	public static CommandExecutor baseSetSpawn = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		boolean aligned = args.length == 2 && args[1].equals("aligned");
		
		if (args.length == 1 || aligned) {
			if (sender instanceof Player) {
				Player p = (Player)sender;
				
				TeamColor color = Config.instance.getColor(args[0]);
				if (color != null) {
					Location loc = p.getLocation();
					if (aligned) {
						loc.setX(loc.getBlockX() + 0.5);
						loc.setZ(loc.getBlockZ() + 0.5);
					}
					color.setSpawnLocation(loc);
					Config.instance.save(confFile);
					sender.sendMessage("Base spawn set to your current location");
				}
				else sender.sendMessage("Base doesn't exist!");
			
			}
			else sender.sendMessage("This commands is for players only!");
		}
		else sender.sendMessage("Invalid syntax! Syntax: /bw conf base spawn [aligned]");
	};
	public static CommandExecutor baseSetGenerator = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		boolean aligned = args.length == 2 && args[1].equals("aligned");
		
		if (args.length == 1 || aligned) {
			if (sender instanceof Player) {
				Player p = (Player)sender;
				
				TeamColor color = Config.instance.getColor(args[0]);
				if (color != null) {
					Location loc = p.getLocation();
					if (aligned) {
						loc.setX(loc.getBlockX() + 0.5);
						loc.setZ(loc.getBlockZ() + 0.5);
					}
					color.setGeneratorLocation(loc);
					Config.instance.save(confFile);
					sender.sendMessage("Base generator set to your current location");
				}
				else sender.sendMessage("Base doesn't exist!");
			
			}
			else sender.sendMessage("This commands is for players only!");
		}
		else sender.sendMessage("Invalid syntax! Syntax: /bw conf base gen [aligned]");
	};
	public static CommandExecutor baseSetBed = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		boolean aligned = args.length == 2 && args[1].equals("aligned");
		
		if (args.length == 1 || aligned) {
			if (sender instanceof Player) {
				Player p = (Player)sender;
				
				TeamColor color = Config.instance.getColor(args[0]);
				if (color != null) {
					Location loc = p.getLocation();
					if (aligned) {
						loc.setX(loc.getBlockX() + 0.5);
						loc.setZ(loc.getBlockZ() + 0.5);
					}
					color.setBedLocation(loc);
					Config.instance.save(confFile);
					sender.sendMessage("Base bed set to your current location");
				}
				else sender.sendMessage("Base doesn't exist!");
			
			}
			else sender.sendMessage("This commands is for players only!");
		}
		else sender.sendMessage("Invalid syntax! Syntax: /bw conf base bed [aligned]");
	};
	public static CommandExecutor baseList = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		if (args.length == 0) {
			ArrayList<TeamColor> colors = Config.instance.getColors();
			
			if (colors.size() != 0) {
				sender.sendMessage("Bases:");
				for (TeamColor color : colors) {
					sender.sendMessage("§" + color.getChatColor() + color.getName() + "§r" + (
						color.isFullySpecified() ? "" : " (not fully specified)"
					));
				}
			}
			else sender.sendMessage("No bases found.");
		}
		else sender.sendMessage("Invalid syntax! No parameters required");
	};
	
	public static CommandExecutor breakBed = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		if (!Game.isStarted()) {
			sender.sendMessage("§4A game hasn't been started yet.");
			return;
		}
		
		ArrayList<Team> teams = new ArrayList<>();
		
		for (String arg : args) {
			TeamColor color = Config.instance.getColor(arg);
			
			if (color == null) {
				sender.sendMessage(String.format("§4The team color §l§4%s§r§4 doesn't exist.", arg));
				return;
			}
			
			Team team = Game.instance.getTeam(color);
			
			if (team == null) {
				sender.sendMessage(String.format("§6The team color §l§4%s§r§4 isn't in the game.", arg));
			}
			
			teams.add(team);
		}
		
		for (Team team : teams) {
			if (!team.destroyBed(null)) 
				sender.sendMessage(String.format("§4The %s's bed is already destroyed.", team.getTeamColor().getName()));
			
		}
	};
	
	public static CommandExecutor createDiamondGen = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		if (args.length > 1) {
			sender.sendMessage("§4Invalid syntax!§r Syntax: /bw config gen diamond [aligned]");
			return;
		}
		
		boolean aligned = args.length == 1 && args[0] == "aligned";
		
		if (sender instanceof Player) {
			Player p = (Player)sender;

			Location loc = p.getLocation();
			if (aligned) {
				loc.setX(loc.getBlockX() + 0.5);
				loc.setZ(loc.getBlockZ() + 0.5);
			}
			
			Config.instance.getDiamondGenerators().add(loc);
			Config.instance.save(confFile);
			
			p.sendMessage("§aGenerator added!");
		}
		else sender.sendMessage("§4Only a player may execute this command.");
	};
	public static CommandExecutor createEmeraldGen = (CommandSender sender, Command cmd, String alias, String[] args) -> {
		if (args.length > 1) {
			sender.sendMessage("§4Invalid syntax!§r Syntax: /bw config gen emerald [aligned]");
			return;
		}
		
		boolean aligned = args.length == 1 && args[0] == "aligned";
		
		if (sender instanceof Player) {
			Player p = (Player)sender;

			Location loc = p.getLocation();
			if (aligned) {
				loc.setX(loc.getBlockX() + 0.5);
				loc.setZ(loc.getBlockZ() + 0.5);
			}
			
			Config.instance.getEmeraldGenerators().add(loc);
			Config.instance.save(confFile);
			
			p.sendMessage("§aGenerator added!");
		}
		else sender.sendMessage("§4Only a player may execute this command.");
	};
	
	public static CommandExecutor _default = CommandExecutors.message("For help do /bw help");
}
