package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.Hashtable;
import java.util.Map;

import me.topchetoeu.bedwars.engine.trader.Deal;
import me.topchetoeu.bedwars.engine.trader.DealType;
import me.topchetoeu.bedwars.engine.trader.DealTypes;

public class EnforcedRankedDealType implements DealType {
	@SuppressWarnings("unchecked")
	@Override
	public Deal parse(Map<String, Object> map) {
		Hashtable<RankTier, Price> prices = new Hashtable<>();
		Rank rank = RankedDealType.getDefinedRanks().get((String)map.get("rank"));
		
		Map<String, Map<String, Object>> pricesMap = (Map<String, Map<String, Object>>)map.get("prices");
		
		for (String name : pricesMap.keySet()) {
			RankTier tier = rank.getTier(name);
			Price price = Price.deserialize(pricesMap.get(name));
			
			prices.put(tier, price);
		}
		
		return new EnforcedRankedDeal(rank, prices);
	}

	@Override
	public String getId() { return "tier_enforced"; }
	
	public static void init() {
		DealTypes.register(new EnforcedRankedDealType());
	}
}
