package me.topchetoeu.bedwars.engine;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class SavedBlock {
    public Location loc;
    public BlockData meta;
    public Material type;
    
    public SavedBlock(Location l, BlockData m, Material t) {
        loc = l;
        meta = m;
        type = t;
    }
}
