package me.topchetoeu.bedwars.engine;

import java.util.ArrayList;
import java.util.Hashtable;

import org.bukkit.Location;
import org.bukkit.Material;

import me.topchetoeu.bedwars.Utility;

public class GeneratorLabel {
    private HoverLabel label;
    private String firstLine;    
    
    private Hashtable<Material, Float> remainingTimes = new Hashtable<>();
    
    public void close() {
        label.close();
    }
    
    public void setRemaining(Material item, float remainingSeconds) {
        remainingTimes.put(item, remainingSeconds);
        update();
    }
    public float getRemaining(Material item) {
        return remainingTimes.get(item);
    }
    
    public void update() {
        ArrayList<String> lines = new ArrayList<>();
        
        lines.add(firstLine);    
        lines.add(null);
        
        for (Material item : remainingTimes.keySet()) {
            lines.add(String.format("%s in %.2f seconds", Utility.getItemName(item), remainingTimes.get(item)));
        }
        
        label.setData(lines);
    }
    
    public GeneratorLabel(String firstLine, Location loc) {
        this.firstLine = firstLine;
        label = new HoverLabel(loc, new ArrayList<String>());
    }
}
