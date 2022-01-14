package me.topchetoeu.bedwars.engine;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.topchetoeu.bedwars.Main;
import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.trader.dealTypes.RankedDealType;

public class Game implements Listener, AutoCloseable {
	public static Game instance = null;
	private static Deque<Location> placedBlocks = new ArrayDeque<>();
	private static HashSet<SavedBlock> brokenBlocks = new HashSet<>();
	// private static int onlineCount;
	
	public static boolean inGame(OfflinePlayer p) {
		return isStarted() && instance.isPlaying(p);
	}
	public static void start() {
		Game.instance = new Game(
			new ArrayList<TeamColor>(Config.instance.getColors().stream().filter(v -> v.isFullySpecified()).collect(Collectors.toList())),
			Config.instance.getTeamSize(),
			new ArrayList<OfflinePlayer>(Bukkit.getOnlinePlayers())
		);

		Bukkit.getOnlinePlayers().forEach(p -> {
			RankedDealType.getDefinedRanks().values().forEach(v -> v.refreshInv(p.getPlayer()));
		});
		
		ScoreboardManager.updateAll();
	}
	
	public static void stop() {
		Game.instance.close();
		Game.instance = null;
		Main.getInstance().updateTimer();
		
		ScoreboardManager.updateAll();
	}
	
