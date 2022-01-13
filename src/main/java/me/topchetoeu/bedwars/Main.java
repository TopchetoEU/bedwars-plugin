package me.topchetoeu.bedwars;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import me.topchetoeu.bedwars.commandUtility.Command;
import me.topchetoeu.bedwars.commandUtility.CommandExecutors;
import me.topchetoeu.bedwars.engine.Config;
import me.topchetoeu.bedwars.engine.Game;
import me.topchetoeu.bedwars.engine.trader.Favourites;
import me.topchetoeu.bedwars.engine.trader.Sections;
import me.topchetoeu.bedwars.engine.trader.Traders;
import me.topchetoeu.bedwars.engine.trader.dealTypes.EnforcedRankedDealType;
import me.topchetoeu.bedwars.engine.trader.dealTypes.ItemDealType;
import me.topchetoeu.bedwars.engine.trader.dealTypes.RankedDealType;
import me.topchetoeu.bedwars.engine.trader.dealTypes.TeamUpgradeRanks;
import me.topchetoeu.bedwars.engine.trader.dealTypes.RankedUpgradeDealType;
import me.topchetoeu.bedwars.engine.trader.upgrades.BlindnessTeamUpgrade;
import me.topchetoeu.bedwars.engine.trader.upgrades.EfficiencyTeamUpgrade;
import me.topchetoeu.bedwars.engine.trader.upgrades.FatigueTeamUpgrade;
import me.topchetoeu.bedwars.engine.trader.upgrades.HealTeamUpgrade;
import me.topchetoeu.bedwars.engine.trader.upgrades.ProtectionTeamUpgrade;
import me.topchetoeu.bedwars.engine.trader.upgrades.SharpnessTeamUpgrade;

// TODO add permissions

public class Main extends JavaPlugin implements Listener {
	
	
	private static Main instance;
	private int playerCount;
	public static Main getInstance() {
		return instance;
	}
	
	private File confFile = new File(getDataFolder(), "config.yml");
	private int getGameSize() {
		return Config.instance.getTeamSize() * Config.instance.getColors().size();
	}
	
	int timer = 0;
	BukkitTask timerTask = null;
	
	private void stopTimer() {
		if (timerTask == null) return;
		Utility.broadcastTitle("Not enough players!", null, 10, 40, 5);
		timerTask.cancel();
		timerTask = null;
	}
	private void startTimer() {
		if (timerTask != null) return;
		
		timerTask = Bukkit.getScheduler().runTaskTimer(this, () -> {
			if (Game.isStarted()) {
				stopTimer();
				return;
			}
			if (timer % 30 == 0 || timer == 15 || timer == 10)
				Utility.broadcastTitle("Starting in " + timer + " seconds!", null, 10, 40, 5);
			else if (timer <= 5)
				Utility.broadcastTitle("Starting in " + timer + " seconds!", null, 0, 20, 0);
			timer--;
			if (timer <= 0) {
				Game.start();
				timerTask.cancel();
				timerTask = null;
			}
		}, 0, 20);
	}
	public void updateTimer() {
		// TODO make timing configurable
		if (!Game.isStarted()) {
			if (playerCount <= 1 || playerCount <= getGameSize() / 4) {
				Utility.broadcastTitle("Not enough players", "Waiting for more...", 0, 100, 0);
				stopTimer();
			}
			else if (playerCount <= getGameSize() / 2) {
				timer = 60;
				startTimer();
			}
			else if (playerCount <= getGameSize() - 1) {
				timer = 60;
				startTimer();
			}
			else {
				timer = 15;
				startTimer();
			}
		}
	}
	
	@EventHandler
	private void onJoin(PlayerJoinEvent e) {
		playerCount++;
		e.getPlayer().setGameMode(GameMode.SPECTATOR);
		updateTimer();
	}
	@EventHandler
	private void onLeave(PlayerQuitEvent e) {
		playerCount--;
		updateTimer();
	}
	
