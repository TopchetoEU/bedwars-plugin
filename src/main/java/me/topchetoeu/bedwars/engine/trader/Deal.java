package me.topchetoeu.bedwars.engine.trader;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Deal {
	public ItemStack getDealItem(Player p);
	public String getDealName(Player p);
	public Material getPriceType(Player p);
	public int getPrice(Player p);
	public boolean alreadyBought(Player p);
	
	public void commence(Player p);
}