	public static void stop(boolean immediatly) {
		if (immediatly) stop();
		else Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> stop(), 20 * 5);
	}
	public static boolean isStarted() {
		return instance != null;
	}

	private ArrayList<Team> teams = new ArrayList<>();
	private ArrayList<Generator> diamondGens = new ArrayList<>();
	private ArrayList<Generator> emeraldGens = new ArrayList<>();
	
	public List<Generator> getDiamondGenerators() {
		return Collections.unmodifiableList(diamondGens);
	}
	public List<Generator> getEmeraldGenerators() {
		return Collections.unmodifiableList(emeraldGens);
	}

	public void teamifyItem(Player player, ItemStack item, boolean colour, boolean enchant) {
		Team t = getTeam(player);
		if (t != null) t.teamifyItem(item, colour, enchant);
	}
	
	public void win(TeamColor color) {
		getTeam(color).sendTitle("You won!", "", 0, 20 * 5, 0);
		getTeam(color).sendTilteToOthers(color.getColorName() + " won!", "You lost :(", 0, 20 * 5, 0);
		stop(false);
	}
	
	public void breakBlock(Block block) {
		SavedBlock bl = new SavedBlock(block.getLocation(), block.getState().getBlockData(), block.getState().getType());
		brokenBlocks.add(bl);
		block.setType(Material.AIR, false);
	}
	public void registerBrokenBlock(Location block) {
		placedBlocks.remove(block);
	}
	public void registerPlacedBlock(Location block) {
		placedBlocks.push(block);
	}
	
	public boolean allowPlace(Location loc) {
		for (Generator gen : Generator.getGenerators()) {
			if (gen.getLocation().distance(loc) < 5) return false;
		}
		return true;
	}
	public boolean allowBreak(Location loc) {
		if (!isStarted()) return true;
		return placedBlocks.contains(loc) || Utility.isBed(loc.getWorld().getBlockAt(loc));
	}
	
	public ArrayList<Team> getTeams() {
		return teams;
	}
	public List<Team> getAliveTeams() {
		return teams.stream().filter(v -> !v.isEliminated()).collect(Collectors.toList());
	}
	public Team getTeam(TeamColor color) {
		return teams
			.stream()
			.filter(v -> v.getTeamColor() == color)
			.findFirst()
			.orElse(null);
	}
	public Team getTeam(OfflinePlayer player) {
		return teams
			.stream()
			.filter(v -> v.getPlayers().stream().anyMatch(p -> p.equals(player)))
			.findFirst()
			.orElse(null);
	}
	public ArrayList<BedwarsPlayer> getPlayers() { 
		ArrayList<BedwarsPlayer> res = new ArrayList<>();
		
		for (Team team : teams) {
			res.addAll(team.getPlayers());
		}
		
		return res;
	}
	public BedwarsPlayer getPlayer(OfflinePlayer player) { 
		Optional<BedwarsPlayer> _p = getPlayers()
				.stream()
				.filter(v -> v.getPlayer().getUniqueId().equals(player.getUniqueId()))
				.findFirst();
		
		if (_p.isPresent()) return _p.get();
		else return null;
	}

	public boolean isPlaying(OfflinePlayer p) {
		return getPlayer(p) != null;
	}

	public void close() {
		for (Team team : teams) {
			team.close();
		}
		for (World w : Bukkit.getWorlds()) {
			for (Entity e : w.getEntities()) {
				if (!(e instanceof Player))
				if (!(e instanceof Villager))
				if (!(e instanceof ArmorStand))
					e.remove();
			}
		}
		
		for (Generator gen: diamondGens) {
			gen.close();
			gen.getLabel().close();
		}
		for (Generator gen: emeraldGens) {
			gen.close();
			gen.getLabel().close();
		}
		
		RankedDealType.resetPlayerTiers();
		
		HandlerList.unregisterAll(this);
		
		for (Location placedBlock : placedBlocks) {
			placedBlock.getBlock().setType(Material.AIR);
		}
		for (SavedBlock brokenBlock : brokenBlocks) {
			brokenBlock.loc.getBlock().setType(brokenBlock.type, false);
			brokenBlock.loc.getBlock().setBlockData(brokenBlock.meta, false);
		}
		
		placedBlocks.clear();
		brokenBlocks.clear();
		
		teams = null;
	}
	
	@EventHandler
	private void onLogout(PlayerQuitEvent e) {
		if (isPlaying(e.getPlayer())) {
			e.setQuitMessage(e.getPlayer().getName() + " logged out.");
		}
	}
	@EventHandler
	private void onLogin(PlayerJoinEvent e) {
		if(!isPlaying(e.getPlayer())) {
			e.getPlayer().setGameMode(GameMode.SPECTATOR);
			e.getPlayer().teleport(Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
			Utility.sendTitle(e.getPlayer(), "You are now spectating", null, 5, 35, 10);
		}
		else
			e.setJoinMessage(e.getPlayer().getName() + " reconnected.");
	}
	@EventHandler
	private void onInventoryClick(InventoryClickEvent e) {		
		if (isStarted() && isPlaying((Player)e.getWhoClicked())) {
			if (e.getClickedInventory() instanceof CraftingInventory) e.setCancelled(true);
		}
	}
	@EventHandler
	private void onItemDamage(PlayerItemDamageEvent e) {
		e.setCancelled(true);
	}

	
	private static boolean isExceptional(Material mat) {
		return !mat.isSolid();
	}

	@EventHandler
	private boolean onBlockBreak(Block b) {
		if (isExceptional(b.getType())) {
			b.getDrops().forEach(v -> b.getWorld().dropItemNaturally(b.getLocation().add(0.5, 0.5, 0.5), v));
			breakBlock(b);
		}
		else if (Utility.isBed(b)) {
			return false;
		}
		else if (allowBreak(b.getLocation())) {
			registerBrokenBlock(b.getLocation());
			b.breakNaturally();
		}
		else {
			return false;
		}
		
		return true;
	}
	@EventHandler
	private void onBlockBreak(BlockBreakEvent e) {
		e.setCancelled(true);
		onBlockBreak(e.getBlock());
	}
	@SuppressWarnings("incomplete-switch")
	@EventHandler
	private void onBlockPlace(PlayerBucketEmptyEvent e) {
		
		Location loc = e.getBlockClicked().getLocation();
		
		switch (e.getBlockFace()) {
		case UP:
			loc.add(0, 1, 0);
			break;
		case DOWN:
			loc.add(0, -1, 0);
			break;
		case EAST:
			loc.add(1, 0, 0);
			break;
		case WEST:
			loc.add(-1, 0, 0);
			break;
		case NORTH:
			loc.add(0, 0, -1);
			break;
		case SOUTH:
			loc.add(0, 0, 1);
			break;
		}
		
		if (!allowPlace(loc)) {
			e.setCancelled(true);
			return;
		}
		
		if (loc.getBlock().getType() != Material.AIR) {
			if (!allowBreak(loc)) e.setCancelled(true);
		}
		else registerPlacedBlock(loc);
	}
	@EventHandler
	@SuppressWarnings("incomplete-switch")
	private void onBlockPlace(BlockPlaceEvent e) {
		if (e.getBlock().getType() == Material.TNT) {
			e.setCancelled(true);
			Utility.takeOne(e.getPlayer(), e.getHand());
			e.getBlock().getWorld().spawnEntity(e.getBlock().getLocation().add(.5, 0, .5), EntityType.PRIMED_TNT).setVelocity(new Vector(0, 0, 0));
		}
		else {
			switch (e.getBlockReplacedState().getType()) {
				case WATER:
				case LAVA:
					if (!allowBreak(e.getBlock().getLocation())) e.setCancelled(true);
					return;
				case AIR:
					if (!allowPlace(e.getBlock().getLocation())) {
						e.setCancelled(true);
						return;
					}
					break;
				case GRASS:
				case TALL_GRASS:
					break;
			}
	
			if (!e.isCancelled()) {
				registerPlacedBlock(e.getBlock().getLocation());
			}
		}
	}
	@EventHandler
	private void onWaterPassTrough(BlockFromToEvent e) {
		e.setCancelled(true);
	}
	@EventHandler
	private void onEntityExplode(EntityExplodeEvent e) {
		e.setCancelled(true);
		for (Block b : e.blockList()) {
			if (b.getType() != Material.GLASS) {
				onBlockBreak(b);
			}
		}
		
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.playSound(e.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
		}
	}
	@EventHandler
	private void onUse(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (e.getItem() == null) return;
			if (e.getItem().getType() == Material.FIRE_CHARGE) {
			
				Utility.takeOne(e.getPlayer(), e.getHand());
				
				Location loc = e.getPlayer().getEyeLocation();
				
				Fireball fireball = (Fireball)e.getPlayer().getWorld().spawnEntity(
					loc.add(loc.getDirection().multiply(.5)),
					EntityType.FIREBALL
				);
				
				fireball.getLocation().add(fireball.getDirection().multiply(10));
				fireball.setShooter(e.getPlayer());
				fireball.setYield(3);
				e.setCancelled(true);
			}
		}
	}
	
	public Game(ArrayList<TeamColor> colors, int perTeam, ArrayList<OfflinePlayer> players) {
		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
		int spectatorsCount = players.size() - perTeam * colors.size();
		ArrayList<OfflinePlayer> spectators = new ArrayList<OfflinePlayer>();
		
		if (spectatorsCount > 0) {
			for (int i = 0; i < spectatorsCount; i++) {
				OfflinePlayer removed = players.remove((int)(Math.random() * players.size()));
				spectators.add(removed);
				if (removed.isOnline()) {
					removed.getPlayer().setGameMode(GameMode.SPECTATOR);
					removed.getPlayer().teleport(Config.instance.getRespawnLocation());
				}
				if (removed.isOnline()) removed.getPlayer().sendMessage("You will be a spectator");
			}
		}

		this.teams = new ArrayList<>();	
		
		if (colors.size() != 0) {
			int colorI = 0;
			
			for (TeamColor color : colors) {
				this.teams.add(new Team(color));
			}
			
			while (!players.isEmpty()) {
				Team currTeam = this.teams.get(colorI);
				OfflinePlayer p;
				
				currTeam.addPlayer(p = players.remove((int)(Math.random() * players.size())));
				
				if (p.isOnline()) {
					// onlineCount++;
				}
				
				if (currTeam.getPlayersCount() == perTeam) {
					colorI++;
				}
			}
		}
		
		// TODO: Make times configurable
		for (Location loc : Config.instance.getDiamondGenerators()) {
			GeneratorLabel label = new GeneratorLabel("§cDiamond Generator", loc.clone().add(0, 1, 0));
			Generator gen = new Generator(loc, 4, label);
			gen.addItem(Material.DIAMOND, 600);
			
			diamondGens.add(gen);
		}
		for (Location loc : Config.instance.getEmeraldGenerators()) {
			GeneratorLabel label = new GeneratorLabel("§cEmerald Generator", loc.clone().add(0, 1, 0));
			Generator gen = new Generator(loc, 2, label);
			gen.addItem(Material.EMERALD, 1200);
			
			emeraldGens.add(gen);
		}
	}
	
}
