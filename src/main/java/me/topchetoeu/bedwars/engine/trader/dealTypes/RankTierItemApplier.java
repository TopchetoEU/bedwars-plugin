package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.topchetoeu.bedwars.InventoryUtility;
import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.Game;
import me.topchetoeu.bedwars.engine.Team;
import me.topchetoeu.bedwars.engine.trader.dealTypes.RankedDealType.ApplyAction;

public class RankTierItemApplier implements RankTierApplier {
	private ItemStack item;
	private ApplyAction apply;
	
	public ItemStack getIcon() {
		return item;
	}
	public ItemStack getItem() {
		return item;
	}
	public ApplyAction getApplyAction() {
		return apply;
	}
	public void apply(Player p, ItemStack[] items, Rank rank) {
		ItemStack item = this.item.clone();
		if (Game.isStarted()) {
			Team team = Game.instance.getTeam(p);
			if (team != null) team.teamifyItem(item, true, true);
		}
		ItemStack remaining = null;
		switch (apply) {
		case GIVE:
			remaining = InventoryUtility.giveItem(items, item);
			break;
		case REPLACEPREV:
			rank.clear(items);
			remaining = InventoryUtility.giveItem(items, item);
			break;
		case SLOT_HELMET:
			p.getInventory().setHelmet(item);
			break;
		case SLOT_CHESTPLATE:
			p.getInventory().setChestplate(item);
			break;
		case SLOT_LEGGINGS:
			p.getInventory().setLeggings(item);
			break;
		case SLOT_BOOTS:
			p.getInventory().setBoots(item);
			break;
		}
		
		if (remaining != null) p.getWorld().dropItemNaturally(p.getLocation(), remaining);
	}
	
	@SuppressWarnings("unchecked")
	public static RankTierItemApplier deserialize(Map<String, Object> map) {
		ItemStack item = Utility.deserializeItemStack((Map<String, Object>)map.get("item"));
		ApplyAction apply = ApplyAction.REPLACEPREV;
		if (map.containsKey("apply")) apply = ApplyAction.valueOf(((String)map.get("apply")).toUpperCase());
		
		return new RankTierItemApplier(item, apply);
	}
	
	public RankTierItemApplier(ItemStack item, ApplyAction apply) {
		this.item = item;
		this.apply = apply;
	}
}