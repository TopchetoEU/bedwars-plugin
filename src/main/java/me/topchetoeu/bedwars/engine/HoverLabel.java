package me.topchetoeu.bedwars.engine;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class HoverLabel {
    private List<String> data;
    private List<ArmorStand> armorStands;
    private Location loc;
    
    private ArmorStand generateArmorStand(Location loc, String name) {
        if (name == null || name.equals("")) return null;
        ArmorStand as = (ArmorStand)loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        
        as.setGravity(false);
        as.setVisible(false);
        as.setCustomName(name);
        as.setCustomNameVisible(true);
        
        return as;
    }
    @EventHandler
    private void damage(EntityDamageEvent e) {
        if (e.getEntity() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand)e.getEntity();
            if (armorStands.contains(as)) {
                e.setCancelled(true);
            }
        }
    }
    
    public void close() {
        for (ArmorStand as : armorStands) {
            if (as != null) as.remove();
        }
    }
    
    public void setLocation(Location loc) {
        this.loc = loc;
        for(ArmorStand as : armorStands) {
            if (as != null) as.teleport(loc);
            loc = loc.add(0, -0.25, 0);
        }
    }
    public Location getLocation() {
        return loc;
    }
    
    private Location replaceData(List<String> data, int n) {
        Location loc = this.loc.clone();
        for (int i = 0; i < n; i++) {
            String line = data.get(i);
            
            if (line == null || line.equals("")) {
                if (armorStands.get(i) != null) armorStands.get(i).remove();
                armorStands.set(i, null);
            }
            else {
                if (armorStands.get(i) == null) armorStands.set(i, generateArmorStand(loc, line));
                else armorStands.get(i).setCustomName(line);
            }
            loc.add(0, -0.25, 0);
        }
        
        return loc;
    }
    
    public void setData(List<String> data) {
        if (data.size() > this.data.size()) {
            Location loc = replaceData(data, this.data.size());
            
            for (int i = this.data.size(); i < data.size(); i++) {
                armorStands.add(generateArmorStand(loc, data.get(i)));
                loc.add(0, -0.25, 0);
            }
        }
        else if (data.size() == this.data.size()) {
            replaceData(data, data.size());
        }
        else {
            replaceData(data, data.size());
            for (int i = data.size(); i < this.data.size(); i++) {
                ArmorStand curr = armorStands.get(data.size());
                if (curr != null) curr.remove();
                armorStands.remove(data.size());
            }
        }
        
        this.data = data;
    }
    public List<String> getData() {
        return data;
    }
    
    public HoverLabel(Location loc, List<String> data) {
        this.loc = loc;
        this.data = new ArrayList<String>();
        this.armorStands = new ArrayList<ArmorStand>();
        setData(data);
    }
}
