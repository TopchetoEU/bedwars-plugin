package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.Hashtable;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.topchetoeu.bedwars.engine.Game;
import me.topchetoeu.bedwars.engine.trader.Deal;

public class EnforcedRankedDeal implements Deal {
	private Rank soldRank;
	private Hashtable<RankTier, Price> prices;

	public Rank getRank() {
		return soldRank;
	}
	
	@Override
	public ItemStack getDealItem(Player p) {
		ItemStack icon = soldRank.getNextTier(p).getIcon();
		if (Game.isStarted()) Game.instance.teamifyItem(p, icon, true, true);
		return icon;
	}
	@Override
	public String getDealName(Player p) {
		return soldRank.getNextTier(p).getDisplayName();
	}
	@Override
	public Material getPriceType(Player p) {
		return prices.get(soldRank.getNextTier(p)).getPriceType();
	}
	@Override
	public int getPrice(Player p) {
		return prices.get(soldRank.getNextTier(p)).getPrice();
	}
	@Override
	public boolean alreadyBought(Player p) {
		return soldRank.getNextTier(p) == soldRank.getPlayerTier(p);
	}
	@Override
	public void commence(Player p) {
		p.sendMessage(String.format("§rYou just purchased %s.", getDealName(p)));
		soldRank.increasePlayerTier(p, soldRank.getNextTier(p));
	}
	
	public EnforcedRankedDeal(Rank rank, Hashtable<RankTier, Price> prices) {
		this.soldRank = rank;
		this.prices = prices;
	}
}
