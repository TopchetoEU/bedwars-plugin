package me.topchetoeu.bedwars.engine;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;

public class SavedBlock {
	public Location loc;
	public MaterialData meta;
	public Material type;
	
	public SavedBlock(Location l, MaterialData m, Material t) {
		loc = l;
		meta = m;
		type = t;
	}
}