	@EventHandler
	private void onFoodLost(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		playerCount = Bukkit.getServer().getOnlinePlayers().size();
		try {
			instance = this;
			getDataFolder().mkdir();
			File conf = new File(getDataFolder(), "config.yml");
			if (!conf.exists())
				try {
					YamlConfiguration.loadConfiguration(
							getClass()
							.getClassLoader()
							.getResourceAsStream("config.yml")
					).save(confFile);
				}
				catch (IOException e) { /* Everything is fine */ }
			
			// Deprecation warnings are for beginners
			Config.load(conf);
			File defaultFavs = new File(getDataFolder(), "default-favourites.yml");
	
			if (!defaultFavs.exists()) {
				try {
					OutputStream w = new FileOutputStream(defaultFavs);
					InputStream r =  getClass()
						.getClassLoader()
						.getResourceAsStream("default-favourites.yml");
					
					w.write(r.readAllBytes());
					
					w.close();
					r.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			File favsDir = new File(getDataFolder(), "favourites");
	
			try {
				Traders.instance = new Traders(new File(getDataFolder(), "traders.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			YamlConfiguration sectionsConf = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "sections.yml"));

			BlindnessTeamUpgrade.init(this);
			FatigueTeamUpgrade.init(this);
			HealTeamUpgrade.init(this);
			EfficiencyTeamUpgrade.init();
			ProtectionTeamUpgrade.init();
			SharpnessTeamUpgrade.init();
			TeamUpgradeRanks.init(this, sectionsConf);
			
			ItemDealType.init();
			RankedDealType.init(this, sectionsConf);
			EnforcedRankedDealType.init();
			RankedUpgradeDealType.init();
			Sections.init(new File(getDataFolder(), "sections.yml"));
			Favourites.instance = new Favourites(favsDir, defaultFavs);
			
			updateTimer();
			
			getServer().getWorlds().get(0).getEntitiesByClass(Villager.class).forEach(v -> {
				net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) v).getHandle();
			    nmsEntity.b(true); // Disables its AI
			});
			
			Command cmd = new Command("bedwars", "bw").setExecutor(Commands._default);
			cmd
				.attachCommand(new Command("help")
					.setExecutor(CommandExecutors.help(cmd))
					.setHelpMessage("Shows help for the command")
				)
				.attachCommand(new Command("start")
					.setExecutor(Commands.start)
					.setHelpMessage("Starts the game")
				)
				.attachCommand(new Command("stop")
					.setExecutor(Commands.stop)
					.setHelpMessage("Stops the game, noone wins")
				)
				.attachCommand(new Command("respawn", "revive")
					.setExecutor(Commands.revive)
					.setHelpMessage("Respawns a spectator, if he has a bed, he is immediatly respawned"))
				.attachCommand(new Command("breakbed", "eliminateteam")
					.setExecutor(Commands.breakBed)
					.setHelpMessage("Destoys the bed of a team")
				)
				.attachCommand(new Command("eliminate")
					.setHelpMessage("Eliminates a player")
				)
				.attachCommand(new Command("kill")
					.setExecutor(Commands.kill)
					.setHelpMessage("Kills a player")
				)
				.attachCommand(new Command("killteam")
					.setHelpMessage("Kills all players of a team")
				)
				.attachCommand(new Command("villagertools", "villager", "trader")
					.setExecutor((sender, _cmd, alias, args) -> {
						if (args.length == 0) {
							if (sender instanceof Player) {
								Player p = (Player)sender;
								
								p.getInventory().addItem(Utility.namedItem(new ItemStack(Material.MONSTER_EGG), "§rTrader spawner"));
								p.getInventory().addItem(Utility.namedItem(new ItemStack(Material.STICK), "§rTrader eradicator"));
							}
						}
					})
					.setHelpMessage("Gives you tools to manage traders")
				)
				.attachCommand(new Command("config", "conf", "settings")
					.setExecutor(Commands._default)
					.setHelpMessage("Command for configuring the map")
					.attachCommand(new Command("spawn")
						.setHelpMessage("Sets the spawn at which the platform is going to be spawned, and where spectators are going to be spawned")
					)
					.attachCommand(new Command("base", "b")
						.setExecutor(Commands._default)
						.setHelpMessage("Command for configuring separate bases")
						.attachCommand(new Command("new", "add", "create", "c")
							.setExecutor(Commands.baseAdd)
							.setHelpMessage("Creates a base with a color, chat id and a wool id. NOTE: for chat id, do the following: if in chat the color you want is &2, specify just '2'")
						)
						.attachCommand(new Command("remove", "delete", "del", "d")
							.setExecutor(Commands.baseRemove)
							.setHelpMessage("Removes a base with the selected name")
						)
						.attachCommand(new Command("setbed", "bed", "b")
							.setExecutor(Commands.baseSetBed)
							.setHelpMessage("Sets the location of the bed. Any broken bed within 5 blocks of the specified location will trigger the breaking of the team's bed")
						)
						.attachCommand(new Command("setgenerator", "generator", "setgen", "gen", "g")
							.setExecutor(Commands.baseSetGenerator)
							.setHelpMessage("Sets the location of the generator. Anyone within 2 blocks of it will pick up the produced items")
						)
						.attachCommand(new Command("setspawn", "spawn", "s")
							.setExecutor(Commands.baseSetSpawn)
							.setHelpMessage("Sets the location where players of the team will respawn")
						)
						.attachCommand(new Command("list", "l")
							.setExecutor(Commands.baseList)
							.setHelpMessage("Lists all bases")
						)
					)
					.attachCommand(new Command("generator", "gen", "g")
						.setExecutor(Commands._default)
						.setHelpMessage("Command for configuring the global generators")
						.attachCommand(new Command("diamond", "d")
							.setExecutor(Commands.createDiamondGen)
							.setHelpMessage("Creates a diamond generator in (approximately) your position")
						)
						.attachCommand(new Command("emerald", "e")
							.setExecutor(Commands.createEmeraldGen)
							.setHelpMessage("Creates a emerald generator in (approximately) your position")
						)
						.attachCommand(new Command("remove", "delete", "del", "r")
							.setHelpMessage("Deletes all generators within 5 block of your position")
						)
					)
				)
				.attachCommand(new Command("diemydarling")
					.setExecutor((a, b, c, d) -> {
						Bukkit.getWorld("world")
							.getEntities()
							.stream()
							.filter(v -> v instanceof ArmorStand)
							.forEach(v -> v.remove());
					})
				)
				.register(this);
			
			getServer().getPluginManager().registerEvents(this, this);
		}
		catch (Throwable t) {
			getLogger().log(Level.SEVERE, "Failed to initialize. Config files are probably to blame", t);
			getServer().broadcastMessage("§4The bedwars plugin failed to initialize. Many, if not all parts of the plugin won't work. Check console for details and stack trace.");
		}
	}
	public void onDisable() {
		if (Game.isStarted()) Game.instance.close();
	}
}
