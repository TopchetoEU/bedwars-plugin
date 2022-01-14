package me.topchetoeu.bedwars.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	public static Config instance;
	
	private int perTeam = 0;
	private Location respawnLocation;
	private ArrayList<TeamColor> colors;
	private ArrayList<Location> diamondGenerators;
	private ArrayList<Location> emeraldGenerators;
	
	public Location getRespawnLocation() {
		return respawnLocation;
	}
	public void setRespawnLocation(Location loc) {
		respawnLocation = loc;
	}
	
	public int getTeamSize() {
		return perTeam;
	}
	public void setTeamSize(int size) {
		perTeam = size;
	}

	public ArrayList<TeamColor> getColors() {
		return colors;
	}
	public TeamColor getColor(String name) {
		return Config.instance.getColors()
			.stream()
			.filter(v -> v.getName().equals(name.toLowerCase()))
			.findFirst()
			.orElse(null);
	}
	
	public ArrayList<Location> getDiamondGenerators() {
		return diamondGenerators;
	}
	public Location getClosestDiamondGenerator(Location loc) {
		if (diamondGenerators.size() == 0) return null;
		Location closest = null;
		double smallestDist = diamondGenerators.get(0).distance(loc);
		
		for (int i = 0; i < diamondGenerators.size(); i++) {
			Location el = diamondGenerators.get(i);
			double dist = el.distance(loc);
			if (dist < smallestDist) {
				closest = el;
				smallestDist = dist;
			}
		}
		
		return closest;
	}
	public List<Location> getDiamondGeneratorsInRadius(double radius, Location loc) {
		return diamondGenerators.stream()
			.filter(v -> v.distance(loc) <= radius)
			.collect(Collectors.toList());
	}
	
	public ArrayList<Location> getEmeraldGenerators() {
		return emeraldGenerators;
	}
	public Location getClosestEmeraldGenerator(Location loc) {
		if (diamondGenerators.size() == 0) return null;
		Location closest = null;
		double smallestDist = emeraldGenerators.get(0).distance(loc);
		
		for (int i = 0; i < emeraldGenerators.size(); i++) {
			Location el = emeraldGenerators.get(i);
			double dist = el.distance(loc);
			if (dist < smallestDist) {
				closest = el;
				smallestDist = dist;
			}
		}
		
		return closest;
	}
	public List<Location> getEmeraldGeneratorsInRadius(double radius, Location loc) {
		return emeraldGenerators.stream()
			.filter(v -> v.distance(loc) <= radius)
			.collect(Collectors.toList());
	}
	
	private static ArrayList<?> toList(Stream<?> s) {
		return new ArrayList<>(s.collect(Collectors.toList()));
	}
	
	@SuppressWarnings("unchecked")
	public static void load(File confFile) {
		Configuration conf = YamlConfiguration.loadConfiguration(confFile);
		
		Config c = new Config();
		
		c.perTeam = conf.getInt("perTeam");
		if (conf.get("respawnLocation") != null) c.respawnLocation = Location.deserialize(conf.getConfigurationSection("respawnLocation").getValues(false));
		c.colors = (ArrayList<TeamColor>)toList(conf
			.getMapList("colors")
			.stream()
			.map(v -> TeamColor.deserialize((Map<String, Object>)v))
		);
		c.diamondGenerators = (ArrayList<Location>)toList(conf
			.getMapList("diamondGenerators")
			.stream()
			.map(v -> Location.deserialize((Map<String, Object>)v))
		);
		c.emeraldGenerators = (ArrayList<Location>)toList(conf
				.getMapList("emeraldGenerators")
				.stream()
				.map(v -> Location.deserialize((Map<String, Object>)v))
			);
		
		instance = c;
	}

	public void save(File confFile) {
		FileConfiguration conf = YamlConfiguration.loadConfiguration(confFile);

		conf.set("perTeam", perTeam);
		conf.set("colors", toList(colors.stream().map(v -> v.serialize())));
		conf.set("diamondGenerators", toList(diamondGenerators.stream().map(v -> v.serialize())));
		conf.set("emeraldGenerators", toList(emeraldGenerators.stream().map(v -> v.serialize())));
		
		try {
			conf.save(confFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
