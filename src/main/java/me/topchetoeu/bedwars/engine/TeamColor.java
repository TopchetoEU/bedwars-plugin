package me.topchetoeu.bedwars.engine;

import java.util.Hashtable;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class TeamColor implements ConfigurationSerializable {
	private String name;
	private int woolId;
	private char colorId;
	private Location bed = null;
	private Location spawnLocation = null;
	private Location generatorLocation = null;
	
	public String getName() {
		return name;
	}
	public String getColorName() {
		return String.format("§%c%s%s", colorId, name.substring(0, 1).toUpperCase(), name.substring(1));
	}
	public int getWoolId() {
		return woolId;
	}
	public char getColorId() {
		return colorId;
	}
	
	public Location getBedLocation() {
		return bed;
	}
	public void setBedLocation(Location loc) {
		bed = loc;
	}

	public Location getSpawnLocation() {
		return spawnLocation;
	}
	public void setSpawnLocation(Location loc) {
		spawnLocation = loc;
	}
	
	public Location getGeneratorLocation() {
		return generatorLocation;
	}
	public void setGeneratorLocation(Location loc) {
		generatorLocation = loc;
	}
	
	public boolean isFullySpecified() {
		return bed != null && spawnLocation != null && generatorLocation != null;
	}
	
	public TeamColor(String name, int woolId, char colorId) {
		this.name = name;
		this.woolId = woolId;
		this.colorId = colorId;
	}
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new Hashtable<>();
		
		map.put("name", name);
		map.put("woolId", woolId);
		map.put("colorId", colorId);
		if (bed != null) map.put("bed", bed.serialize());
		if (generatorLocation != null) map.put("generator", generatorLocation.serialize());
		if (spawnLocation != null) map.put("spawn", spawnLocation.serialize());
		
		return map;
	}
	@SuppressWarnings("unchecked")
	public static TeamColor deserialize(Map<String, Object> map) {
		TeamColor color = new TeamColor(
			(String)map.get("name"),
			(int)map.get("woolId"),
			((String)map.get("colorId")).charAt(0)
		);
		
		if (map.containsKey("bed")) color.setBedLocation(Location.deserialize((Map<String, Object>) map.get("bed")));
		if (map.containsKey("generator")) color.setGeneratorLocation(Location.deserialize((Map<String, Object>) map.get("generator")));
		if (map.containsKey("spawn"))color.setSpawnLocation(Location.deserialize((Map<String, Object>) map.get("spawn")));
		
		return color;
	}
}
