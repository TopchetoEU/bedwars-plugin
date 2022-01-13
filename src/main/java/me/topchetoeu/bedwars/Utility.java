package me.topchetoeu.bedwars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

public class Utility {

	private static IChatBaseComponent getText(String text) {
		return new ChatComponentText(text);
	}
	
	public static void sendTitle(Player p, String title, String subtitle, int fadein, int duration, int fadeout) {
		EntityPlayer handle = ((CraftPlayer)p).getHandle();
		
		handle.playerConnection.sendPacket(new PacketPlayOutTitle(
			EnumTitleAction.TIMES, null, fadein, duration, fadeout
		));
		if (subtitle == null) {
			subtitle = "";
		}
		if (title == null) {
			title = "";
		}
		handle.playerConnection.sendPacket(new PacketPlayOutTitle(
			EnumTitleAction.SUBTITLE, getText(subtitle)
		));
		handle.playerConnection.sendPacket(new PacketPlayOutTitle(
			EnumTitleAction.TITLE, getText(title)
		));
	}
	public static void broadcastTitle(String title, String subtitle, int fadein, int duration, int fadeout) {
		Bukkit.getOnlinePlayers().forEach(v -> sendTitle(v, title, subtitle, fadein, duration, fadeout));
	}
	public static boolean isParsable(String val) {
		try {
			Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return false;
		}
		
		return true;
	}
	
	public static ItemStack namedItem(ItemStack i, String name) {
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(name);
		i.setItemMeta(meta);
		return i;
	}
	public static ItemStack copyNamedItem(ItemStack i, String name) {
		i = new ItemStack(i);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(name);
		i.setItemMeta(meta);
		return i;
	}
	public static String getItemName(Material item) {
	    net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(new ItemStack(item));
	    return nmsStack.getItem().a(nmsStack);
	}
	public static String getItemName(ItemStack item) {
		if (item.getItemMeta().hasDisplayName()) return item.getItemMeta().getDisplayName();
	    net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
	    return nmsStack.getItem().a(nmsStack);
	}
	
	@SuppressWarnings("unchecked")
	public static ItemStack deserializeItemStack(Map<String, Object> map) {
		String id = ((String)map.get("id")).toUpperCase();
		int amount = map.containsKey("amount") ? (Integer)map.get("amount") : 1;
		short damage = (short)(map.containsKey("damage") ? (Integer)map.get("damage") : 0);
				
		ItemStack item = new ItemStack(Material.getMaterial(id), amount, damage);
		
		ItemMeta meta = item.getItemMeta();
		if (map.containsKey("displayName")) meta.setDisplayName((String)map.get("displayName"));
		
		if (map.containsKey("lore")) meta.setLore((ArrayList<String>)map.get("lore"));
		
		if (map.containsKey("enchants")) {
			for(Entry<String, Integer> entry : ((Map<String, Integer>)map.get("enchants")).entrySet()) {
				Enchantment e = Enchantment.getByName(entry.getKey().toUpperCase());
				
				meta.addEnchant(e, entry.getValue(), true);
			}
		}
		if (map.containsKey("potion")) {
			Map<String, Object> potionMap = (Map<String, Object>)map.get("potion");
			String name = (String)potionMap.get("id");
			int level = (Integer)potionMap.get("level");
			int duration = (Integer)potionMap.get("duration");
			boolean splash = potionMap.containsKey("splash") && (boolean)potionMap.get("splash");
		
			PotionEffectType effectType = PotionEffectType.getByName(name.toUpperCase());
			
			PotionMeta potionMeta = (PotionMeta)meta;
			potionMeta.addCustomEffect(new PotionEffect(
				effectType,
				duration, level, false
			), false);
			
			Potion pot = new Potion(PotionType.getByEffect(effectType), 1);
			if (splash) pot = pot.splash();
			pot.apply(item);
			
			meta = potionMeta;
		}
		
		item.setItemMeta(meta);
		
		return item;
	}
	public static Map<String, Object> mapifyConfig(ConfigurationSection config) {
		Map<String, Object> map = config.getValues(false);
		for (String key : map.keySet()) {
			Object val = map.get(key);
			if (val instanceof ConfigurationSection) {
				map.put(key, mapifyConfig((ConfigurationSection)val));
			}
		}
		
		return map;
	}

	public static Optional<PotionEffect> getPotionEffect(Collection<PotionEffect> p, PotionEffectType type) {
		return p.stream().filter(v -> v.getType().equals(type)).findFirst();
	}
	public static Optional<PotionEffect> getPotionEffect(LivingEntity p, PotionEffectType type) {
		return getPotionEffect(p.getActivePotionEffects(), type);
	}
	
	public static void applyPotionEffect(LivingEntity p, PotionEffect e) {
		PotionEffect eff = getPotionEffect(p, e.getType()).orElse(null);
		
		if (eff == null) p.addPotionEffect(e);
		else {
			p.removePotionEffect(e.getType());
			p.addPotionEffect(e);
		}
	}
	
	@Deprecated(since = "Don't forget to remove these")
	public static void debugMsg(Object obj) {
		if (obj == null) obj = "null";
		Bukkit.getServer().broadcastMessage(obj.toString());
	}
	@Deprecated(since = "Don't forget to remove these")
	public static void debugMsg(CommandSender p, Object obj) {
		if (obj == null) obj = "null";
		p.sendMessage(obj.toString());
	}
}
