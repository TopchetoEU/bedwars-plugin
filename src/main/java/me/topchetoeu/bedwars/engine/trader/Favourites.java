package me.topchetoeu.bedwars.engine.trader;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class Favourites {
    public static Favourites instance;
    
    private File directory;
    private Map<Integer, DealPtr> defaults;
    
    private YamlConfiguration serialize(Map<Integer, DealPtr> favs) {
        YamlConfiguration config = new YamlConfiguration();
        
        for (int n : favs.keySet()) {
            Map<String, Object> map = new Hashtable<>();
            map.put("section", favs.get(n).getSectionN());
            map.put("deal", favs.get(n).getDealN());
            
            config.set(Integer.toString(n), map);
        }
        
        return config;
    }
    private Map<Integer, DealPtr> deserialize(YamlConfiguration config) {
        Map<Integer, DealPtr> favs = new Hashtable<>();
        
        for (String s : config.getKeys(false)) {
            int n = Integer.parseInt(s);
            ConfigurationSection section = config.getConfigurationSection(s);
            
            int dealN, sectN;
            
            dealN = section.getInt("deal");
            sectN = section.getInt("section");
            
            DealPtr deal = new DealPtr(sectN, dealN);
            
            favs.put(n, deal);
        }
        
        return favs;
    }
    
    public Map<Integer, DealPtr> getFavourites(OfflinePlayer p) {
        File file = new File(directory, p.getUniqueId().toString() + ".yml");
        
        if (file.exists() && file.canRead()) return deserialize(YamlConfiguration.loadConfiguration(file));
        else {
            try {
                serialize(defaults).save(file);
            } catch (IOException e) { /* everythings fine */ }
            
            return defaults;
        }
    }
    public void updateFavourites(OfflinePlayer p, Map<Integer, DealPtr> newFavs) {
        try {
            serialize(newFavs).save(new File(directory, p.getUniqueId().toString() + ".yml"));
        } catch (IOException e) {
            p.getPlayer().sendMessage(e.getMessage());
        }
    }
    
    public Favourites(File directory, Map<Integer, DealPtr> defaults) {
        directory.mkdir();
        this.directory = directory;
        this.defaults = defaults;
    }
    public Favourites(File directory, File defaults) {
        directory.mkdir();
        this.directory = directory;
        this.defaults = deserialize(YamlConfiguration.loadConfiguration(defaults));
    }
}
