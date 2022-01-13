package me.topchetoeu.bedwars.engine.trader.dealTypes;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.Game;
import me.topchetoeu.bedwars.engine.trader.Deal;

public class RankedDeal implements Deal {
	private Rank soldRank;
	private RankTier soldTier;
	
	private Material priceType;
	private int price;

	public Rank getRank() {
		return soldRank;
	}
	public RankTier getTier() {
		return soldTier;
	}
	
	@Override
	public ItemStack getDealItem(Player p) {
		ItemStack icon = soldTier.getIcon();
		if (Game.isStarted()) Game.instance.teamifyItem(p, icon, true, true);
		return icon;
	}
	@Override
	public String getDealName(Player p) {
		return Utility.getItemName(soldTier.getIcon());
	}
	@Override
	public Material getPriceType(Player p) {
		return priceType;
	}
	@Override
	public int getPrice(Player p) {
		return price;
	}
	@Override
	public boolean alreadyBought(Player p) {
		return soldRank.playerHasOrAboveTier(p, soldTier);
	}
	@Override
	public void commence(Player p) {
		p.sendMessage(String.format("§rYou just purchased %s.", getDealName(p)));
		soldRank.increasePlayerTier(p, soldTier);
	}
	
	public RankedDeal(Rank rank, RankTier tier, Material priceType, int price) {
		soldRank = rank;
		soldTier = tier;
		this.priceType = priceType;
		this.price = price;
	}
}
