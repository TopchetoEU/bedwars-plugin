package me.topchetoeu.bedwars;

import org.bukkit.inventory.ItemStack;

public class InventoryUtility {
	public static boolean itemEquals(ItemStack a, ItemStack b, boolean ignoreAmount) {
		if (a == null && b == null) return true;
		if (a == null || b == null) return false;
		
		if (ignoreAmount) {
			a = a.clone();
			a.setAmount(1);
			b = b.clone();
			b.setAmount(1);
			
			return a.equals(b);
		}
		else return a.equals(b);
	}
	public static ItemStack giveItem(ItemStack[] inv, ItemStack _item) {
		ItemStack item = _item.clone();
		int remaining = item.getAmount();
		int maxStackSize = item.getMaxStackSize();
		
		for (int i = 0; i < 36; i++) {
			if (itemEquals(inv[i], item, true)) {
				if (inv[i].getAmount() < maxStackSize) {
					int newCount = remaining + inv[i].getAmount();
					if (newCount > maxStackSize) {
						inv[i].setAmount(maxStackSize);
						remaining = newCount - maxStackSize;
					}
					else {
						inv[i].setAmount(newCount);
						return null;
					}
				}
			}
		}
		
		item.setAmount(remaining);
		
		for (int i = 0; i < 36; i++) {
			if (inv[i] == null) {
				inv[i] = item;
				return null;
			}
		}
		
		return item;
	}
	public static boolean hasItem(ItemStack[] inv, ItemStack item) {
		int n = 0;
		
		for (int i = 0; i < inv.length; i++) {
			if (inv[i] != null) {
				if (itemEquals(inv[i], (item), true)) {
					n += inv[i].getAmount();
					if (n >= item.getAmount()) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	public static ItemStack takeItems(ItemStack[] inv, ItemStack item) {
		item = item.clone();
		
		for (int i = 0; i < inv.length; i++) {
			if (inv[i] != null) {
				if (itemEquals(inv[i], (item), true)) {
					int amount = inv[i].getAmount();
					if (item.getAmount() > amount) {
						item.setAmount(item.getAmount() - amount); 
						inv[i] = null;
					}
					else if (item.getAmount() == amount) {
						inv[i] = null;
						return null;
					}
					else if (item.getAmount() < amount) {
						inv[i].setAmount(inv[i].getAmount() - item.getAmount());
						return null;
					}
				}
			}
		}
		
		return item;
	}
}
