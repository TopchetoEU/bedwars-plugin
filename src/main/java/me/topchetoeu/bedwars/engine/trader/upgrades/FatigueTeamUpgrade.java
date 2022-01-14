package me.topchetoeu.bedwars.engine.trader.upgrades;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.topchetoeu.bedwars.engine.BedwarsPlayer;
import me.topchetoeu.bedwars.engine.Game;
import me.topchetoeu.bedwars.engine.Team;

public class FatigueTeamUpgrade implements TeamUpgrade {

	@Override
	public void addToTeam(Team team) {
		team.addUpgrade(this);
	}

	@Override
	public void updateTeam(Team team) {
	}

	@Override
	public void upgradeItem(ItemStack item) {
	}

	@Override
	public String getName() {
		return "fatigue";
	}
	@Override
	public String getDisplayName() {
		return "Mining Fatigue";
	}

	public FatigueTeamUpgrade(Plugin pl) {
		Bukkit.getScheduler().runTaskTimer(pl, () -> {
			if (!Game.isStarted()) return;
			
			for (BedwarsPlayer bwp : Game.instance.getPlayers()) {
				if (!bwp.isOnline()) continue;
				Player p = bwp.getOnlinePlayer();
				
				for (Team t : Game.instance.getTeams()) {
					if (!t.hasUpgrade(this)) continue;
					if (t.hasPlayer(bwp)) continue;
					if (p.getLocation().distance(t.getTeamColor().getBedLocation()) < 7.5) {
						p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 200, 1, true, false));
						t.removeUpgrade(getClass());
					}
				}
			}
		}, 0, 20);
	}

	public static void init(Plugin pl) {
		TeamUpgrades.register(new FatigueTeamUpgrade(pl));
	}
}
