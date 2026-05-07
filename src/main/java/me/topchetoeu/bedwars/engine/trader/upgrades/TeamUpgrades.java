package me.topchetoeu.bedwars.engine.trader.upgrades;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;

public class TeamUpgrades {
    private static Hashtable<String, TeamUpgrade> upgrades = new Hashtable<>();
    
    public static TeamUpgrade get(String name) {
        return upgrades.get(name);
    }
    public static boolean register(TeamUpgrade upgrade) {
        if (upgrades.containsKey(upgrade.getName())) return false;
        
        upgrades.put(upgrade.getName(), upgrade);
        return true;
    }
    public static Collection<TeamUpgrade> getAll() {
        return Collections.unmodifiableCollection(upgrades.values());
    }
}
