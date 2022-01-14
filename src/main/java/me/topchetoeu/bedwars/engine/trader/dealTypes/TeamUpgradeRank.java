package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.Team;
import me.topchetoeu.bedwars.engine.trader.upgrades.TeamUpgrade;
import me.topchetoeu.bedwars.engine.trader.upgrades.TeamUpgrades;

public class TeamUpgradeRank {
	private ArrayList<TeamUpgrade> upgrades = new ArrayList<>();
	private ArrayList<ItemStack> items = new ArrayList<>();
	private String name;
	
	public Collection<TeamUpgrade> getUpgrades() {
		return Collections.unmodifiableCollection(upgrades);
	}
	public String getName() {
		return name;
	}
	
	public ItemStack getIcon(TeamUpgrade upgrade) {
		int index = upgrades.indexOf(upgrade);
		if (index < 0) return null;
		else return items.get(index);
	}
	public TeamUpgrade getTeamUpgrade(Team t) {
		for (int i = upgrades.size() - 1; i >= 0; i--) {
			TeamUpgrade upgrade = upgrades.get(i);
			
			if (t.getUpgrades().contains(upgrade)) return upgrade;
		}
		
		return null;
	}
	public TeamUpgrade getNextTeamUpgrade(Team t) {
		if (upgrades.size() == 1)
			return upgrades.get(0);

		for (int i = upgrades.size() - 2; i >= 0; i--) {
			TeamUpgrade upgrade = upgrades.get(i);
			
			if (t.getUpgrades().contains(upgrade)) {
				return upgrades.get(i + 1);
			}
		}
		
		return upgrades.get(0);
	}
	public boolean isTeamAlreadyHighest(Team t) {
		return t.getUpgrades().contains(upgrades.get(upgrades.size() - 1));
	}
	public TeamUpgrade get(String name) {
		return upgrades
			.stream()
			.filter(v -> v.getName().equals(name))
			.findAny()
			.orElse(null);
	}
	
	@SuppressWarnings("unchecked")
	public static TeamUpgradeRank deserialize(Plugin pl, Collection<Object> list) {
		TeamUpgradeRank rank = new TeamUpgradeRank();
		for (Object obj : list) {
			Map<String, Object> map = (Map<String, Object>)obj;
			if (!map.containsKey("name")) throw new RuntimeException("Expected name property in upgrade rank definition.");
			if (!map.containsKey("item")) throw new RuntimeException("Expected item property in upgrade rank definition.");
			
			String name = map.get("name").toString();
			TeamUpgrade upgrade = TeamUpgrades.get(name);
			ItemStack item = Utility.deserializeItemStack((Map<String, Object>)map.get("item"));
			
			rank.items.add(item);
			rank.upgrades.add(upgrade);
		}

		return rank;
	}
}
