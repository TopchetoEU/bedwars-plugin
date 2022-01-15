package me.topchetoeu.bedwars;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;

public class Utility {
    public static void sendTitle(Player p, String title, String subtitle, int fadeIn, int duration, int fadeout) {
        p.sendTitle(title, subtitle, fadeIn, duration, fadeout);
    }
    public static void broadcastTitle(String title, String subtitle, int fadeIn, int duration, int fadeout) {
        Bukkit.getOnlinePlayers().forEach(v -> sendTitle(v, title, subtitle, fadeIn, duration, fadeout));
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
    public static TranslatableComponent getItemName(Material item) {
        return new TranslatableComponent(CraftItemStack.asNMSCopy(new ItemStack(item)).n());
    }
    public static TranslatableComponent getItemName(ItemStack item) {
        return new TranslatableComponent(CraftItemStack.asNMSCopy(new ItemStack(item)).n());
    }
    public static void takeOne(Player p, EquipmentSlot e) {
        ItemStack i = p.getInventory().getItem(e);
        if (i.getAmount() == 0) p.getInventory().setItem(e, i);
        else {
            i.setAmount(i.getAmount() - 1);
            p.getInventory().setItem(e, i);
        }
    }
    
    @SuppressWarnings({ "unchecked", "deprecation" })
    public static ItemStack deserializeItemStack(Map<String, Object> map) {
        String id = ((String)map.get("id")).toUpperCase();
        int amount = map.containsKey("amount") ? (Integer)map.get("amount") : 1;
                
        ItemStack item = new ItemStack(Material.getMaterial(id), amount);
        
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
        
            PotionEffectType effectType = PotionEffectType.getByName(name.toUpperCase());
            
            PotionMeta potionMeta = (PotionMeta)meta;
            potionMeta.addCustomEffect(new PotionEffect(
                effectType,
                duration, level, false
                ), false);
            potionMeta.setColor(effectType.getColor());
            
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

    public static boolean isBed(Block meta) {
        return meta.getBlockData() instanceof Bed;
    }
    public static boolean isWool(Block meta) {
        return meta.getType().getKey().getKey().endsWith("_wool");
    }
    public static boolean isWool(Material meta) {
        return meta.getKey().getKey().endsWith("_wool");
    }
    public static boolean isTool(Material type) {
        return type == Material.SHEARS ||
            type.getKey().getKey().endsWith("_pickaxe") ||
            type.getKey().getKey().endsWith("_shovel") ||
            type.getKey().getKey().endsWith("_axe");
    }
    public static boolean isArmor(Material type) {
        return
            type.getKey().getKey().endsWith("_helmet") ||
            type.getKey().getKey().endsWith("_chestplate") ||
            type.getKey().getKey().endsWith("_leggings") ||
            type.getKey().getKey().endsWith("_boots");
    }
    public static boolean isWeapon(Material type) {
        return
            type.getKey().getKey().endsWith("_sword") ||
            type.getKey().getKey().endsWith("_axe");
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
    public static ChatColor bukkitToBungeeColor(org.bukkit.ChatColor bukkitChatColor) {
        switch (bukkitChatColor) {
            case RED:
                return ChatColor.RED;
            case AQUA:
                return ChatColor.AQUA;
            case BLACK:
                return ChatColor.BLACK;
            case BLUE:
                return ChatColor.BLUE;
            case BOLD:
                return ChatColor.BOLD;
            case DARK_AQUA:
                return ChatColor.DARK_AQUA;
            case DARK_BLUE:
                return ChatColor.DARK_BLUE;
            case DARK_GRAY:
                return ChatColor.DARK_GRAY;
            case DARK_GREEN:
                return ChatColor.DARK_GREEN;
            case DARK_PURPLE:
                return ChatColor.DARK_PURPLE;
            case DARK_RED:
                return ChatColor.DARK_RED;
            case GOLD:
                return ChatColor.GOLD;
            case GRAY:
                return ChatColor.GRAY;
            case GREEN:
                return ChatColor.GREEN;
            case ITALIC:
                return ChatColor.ITALIC;
            case LIGHT_PURPLE:
                return ChatColor.LIGHT_PURPLE;
            case MAGIC:
                return ChatColor.MAGIC;
            case RESET:
                return ChatColor.RESET;
            case STRIKETHROUGH:
                return ChatColor.STRIKETHROUGH;
            case UNDERLINE:
                return ChatColor.UNDERLINE;
            case WHITE:
                return ChatColor.WHITE;
            case YELLOW:
                return ChatColor.YELLOW;
            default:
                return null;
        }
    }
}
