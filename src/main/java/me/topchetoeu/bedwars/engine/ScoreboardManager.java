package me.topchetoeu.bedwars.engine;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardManager {
	private static Hashtable<UUID, Scoreboard> scoreboards = new Hashtable<>();
	
	private static List<String> getLines(Player p) {
		if (!Game.isStarted()) return new ArrayList<>();
		return Game.instance
			.getTeams()
			.stream()
			.map(v -> {
				String teamCounter = "§a✔";
				if (v.getRemainingPlayers() == 0) teamCounter = "§4✖";
				else if (!v.hasBed())  teamCounter = Integer.toString(v.getRemainingPlayers());
				
				String newStr = String.format(" %s§r: %s", v.getTeamColor().getColorName(), teamCounter);
				
				if (v.hasPlayer(p)) {
					newStr = (newStr + "§r (you)").replaceAll("§([0-9a-z])", "§$1§l");
				}
				
				return newStr;
			})
			.collect(Collectors.toList());
					
	}
	
	public static Scoreboard getScoreboard(Player p) {
		Scoreboard scoreboard = scoreboards.get(p.getUniqueId());
		if (scoreboard == null) {
			scoreboards.put(p.getUniqueId(), Bukkit.getScoreboardManager().getNewScoreboard());
			scoreboard = scoreboards.get(p.getUniqueId());
			
			p.setScoreboard(scoreboard);
			
			Objective objective = scoreboard.registerNewObjective("bedwars", "dummy", "       §4§lBedwars    ");
			objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		}
		
		return scoreboard;
	}
	
	public static void update(Player p) {
		Scoreboard scoreboard = getScoreboard(p);
		
		
		List<String> lines = getLines(p);
		for (String entry : scoreboard.getEntries()) {
			scoreboard.resetScores(entry);
		}
		
		for (int i = 0; i < lines.size(); i++) {
			scoreboard.getObjective("bedwars").getScore(lines.get(lines.size() - 1 - i)).setScore(i);
		}
	}
	public static void updateAll() {
		updateAll(false);
	}
	public static void updateAll(boolean supressTeamCheck) {
		Bukkit.getServer().getOnlinePlayers().forEach(v -> update(v));

		if (Game.isStarted() && Game.instance.getAliveTeams().size() == 1) {
			Game.instance.win(Game.instance.getAliveTeams().get(0).getTeamColor());
		}
	}
}
