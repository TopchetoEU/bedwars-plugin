package me.topchetoeu.bedwars.engine;

import java.util.ArrayList;
import java.util.Hashtable;

import org.bukkit.Location;
import org.bukkit.Material;

import me.topchetoeu.bedwars.Utility;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class GeneratorLabel {
    private HoverLabel label;
    private BaseComponent[] firstLine;    
    
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
        ArrayList<BaseComponent[]> lines = new ArrayList<>();
        
        lines.add(firstLine);    
        lines.add(null);
        
        for (Material item : remainingTimes.keySet()) {
            lines.add(new ComponentBuilder().append(Utility.getItemName(item)).append(" in %.2f seconds".formatted(remainingTimes.get(item))).create());
        }
        
        label.setData(lines);
    }
    
    public GeneratorLabel(BaseComponent[] firstLine, Location loc) {
        this.firstLine = firstLine;
        label = new HoverLabel(loc, new ArrayList<BaseComponent[]>());
    }
}
