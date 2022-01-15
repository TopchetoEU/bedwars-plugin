package me.topchetoeu.bedwars.engine;

import java.util.Hashtable;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import me.topchetoeu.bedwars.Utility;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class TeamColor implements ConfigurationSerializable {
    private String name;
    private Material wool;
    private ChatColor chatColor;
    private Color color;
    private Location bed = null;
    private Location spawnLocation = null;
    private Location generatorLocation = null;
    
    public String getName() {
        return name;
    }
    public BaseComponent[] getColorName() {
        return new ComponentBuilder().append(name).color(chatColor).create();
    }
    public Material getWoolMaterial() {
        return wool;
    }
    public Color getColor() {
        return color;
    }
    public ChatColor getChatColor() {
        return chatColor;
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
    
    public TeamColor(String name, Material wool, Color color, ChatColor colorId) {
        this.name = name;
        this.wool = wool;
        this.color = color;
        this.chatColor = colorId;
    }
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new Hashtable<>();
        
        map.put("name", name);
        map.put("wool", wool.getKey().getKey().toLowerCase());
        map.put("color", color.serialize());
        map.put("chatColor", chatColor.getName());
        
        if (bed != null) map.put("bed", bed.serialize());
        if (generatorLocation != null) map.put("generator", generatorLocation.serialize());
        if (spawnLocation != null) map.put("spawn", spawnLocation.serialize());
        
        return map;
    }
    @SuppressWarnings({ "unchecked" })
    public static TeamColor deserialize(Map<String, Object> map) {
        TeamColor color = new TeamColor(
            map.get("name").toString(),
            Material.getMaterial(map.get("wool").toString().toUpperCase()),
            Color.deserialize((Map<String, Object>)map.get("color")),
            Utility.bukkitToBungeeColor(org.bukkit.ChatColor.valueOf(map.get("chatColor").toString().toUpperCase()))
        );
        
        if (map.containsKey("bed")) color.setBedLocation(Location.deserialize((Map<String, Object>) map.get("bed")));
        if (map.containsKey("generator")) color.setGeneratorLocation(Location.deserialize((Map<String, Object>) map.get("generator")));
        if (map.containsKey("spawn"))color.setSpawnLocation(Location.deserialize((Map<String, Object>) map.get("spawn")));
        
        return color;
    }
}
