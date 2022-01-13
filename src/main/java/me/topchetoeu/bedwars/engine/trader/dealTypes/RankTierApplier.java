package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface RankTierApplier {
	ItemStack getIcon();
	void apply(Player p, ItemStack[] inv, Rank rank);
	
	public static RankTierApplier deserialize(Map<String, Object> map) {
		if (!map.containsKey("type")) throw new RuntimeException("A type property was expected for a rank tier item.");
		
		switch (map.get("type").toString()) {
		case "item":
			return RankTierItemApplier.deserialize(map);
		case "upgrade":
			return null;
		default:
			throw new RuntimeException("Unrecoginsed rank tier item type '" + map.get("type") + "'.");
		}
	}
}
