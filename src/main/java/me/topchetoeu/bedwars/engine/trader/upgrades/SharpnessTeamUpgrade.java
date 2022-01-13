package me.topchetoeu.bedwars.engine.trader.upgrades;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.topchetoeu.bedwars.engine.Team;
import net.minecraft.server.v1_8_R3.ItemAxe;
import net.minecraft.server.v1_8_R3.ItemSword;

public class SharpnessTeamUpgrade implements TeamUpgrade {
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
		return "sharpness-" + level;
	}
	@Override
	public String getDisplayName() {
		return "Sharpness " + level;
	}

	@Override
	public void upgradeItem(ItemStack item) {
		if (item == null) return;
		net.minecraft.server.v1_8_R3.Item nmsItem = CraftItemStack.asNMSCopy(item).getItem();
		if (nmsItem instanceof ItemSword || nmsItem instanceof ItemAxe) {
			item.addEnchantment(Enchantment.DAMAGE_ALL, level);
		}
	}
	
	public SharpnessTeamUpgrade(int level) {
		this.level = level;
	}
	
	public static void init() {
		TeamUpgrades.register(new SharpnessTeamUpgrade(1));
		TeamUpgrades.register(new SharpnessTeamUpgrade(2));
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getName();
	}
}
