package me.topchetoeu.bedwars.engine;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class WoodenSword {
	public static boolean isOtherSword(Material mat) {
		return mat == Material.IRON_SWORD ||
				mat == Material.DIAMOND_SWORD ||
				mat == Material.STONE_SWORD ||
				mat == Material.STONE_SWORD;
	}
	public static boolean hasOtherSword(Inventory inv) {
		return inv.contains(Material.IRON_SWORD) ||
				inv.contains(Material.DIAMOND_SWORD) ||
				inv.contains(Material.STONE_SWORD) ||
				inv.contains(Material.STONE_SWORD);
	}
	public static void update(BedwarsPlayer p, Inventory inv) {
		if (hasOtherSword(inv))
			inv.remove(Material.WOOD_SWORD);
		else if (!inv.contains(Material.WOOD_SWORD))
			inv.addItem(p.getTeam().teamifyItem(new ItemStack(Material.WOOD_SWORD, 1), true, true));
	}
}
