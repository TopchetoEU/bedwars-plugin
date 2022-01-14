package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.Hashtable;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.topchetoeu.bedwars.engine.Game;
import me.topchetoeu.bedwars.engine.Team;
import me.topchetoeu.bedwars.engine.trader.Deal;
import me.topchetoeu.bedwars.engine.trader.upgrades.TeamUpgrade;

public class RankedUpgradeDeal implements Deal {
	private TeamUpgradeRank rank;
	private Hashtable<TeamUpgrade, Price> prices = new Hashtable<>();

	@Override
	public ItemStack getDealItem(Player p) {
		ItemStack _default = new ItemStack(Material.DIAMOND, 1);
		if (!Game.isStarted()) return _default;
		Team t = Game.instance.getTeam(p);
		if (t == null) return _default;

		TeamUpgrade upgrade = rank.getNextTeamUpgrade(t);
		ItemStack icon = rank.getIcon(upgrade);
		if (icon == null) return _default;
		else return icon;
	}


	@Override
	public void commence(Player p) {
		if (!Game.isStarted()) return;
		Team t = Game.instance.getTeam(p);
		if (t == null) return;
		TeamUpgrade upgrade = rank.getNextTeamUpgrade(t);
		if (upgrade == null) return;
		upgrade.addToTeam(t);
		upgrade.updateTeam(t);
		t.sendMessage(p.getName() + "§r purchased " + upgrade.getDisplayName() + "§r!");
	}

	@Override
	public String getDealName(Player p) {
		if (!Game.isStarted()) return "None";
		Team t = Game.instance.getTeam(p);
		if (t == null) return "None";
		
		return rank.getNextTeamUpgrade(t).getDisplayName();
	}

	@Override
	public Material getPriceType(Player p) {
		if (!Game.isStarted()) return Material.DIAMOND;
		Team t = Game.instance.getTeam(p);
		if (t == null) return Material.DIAMOND;
		
		TeamUpgrade upgrade = rank.getNextTeamUpgrade(t);
		Price price = prices.get(upgrade);
		return price.getPriceType();
	}

	@Override
	public int getPrice(Player p) {
		if (!Game.isStarted()) return 128;
		Team t = Game.instance.getTeam(p);
		if (t == null) return 128;
		
		TeamUpgrade upgrade = rank.getNextTeamUpgrade(t);
		Price price = prices.get(upgrade);
		return price.getPrice();
	}

	@Override
	public boolean alreadyBought(Player p) {
		if (!Game.isStarted()) return false;
		Team t = Game.instance.getTeam(p);
		if (t == null) return false;
		return rank.isTeamAlreadyHighest(t);
	}
	
	public RankedUpgradeDeal(TeamUpgradeRank rank, Map<TeamUpgrade, Price> prices) {
		this.prices = new Hashtable<>(prices);
		this.rank = rank;
	}
}
