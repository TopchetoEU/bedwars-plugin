package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.trader.Deal;
import me.topchetoeu.bedwars.engine.trader.DealType;
import me.topchetoeu.bedwars.engine.trader.DealTypes;

public class RankedDealType implements DealType {
	private static Hashtable<String, Rank> definedRanks = new Hashtable<>();
	
	@Override
	public Deal parse(Map<String, Object> map) {
		Material priceType = Material.getMaterial(((String)map.get("priceType")).toUpperCase());
		int price = (Integer)map.get("price");
		Rank rank = definedRanks.get((String)map.get("rank"));
		RankTier tier = rank.getTier((String)map.get("tier"));
		
		return new RankedDeal(rank, tier, priceType, price);
	}

	@Override
	public String getId() { return "tier"; }
	
	public interface LoseAction {
		RankTier getTier(RankTier curr);
	}
	
	public enum InventoryBehaviour {
		FREE,
		NODROP,
		NOENDER,
		STUCK,
	}
	
	public enum ApplyAction {
		GIVE,
		REPLACEPREV,
		SLOT_HELMET,
		SLOT_CHESTPLATE,
		SLOT_LEGGINGS,
		SLOT_BOOTS,
	}
	
	@SuppressWarnings("unchecked")
	public static void init(Plugin pl, Configuration config) {
		DealTypes.register(new RankedDealType());
		Map<String, Object> mapConf = Utility.mapifyConfig(config);
		Map<String, Object> ranks = (Map<String, Object>)mapConf.get("ranks");
		
		if (ranks != null) {
			for (String key : ranks.keySet()) {
				Map<String, Object> map = (Map<String, Object>)ranks.get(key);
				
				definedRanks.put(key, Rank.deserialize(pl, map));
			}
		}
	}
	
	public static void resetPlayerTiers() {
		for (String key: definedRanks.keySet()) {
			definedRanks.get(key).resetPlayerTiers();
		}
	}
	
	public static void refreshPlayer(Player p) {
		for (Rank rank : definedRanks.values()) {
			rank.refreshInv(p);
		}
	}
	
	public static Map<String, Rank> getDefinedRanks() {
		return Collections.unmodifiableMap(definedRanks);
	}
}
