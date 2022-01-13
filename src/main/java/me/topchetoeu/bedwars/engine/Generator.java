package me.topchetoeu.bedwars.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import me.topchetoeu.bedwars.Main;

public class Generator implements AutoCloseable, Listener {
	private class _Item implements AutoCloseable {
		private BukkitTask task;
		private BukkitTask timerTask;
		private Generator parentInstance;
		private int timer = 0;
		
		public void close() {
			task.cancel();
			timerTask.cancel();
			timerTask = null;
			task = null;
		}
		
		public _Item(int interval, Material type, Generator gen) {
			parentInstance = gen;
			timer = interval;
			this.task = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
				Item i = location.getWorld().dropItem(location, new ItemStack(type, 1));
				i.setVelocity(new Vector(0, 0, 0));
				parentInstance.generatedItems.add(i.getUniqueId());
				
				if (parentInstance.generatedItems.size() > parentInstance.maxItems) {
					if (parentInstance.generatedItems.size() != 0) {
						UUID removed = parentInstance.generatedItems.stream().findFirst().get();
						
						Bukkit.getServer().getWorlds()
							.stream()
							.flatMap(v -> v.getEntities().stream())
							.filter(v -> v.getUniqueId().equals(removed))
							.findFirst()
							.ifPresent(v -> v.remove());
						
						parentInstance.generatedItems.remove(removed);
					}
				}
			}, interval, interval);
			this.timerTask = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
				timer--;	
				if (timer == 0) timer = interval;
				
				if (parentInstance.label != null) parentInstance.label.setRemaining(type, timer / 20f);
			}, 0, 1);
		}
	}
	
	private static HashSet<Generator> generators = new HashSet<>();
	
	private HashSet<UUID> generatedItems = new HashSet<>();
	private Hashtable<Material, _Item> itemGenerators = new Hashtable<>();
	private Location location;
	private int maxItems;

	private GeneratorLabel label;
	
	public Location getLocation() {
		return location;
	}
	
	public void addItem(Material type, int interval) {
		if (itemGenerators.contains(type)) removeItem(type);
		itemGenerators.put(type, new _Item(interval, type, this));
	}
	public void removeItem(Material type) {
		itemGenerators.remove(type).close();
	}
	
	public GeneratorLabel getLabel() {
		return label;
	}

	@EventHandler
	private void onItemMerge(ItemMergeEvent e) {
		if (generatedItems.contains(e.getEntity().getUniqueId()) || generatedItems.contains(e.getTarget().getUniqueId()))
			e.setCancelled(true);
	}
	
	@EventHandler
	private void onPickup(PlayerPickupItemEvent e) {
		if (generatedItems.contains(e.getItem().getUniqueId())) {
			e.setCancelled(true);
			
			for (BedwarsPlayer bwp : Game.instance.getPlayers()) {
				if (bwp.isOnline()) {
					Player p = bwp.getOnlinePlayer();
					
					if (p.getLocation().distance(e.getItem().getLocation()) < 2) {
						p.playSound(e.getItem().getLocation(), Sound.ITEM_PICKUP, .5f, 2);
						p.getInventory().addItem(e.getItem().getItemStack());
					}
				}
			}
			generatedItems.remove(e.getItem().getUniqueId());
			e.getItem().remove();
		}
	}
	
	public void close() {
		for (Material m : new ArrayList<>(itemGenerators.keySet())) {
			removeItem(m);
		}
		HandlerList.unregisterAll(this);
		
		itemGenerators = null;
		generatedItems = null;
	}
	
	public static Set<Generator> getGenerators() {
		return Collections.unmodifiableSet(generators);
	}
	
	public Generator(Location loc, int maxItems, GeneratorLabel label) {
		this.location = loc;
		this.maxItems = maxItems;
		this.label = label;
		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
		
		generators.add(this);
	}
}
