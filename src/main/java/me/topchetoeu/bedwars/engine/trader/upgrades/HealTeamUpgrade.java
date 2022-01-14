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

public class HealTeamUpgrade implements TeamUpgrade {

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
		return "heal";
	}
	@Override
	public String getDisplayName() {
		return "Healing";
	}

	public HealTeamUpgrade(Plugin pl) {
		Bukkit.getScheduler().runTaskTimer(pl, () -> {
			if (!Game.isStarted()) return;
			
			for (Team t : Game.instance.getTeams()) {
				if (!t.hasUpgrade(this)) continue;
				for (BedwarsPlayer bwp : t.getPlayers()) {
					if (bwp.isOnline()) {
						Player p = bwp.getOnlinePlayer();
						
						if (p.getLocation().distance(t.getTeamColor().getSpawnLocation()) < 20)
							p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 1, true, false));
					}
				}
			}
		}, 0, 20);
	}

	public static void init(Plugin pl) {
		TeamUpgrades.register(new HealTeamUpgrade(pl));
	}
}
