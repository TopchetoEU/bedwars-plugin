package me.topchetoeu.bedwars.engine.trader;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.BaseComponent;

public interface Deal {
    public ItemStack getDealItem(Player p);
    public BaseComponent[] getDealName(Player p);
    public Material getPriceType(Player p);
    public int getPrice(Player p);
    public boolean alreadyBought(Player p);
    
    public void commence(Player p);
}
