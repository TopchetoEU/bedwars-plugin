package me.topchetoeu.bedwars.engine.trader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.topchetoeu.bedwars.InventoryUtility;
import me.topchetoeu.bedwars.Main;
import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.trader.dealTypes.ItemDeal;

// Very bad code.
public class TraderGUI implements Listener {
	private List<Section> sections;
	private Inventory inventory = null;
	private Player player;
	private int currSectionN = -1;
	private Map<Integer, DealPtr> favourites;
	private int currFavourite = -1;
	private int sectionsOffset = 0;
	private boolean sectionsOverflow = false;
	
	public Inventory getInventory() {
		return inventory;
	}
	public Player getPlayer() {
		return player;
	}
	
	public ItemStack generateDealItem(Deal d, boolean addFavouriteLore) {
		String name = "§r" + d.getDealName(player);
		if (d.alreadyBought(player)) name += " §4(already unlocked)";
		else name += String.format(" (%dx %s)", 
			d.getPrice(player),
			Utility.getItemName(d.getPriceType(player))
		);

		ItemStack item = Utility.copyNamedItem(d.getDealItem(player), name);
		
		if (addFavouriteLore) {
			List<String> lore = new ArrayList<>();
			lore.add("§rShift + Left click to set slot");
			lore.add("§rShift + Right click to reset");
			
			ItemMeta meta = item.getItemMeta();
			if (meta.getLore() != null) lore.addAll(meta.getLore());
			meta.setLore(lore);
			item.setItemMeta(meta);
		}
		
		return item;
	}
	
	public void setSectionOffset(int n) {
		sectionsOffset = n;
		ItemStack[] invC = inventory.getContents();

		ItemStack blackGlassPanes = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15);
		ItemMeta meta = blackGlassPanes.getItemMeta();
		meta.setDisplayName(" ");
		blackGlassPanes.setItemMeta(meta);
		
		for (int i = 1; i < 8; i++) {
			invC[i] = blackGlassPanes;
		}
		
		if (sectionsOffset == 0)
			invC[1 - sectionsOffset] = Utility.namedItem(new ItemStack(Material.NETHER_STAR), "§rFavourites");
		
