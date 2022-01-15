package me.topchetoeu.bedwars.engine.trader.upgrades;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.Team;

public class ProtectionTeamUpgrade implements TeamUpgrade {
    private int level;
    
    @Override
    public void addToTeam(Team team) {
        team.removeUpgrade(getClass());
        team.addUpgrade(this);
        updateTeam(team);
    }

    @Override
    public void updateTeam(Team team) {
        team.getPlayers().forEach(ofp -> {
            if (ofp.isOnline()) {
                Player p = ofp.getOnlinePlayer();
                
                ItemStack[] inv = p.getInventory().getContents();
                ItemStack[] armorInv = p.getInventory().getArmorContents();
                
                for (ItemStack i : inv) {
                    upgradeItem(i);
                }
                for (ItemStack i : armorInv) {
                    upgradeItem(i);
                }
                
                p.getInventory().setContents(inv);
                p.getInventory().setArmorContents(armorInv);
                p.updateInventory();
            }
        });
    }

    @Override
    public String getName() {
        return "protection-" + level;
    }
    @Override
    public String getDisplayName() {
        return "Protection " + level;
    }

    @Override
    public void upgradeItem(ItemStack item) {
        if (item == null) return;
        if (Utility.isTool(item.getType())) {
            item.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
        }
    }
    
    public ProtectionTeamUpgrade(int level) {
        this.level = level;
    }
    
    public static void init() {
        TeamUpgrades.register(new ProtectionTeamUpgrade(1));
        TeamUpgrades.register(new ProtectionTeamUpgrade(2));
        TeamUpgrades.register(new ProtectionTeamUpgrade(3));
        TeamUpgrades.register(new ProtectionTeamUpgrade(4));
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
