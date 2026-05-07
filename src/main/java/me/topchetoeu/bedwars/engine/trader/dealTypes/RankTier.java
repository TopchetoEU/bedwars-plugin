package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.topchetoeu.bedwars.Utility;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class RankTier {
    private String name;
    private ItemStack icon;
    private Collection<RankTierItemApplier> items;
    
    public String getName() {
        return name;
    }
    public BaseComponent[] getDisplayName() {
        return new ComponentBuilder().append(Utility.getItemName(icon)).reset().create();
    }
     public ItemStack getIcon() {
        return icon;
    }
    public Collection<RankTierItemApplier> getItems() {
        return items;
    }
    
    public void apply(ItemStack[] inv, Player p, Rank rank) {
        for (RankTierItemApplier item : items) {
            item.apply(p, inv, rank);
        }
    }
    
    @SuppressWarnings("unchecked")
    public static RankTier deserialize(String name, Map<String, Object> map) {
        Collection<RankTierItemApplier> items = ((Collection<Map<String, Object>>)map.get("items"))
            .stream()
            .map(v -> RankTierItemApplier.deserialize(v))
            .collect(Collectors.toList());
        
        ItemStack icon = null;
        if (map.containsKey("icon")) icon = Utility.deserializeItemStack((Map<String, Object>)map.get("icon"));
        else icon = items.stream().findFirst().orElseThrow().getIcon();
        
        return new RankTier(name, icon, items);
    }
    
    public RankTier(String name, ItemStack icon, Collection<RankTierItemApplier> items) {
        this.name = name;
        this.icon = icon;
        this.items = items;
    }
}