		for (int i = 0; i < sections.size(); i++) {
			Section sec = sections.get(i);
			
			int index = i + 2 - sectionsOffset;
			if (index > 0 && index < 8) invC[index] = sec.getIcon();
		}
		inventory.setContents(invC);
	}
	public void selectSection(int n) {
		if (n == -1)
			selectFavourite();
		else {
			if (n < 0 || n >= sections.size()) return;
			
			Section sec = sections.get(n);
			
			ItemStack[] contents = inventory.getContents();
			
			for (int x = 1; x < 8; x++) {
				for (int y = 1; y < 5; y++) {
					contents[x + y * 9] = null;
				}
			}
			
			for (int i = 0; i < sec.getDeals().size(); i++) {
				Deal d = sec.getDeal(i);
				contents[i % 7 + i / 7 * 9 + 10] = generateDealItem(d, false);
			}
			inventory.setContents(contents);
		}
		
		player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
		
		currSectionN = n;
	}
	public void selectFavourite() {
		
		ItemStack[] contents = inventory.getContents();
		
		ItemStack empty = Utility.namedItem(new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)8), "§rEmpty slot");

		ItemMeta meta = empty.getItemMeta();
		List<String> lore = new ArrayList<>();
		lore.add("§rShift + Left click to set slot");
		meta.setLore(lore);
		empty.setItemMeta(meta);
		
		for (int x = 1; x < 8; x++) {
			for (int y = 1; y < 5; y++) {
				contents[x + y * 9] = empty;
			}
		}
		
		for (Integer n : favourites.keySet()) {
			Deal d = favourites.get(n).getDeal(sections);
			
			if (d != null) contents[n % 7 + n / 7 * 9 + 10] = generateDealItem(d, true);
		}
		
		inventory.setContents(contents);
		
		currSectionN = -1;
	}
	
	public void updateSection() {
		selectSection(currSectionN);
	}
	
	public void trade(Deal deal) {		
		if (deal.alreadyBought(player)) {
			player.sendMessage("You already own this.");
			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
		}
		else {
			ItemStack[] inv = player.getInventory().getContents();
			ItemStack price = new ItemStack(deal.getPriceType(player), deal.getPrice(player));

			
			if (deal instanceof ItemDeal && !((ItemDeal)deal).isImplemented()) {
				deal.commence(player);
			}
			else {
				
				if (InventoryUtility.hasItem(inv, price)) {
					InventoryUtility.takeItems(inv, price);
					
					player.getInventory().setContents(inv);
					
					deal.commence(player);
					updateSection();
					if (player.getInventory().contains(Material.STONE_SWORD) ||
						player.getInventory().contains(Material.IRON_SWORD) ||
						player.getInventory().contains(Material.DIAMOND_SWORD)) {
						if (player.getInventory().contains(Material.WOOD_SWORD)) {
							player.getInventory().remove(Material.WOOD_SWORD);
						}
					}
					player.playSound(player.getLocation(), Sound.VILLAGER_YES, 1, 1);
				}
				else {
					player.sendMessage(String.format("You don't have enough %ss!", Utility.getItemName(deal.getPriceType(player)).toLowerCase()));
					player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
				}
			}
		}
	}
	
	private void generateInventory(Inventory inv) {
		ItemStack blackGlassPanes = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15);
		ItemMeta meta = blackGlassPanes.getItemMeta();
		meta.setDisplayName(" ");
		blackGlassPanes.setItemMeta(meta);
		
		ItemStack[] invC = inv.getContents();
		
		for (int i = 0; i < 6; i++) {
			invC[i * 9] = blackGlassPanes;
			invC[i * 9 + 8] = blackGlassPanes;
		}
		for (int i = 0; i < 9; i++) {
			invC[i] = blackGlassPanes;
			invC[i + 45] = blackGlassPanes;
		}
		
		if (sectionsOverflow) {
			invC[0] = Utility.namedItem(new ItemStack(Material.ARROW), "§rBack");
			invC[8] = Utility.namedItem(new ItemStack(Material.ARROW), "§rForward");
		}
		
		inv.setContents(invC);
		
		ItemStack exitItem = new ItemStack(Material.BARRIER);
		meta = exitItem.getItemMeta();
		meta.setDisplayName("§rExit");
		exitItem.setItemMeta(meta);
		inv.setItem(49, exitItem);
		
		setSectionOffset(0);
	}
	
	public Inventory open() {
		inventory = Bukkit.createInventory(null, 54, "Trader");
		generateInventory(inventory);
		selectFavourite();
		
		inventory = player.openInventory(inventory).getTopInventory();
		
		return inventory;
	}
	public void dispose() {
		HandlerList.unregisterAll(this);
		sections = null;
		inventory = null;
	}
		
	private void setFavourite(DealPtr d) {
		if (currFavourite < 0) return;
		if (d == null && favourites.containsKey(currFavourite)) {
			favourites.remove(currFavourite);
			Favourites.instance.updateFavourites(player, favourites);
			player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1, 1);
		}
		else {
			favourites.put(currFavourite, d);
			Favourites.instance.updateFavourites(player, favourites);
			player.playSound(player.getLocation(), Sound.ORB_PICKUP, 1, 1);
		}
		
		currFavourite = -1;
		selectFavourite();
	}
	
	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {
		if ((Object)inventory == (Object)e.getInventory()) {
			e.setCancelled(true);
			if (e.getClickedInventory() == e.getInventory()) {
				if (e.getClick() != ClickType.LEFT && e.getClick() != ClickType.SHIFT_LEFT && e.getClick() != ClickType.SHIFT_RIGHT) return;
				int slot = e.getSlot();
				
				if (slot == 0 && sectionsOffset > 0) {
					setSectionOffset(sectionsOffset - 1);
					return;
				}
				if (slot == 8 && sectionsOffset < sections.size() - 6) {
					setSectionOffset(sectionsOffset + 1);
					return;
				}
				
				if (slot % 9 == 8 || slot % 9 == 0) {
					if (currFavourite >= 0) setFavourite(null);
					return;
				}

				if (slot == 49) {
					player.closeInventory();
					player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
					return;
				}
				else if (slot < 9) selectSection(slot - 2 + sectionsOffset); // Section
				else if (slot / 9 == 5) {
					if (currFavourite >= 0) setFavourite(null);
				}
				else {
					int x = slot % 9 - 1;
					int y = slot / 9 - 1;
					
					int n = x + y * 7;
					
					Deal d = null;
					
					if (currSectionN >= 0) {
						Section s = sections.get(currSectionN);
						d = s.getDeal(n);
					}
					else if (favourites.containsKey(n)) {
						d = favourites.get(n).getDeal(sections);
					}
					
					if (currSectionN < 0) {
						if (e.getClick() == ClickType.SHIFT_LEFT) {
							player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
							currFavourite = n; return;
						}
						else if (e.getClick() == ClickType.SHIFT_RIGHT) {
							currFavourite = n;
							setFavourite(null);
							return;
						}
					}
					
					if (d != null) {
						if (currFavourite < 0) trade(d);
						else {
							if (currSectionN < 0) setFavourite(favourites.get(n));
							else setFavourite(new DealPtr(currSectionN, n));
						}
					}
				}
			}
		}
	}
	@EventHandler
	private void onInventoryClose(InventoryCloseEvent e) {
		if (e.getInventory() == inventory) {
			dispose();
		}
	}
	
	public TraderGUI(File favsDir, Player p) {
		this.sections = Sections.getSections();
		this.player = p;
		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
		this.favourites = Favourites.instance.getFavourites(p);
		this.sectionsOverflow = sections.size() + 1 > 7;
	}
}
