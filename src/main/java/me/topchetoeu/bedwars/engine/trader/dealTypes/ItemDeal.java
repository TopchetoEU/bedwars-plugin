package me.topchetoeu.bedwars.engine.trader.dealTypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.topchetoeu.bedwars.InventoryUtility;
import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.BedwarsPlayer;
import me.topchetoeu.bedwars.engine.Game;
import me.topchetoeu.bedwars.engine.WoodenSword;
import me.topchetoeu.bedwars.engine.trader.Deal;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class ItemDeal implements Deal {
    private ItemStack item;
    private int price;
    private Material priceType;
    private boolean implemented;
    
    @Override
    public ItemStack getDealItem(Player p) {
        ItemStack item = this.item;
        if (Game.isStarted()) Game.instance.teamifyItem(p, item, true, true);
        return item;
    }

    @Override
    public BaseComponent[] getDealName(Player p) {
        ComponentBuilder builder = new ComponentBuilder()
            .append(Integer.toString(item.getAmount()))
            .color(ChatColor.WHITE)
            .append("x ")
            .append(Utility.getItemName(item));
        if (!implemented) builder.append(" (not implemented)").bold(true).color(ChatColor.RED);
        return builder.create();
    }

    @Override
    public Material getPriceType(Player p) {
        return priceType;
    }
    public boolean isImplemented() {
        return implemented;
    }

    @Override
    public int getPrice(Player p) {
        return price;
    }

    @Override
    public void commence(Player p) {
        if (!implemented) {
            p.sendMessage("The item you're trying to buy is not implemented yet.");
            return;
        }
        ItemStack item = getDealItem(p);
        ItemStack[] contents = p.getInventory().getContents();
        InventoryUtility.giveItem(contents, item);
        p.getInventory().setContents(contents);
        if (Game.isStarted()) {
            BedwarsPlayer bwp = Game.instance.getPlayer(p);
            if (bwp != null) WoodenSword.update(bwp, p.getInventory());
        }
        
    }

    public ItemDeal(ItemStack item, int price, Material priceType, boolean implemented) {
        this.item = item;
        this.price = price;
        this.priceType = priceType;
        this.implemented = implemented;    
    }

    @Override
    public boolean alreadyBought(Player p) {
        return false;
    }
}
