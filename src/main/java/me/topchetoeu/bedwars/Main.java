package me.topchetoeu.bedwars;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
    
    // private File confFile = new File(getDataFolder(), "config.yml");
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

    @Override
    public void onEnable() {
        playerCount = Bukkit.getServer().getOnlinePlayers().size();
        try {
            instance = this;
            getDataFolder().mkdir();
            File conf = new File(getDataFolder(), "config.yml");
            if (!conf.exists())
                conf.createNewFile();
            Config.load(conf);
            File defaultFavs = new File(getDataFolder(), "default-favourites.yml");
    
            if (!defaultFavs.exists())
                defaultFavs.createNewFile();
            
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
                v.setAI(false);
            });

            Command cmd = Command.createLiteral("bedwars", "bw");

            cmd.literal("help").setExecutor(CommandExecutors.help()).string("args", false).setRecursive(true).setExecutor(CommandExecutors.help());
            Commands.start(cmd.literal("start"));
            Commands.stop(cmd.literal("stop"));

            Commands.kill(cmd.literal("kill"));
            Commands.revive(cmd.literal("revive"));

            Command config = cmd.literal("configuration", "config", "conf");
            Command base = config.literal("base");
            Command generator = config.literal("generator", "gen");

            Commands.baseAdd(base.literal("add"));
            Commands.baseRemove(base.literal("remove"));
            Commands.baseSetSpawn(base.literal("setspawn", "spawn"));
            Commands.baseSetGenerator(base.literal("setgenerator", "generator", "gen"));
            Commands.baseSetBed(base.literal("setbed", "bed"));
            Commands.baseList(base.literal("list", "l"));

            Commands.createDiamondGen(generator.literal("diamond"));
            Commands.createEmeraldGen(generator.literal("emerald", "em"));
            Commands.clearGens(generator.literal("emerald", "em"));

            Commands.breakBed(cmd.literal("breakbed", "cheat", "bedishonest", "abusepowers"));

            cmd.register(this);
                // .attachCommand(new Command("respawn", "revive")
                //     .setExecutor(Commands.revive)
                //     .setHelpMessage("Respawns a spectator, if he has a bed, he is immediatly respawned"))
                // .attachCommand(new Command("breakbed", "eliminateteam")
                //     .setExecutor(Commands.breakBed)
                //     .setHelpMessage("Destoys the bed of a team")
                // )
                // .attachCommand(new Command("eliminate")
                //     .setHelpMessage("Eliminates a player")
                // )
                // .attachCommand(new Command("killteam")
                //     .setHelpMessage("Kills all players of a team")
                // )
                // .attachCommand(new Command("villagertools", "villager", "trader")
                //     .setExecutor((sender, _cmd, alias, args) -> {
                //         if (args.length == 0) {
                //             if (sender instanceof Player) {
                //                 Player p = (Player)sender;
                //                 Traders.instance.give(p);
                //             }
                //         }
                //     })
                //     .setHelpMessage("Gives you tools to manage traders")
                // )
                // .register(this);
            
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
