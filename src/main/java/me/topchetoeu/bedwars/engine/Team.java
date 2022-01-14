package me.topchetoeu.bedwars.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.topchetoeu.bedwars.Main;
import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.trader.upgrades.TeamUpgrade;

public class Team implements Listener, AutoCloseable {
	private TeamColor color;
	private ArrayList<BedwarsPlayer> players = new ArrayList<>();
	private Hashtable<String, TeamUpgrade> upgrades = new Hashtable<>();
	private Generator generator;
	
	private boolean bed = true;
	private int playersCount;
	private int remainingPlayers;
	
	public boolean hasBed() {
		return bed;
	}
	public boolean destroyBed(OfflinePlayer player) {
		if (!bed) return false;		
		World world = Bukkit.getWorlds().get(0);
		
		for (int x = -5; x < 5; x++) {
			for (int y = -5; y < 5; y++) {
				for (int z = -5; z < 5; z++) {
					Block block = new Location(
						world,
						color.getBedLocation().getBlockX() + x,
						color.getBedLocation().getBlockY() + y,
						color.getBedLocation().getBlockZ() + z
					).getBlock();
					if (Utility.isBed(block)) {
						Game.instance.breakBlock(block);
					}
				}
			}
		}
		
		bed = false;
		for (BedwarsPlayer bwp : players) {
			if (bwp.isOnline()) {
				Player p = bwp.getOnlinePlayer();
				
				String msg = color.getColorName() + "§r's bed was destroyed";
				if (player != null) msg += " by " + player.getName();
				msg += ".";
				Bukkit.broadcastMessage(msg);
				
				Utility.sendTitle(p, "Bed destroyed!", "You will no longer respawn!", 5, 35, 10);
				p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
			}
		}
		
		ScoreboardManager.updateAll();
		
		return true;
	}
	
	public int decreaseRemainingPlayers() {
		return --remainingPlayers;
	}
	public int getRemainingPlayers() {
		return remainingPlayers;
	}
	public int getPlayersCount() {
		return playersCount;
	}
	public void resetRemainingPlayers() {
		remainingPlayers = playersCount;
	}

	public void removeUpgrade(Class<? extends TeamUpgrade> type) {
		upgrades.keySet().removeIf(k -> {
			TeamUpgrade v = upgrades.get(k);
			return v.getClass().equals(type);
		});
	}
	public void addUpgrade(TeamUpgrade upgrade) {
		upgrades.put(upgrade.getName(), upgrade);
	}
	public void updateUpgrades() {
		upgrades.values().forEach(v -> v.updateTeam(this));
	}
	public Collection<TeamUpgrade> getUpgrades() {
		return upgrades.values();
	}
	public boolean hasUpgrade(TeamUpgrade upgrade) {
		return upgrades.contains(upgrade);
	}
	
	public TeamColor getTeamColor() {
		return color;
	}
	
	public List<BedwarsPlayer> getPlayers() {
		return Collections.unmodifiableList(players);
	}
	
	public void removePlayer(BedwarsPlayer p) {
		if (players.remove(p)) {
			playersCount--;
			if (!p.isSpectator()) remainingPlayers--;
		}
		
	}
	public void addPlayer(BedwarsPlayer p) {
		if (players.add(p)) {
			playersCount++;
			if (!p.isSpectator()) remainingPlayers++;
		}
	}
	public void addPlayer(OfflinePlayer p) {
		if (!hasPlayer(p)) {
			players.add(new BedwarsPlayer(p, this));
			playersCount++;
			if (bed) remainingPlayers++;
		}
	}
	
	public boolean hasPlayer(BedwarsPlayer p) {
		return hasPlayer(p.getPlayer());
	}
	public boolean hasPlayer(OfflinePlayer p) {
		return players
			.stream()
			.filter(v -> v.getPlayer().getUniqueId().equals(p.getUniqueId()))
			.findFirst()
			.isPresent();
	}
	
	public boolean isEliminated() {
		return remainingPlayers == 0;
	}

	@Override
	public void close() {
		generator.close();
		
		for (BedwarsPlayer bwp : players) {
			bwp.close();
		}
		
		HandlerList.unregisterAll(this);
		
		players = null;
		generator = null;
	}
	
	@EventHandler
	private void onBlockBreak(BlockBreakEvent e) {
		if (Utility.isBed(e.getBlock())) {
			if (e.getBlock().getLocation().distance(color.getBedLocation()) < 5) {
				if (hasPlayer(e.getPlayer())) {
					e.setCancelled(true);
					if (getPlayersCount() == 1) {
						e.getPlayer().sendMessage("§4You may not destroy your bed.");
					}
					else {
						e.getPlayer().sendMessage("§4You may not destroy your team's bed.");
					}
				}
				else {
					if (bed) {
						e.setCancelled(true);
						destroyBed(e.getPlayer());
					}
				}
			}
		}
	}
	
	public void sendMessage(String msg) {
		for (BedwarsPlayer bwp : players) {
			if (bwp.isOnline()) bwp.getOnlinePlayer().sendMessage(msg);
		}
	}
	public void sendTitle(String title, String subtitle, int fadein, int duration, int fadeout) {
		for (BedwarsPlayer bwp : players) {
			if (bwp.isOnline()) Utility.sendTitle(bwp.getOnlinePlayer(), title, subtitle, fadein, duration, fadeout);
		}
	}
	public void sendTilteToOthers(String title, String subtitle, int fadein, int duration, int fadeout) {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (players.stream().noneMatch(v -> v.getPlayer().getUniqueId().equals(p.getUniqueId())))
				Utility.sendTitle(p, title, subtitle, fadein, duration, fadeout);
		}
	}
	
	public ItemStack teamifyItem(ItemStack stack, boolean colour, boolean upgrades) {
		if (colour) {
			if (Utility.isWool(stack.getType())) stack.setType(color.getWoolMaterial());
			else {
				ItemMeta meta = stack.getItemMeta();
				
				if (meta instanceof LeatherArmorMeta) {
					LeatherArmorMeta armour = (LeatherArmorMeta)meta;
					armour.setColor(color.getColor());
					stack.setItemMeta(armour);
				}
			}
		}
		
		if (upgrades) {
			this.upgrades.values().forEach(v -> {
				v.upgradeItem(stack);
			});
		}
		
		return stack;
	}
	
	
	public Team(TeamColor color, Player... players) {
		this.players.addAll(Arrays.asList(players)
			.stream()
			.map(v -> new BedwarsPlayer(v, this))
			.collect(Collectors.toList())
		);
		this.color = color;
		this.playersCount = this.remainingPlayers = players.length;
		
		this.generator = new Generator(color.getGeneratorLocation(), 48, null);
		this.generator.addItem(Material.IRON_INGOT, 20);
		this.generator.addItem(Material.GOLD_INGOT, 80);
		
		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
	}
}
