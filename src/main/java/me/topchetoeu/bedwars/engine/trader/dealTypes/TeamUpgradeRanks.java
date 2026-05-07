package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.Plugin;

import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.trader.DealTypes;

public class TeamUpgradeRanks {
    private static HashMap<String, TeamUpgradeRank> ranks = new HashMap<>();

    public static Collection<TeamUpgradeRank> getAll() {
        return Collections.unmodifiableCollection(ranks.values());
    }
    public static TeamUpgradeRank get(String name) {
        return ranks.get(name);
    }

    @SuppressWarnings("unchecked")
    public static void init(Plugin pl, Configuration config) {
        DealTypes.register(new RankedDealType());
        Map<String, Object> mapConf = Utility.mapifyConfig(config);
        Map<String, Collection<Object>> rawRanks = (Map<String, Collection<Object>>)mapConf.get("upgrades");
        
        ranks.clear();
        
        if (rawRanks != null) {
            for (String key : rawRanks.keySet()) {
                Collection<Object> map = (Collection<Object>)rawRanks.get(key);
                
                ranks.put(key, TeamUpgradeRank.deserialize(pl, map));
            }
        }
    }
}
