package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.Hashtable;
import java.util.Map;

import me.topchetoeu.bedwars.engine.trader.Deal;
import me.topchetoeu.bedwars.engine.trader.DealType;
import me.topchetoeu.bedwars.engine.trader.DealTypes;
import me.topchetoeu.bedwars.engine.trader.upgrades.TeamUpgrade;

public class RankedUpgradeDealType implements DealType {

    @Override
    @SuppressWarnings("unchecked")
    public Deal parse(Map<String, Object> map) {
        Hashtable<TeamUpgrade, Price> prices = new Hashtable<>();
        TeamUpgradeRank rank = TeamUpgradeRanks.get((String)map.get("rank"));
        
        Map<String, Object> pricesMap = (Map<String, Object>)map.get("prices");
        
        for (String name : pricesMap.keySet()) {
            TeamUpgrade tier = rank.get(name);
            Price price = Price.deserialize((Map<String, Object>)pricesMap.get(name));
            
            prices.put(tier, price);
        }
        
        return new RankedUpgradeDeal(rank, prices);
    }

    @Override
    public String getId() {
        return "upgrade";
    }

    public static void init() {
        DealTypes.register(new RankedUpgradeDealType());
    }

}
