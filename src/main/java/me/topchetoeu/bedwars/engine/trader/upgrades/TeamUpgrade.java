package me.topchetoeu.bedwars.engine.trader.upgrades;

import org.bukkit.inventory.ItemStack;

import me.topchetoeu.bedwars.engine.Team;

public interface TeamUpgrade {
    void addToTeam(Team team);
    void updateTeam(Team team);
    void upgradeItem(ItemStack item);
    String getName();
    String getDisplayName();
}
