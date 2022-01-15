package me.topchetoeu.bedwars.engine.trader.dealTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import me.topchetoeu.bedwars.engine.Game;
import me.topchetoeu.bedwars.engine.trader.dealTypes.RankedDealType.InventoryBehaviour;
import me.topchetoeu.bedwars.engine.trader.dealTypes.RankedDealType.LoseAction;

public class Rank implements Listener {
    private RankTier defaultTier;
    private LoseAction onLose;
    private InventoryBehaviour inventory;
    private Map<String, RankTier> nameToTierMap;
    private ArrayList<RankTier> tiers;
    private Hashtable<UUID, RankTier> currentTier = new Hashtable<>();

    public void resetPlayerTiers() {
        for (UUID key : new HashSet<>(currentTier.keySet())) {
            if (defaultTier == null) currentTier.remove(key);
            else currentTier.put(key, defaultTier);
        }
    }
    
    public boolean hasDefaultTier() {
        return defaultTier != null;
    }
    public RankTier getDefaultTier() {
        return defaultTier;
    }
    public LoseAction getOnLoseAction() {
        return onLose;
    }
    public InventoryBehaviour getInventoryBehaviour() {
        return inventory;
    }
    
    public Collection<RankTier> getTiers() {
        return nameToTierMap.values();
    }
    public Set<String> getTierNames() {
        return nameToTierMap.keySet();
    }
    public RankTier getTier(String name) {
        return nameToTierMap.get(name);
    }

    public boolean containsItem(ItemStack item) {
        if (item == null) return false;
        return tiers.stream()
            .flatMap(v -> v.getItems()
                .stream()
                .filter(_v -> _v instanceof RankTierItemApplier)
                .map(_v -> (RankTierItemApplier)_v)
                .map(i -> i.getItem())
            )
            .anyMatch(v -> v.getType().equals(item.getType()));
    }
    
    public boolean playerHasOrAboveTier(OfflinePlayer player, RankTier tier) {
        return tiers.indexOf(tier) <= tiers.indexOf(getPlayerTier(player));
    }
    public RankTier getPlayerTier(OfflinePlayer player) {
        return currentTier.get(player.getUniqueId());
    }
    public RankTier getNextTier(OfflinePlayer player) {
        int i = tiers.indexOf(getPlayerTier(player)) + 1;
        if (i >= tiers.size()) i--;
        return tiers.get(i);
    }
    
    public void increasePlayerTier(OfflinePlayer player, RankTier target) {
        int i = tiers.indexOf(getPlayerTier(player));
        int targetI = tiers.indexOf(target);
        
        if (targetI <= i) return;
        
        if (player.isOnline()) {
            Player p = player.getPlayer();
            ItemStack[] inv = p.getInventory().getContents();

            i++;
            for (; i <= targetI; i++) {
                tiers.get(i).apply(inv, p, this);
            }
            
            p.getInventory().setContents(inv);
        }
        
        currentTier.put(player.getUniqueId(), target);
    }
    
    public void clear(ItemStack[] inv) {
        for (int i = 0; i < inv.length; i++) {
            if (inv[i] != null && containsItem(inv[i])) {
                inv[i] = null;
            }
        }
    }
    
    public void onDeath(Player p) {
        RankTier tier = getPlayerTier(p);
        
        if (tier != null) {
            currentTier.put(p.getUniqueId(), onLose.getTier(tier));
        }
        refreshInv(p);
    }
    @EventHandler
    private void onRespawn(PlayerRespawnEvent e) {
        if (Game.instance.isPlaying(e.getPlayer())) {
        }
    }
    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        if (Game.inGame(e.getPlayer())) {
            if (defaultTier != null) increasePlayerTier(e.getPlayer(), defaultTier);
            refreshInv(e.getPlayer());
        }
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent e) {
        if (containsItem(e.getItemDrop().getItemStack())) {
            if (inventory != InventoryBehaviour.FREE) e.setCancelled(true);
        }
    }
    @EventHandler
    private void onInventory(InventoryClickEvent e) {
        if (Game.isStarted() && Game.instance.isPlaying((Player)e.getWhoClicked())) {
            if (containsItem(e.getCurrentItem()) || containsItem(e.getCursor())) {
                switch (inventory) {
                case STUCK:
                    e.setCancelled(true);
                    return;
                case NOENDER:
                    if ((e.getClickedInventory() instanceof PlayerInventory)) {
                        if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) e.setCancelled(true);
                    }
                    else e.setCancelled(true);
                    
                case NODROP:
                    switch (e.getAction()) {
                    case DROP_ALL_CURSOR:
                    case DROP_ALL_SLOT:
                    case DROP_ONE_CURSOR:
                    case DROP_ONE_SLOT:
                        e.setCancelled(true);
                    default:
                        break;
                    }
                    return;
                case FREE:
                    return;
                }
            }
        }
    }
    
    public void refreshInv(Player p) {
        ItemStack[] inv = p.getInventory().getContents();
        
        clear(inv);
        
        RankTier tier = getPlayerTier(p);
        
        if (tier != null) {
            int max = tiers.indexOf(tier);

            for (int i = 0; i <= max; i++) {
                tiers.get(i).apply(inv, p, this);
            }
        }

        p.getInventory().setContents(inv);
        p.updateInventory();
    }
    
        
    @SuppressWarnings("unchecked")
    public static Rank deserialize(Plugin pl, Map<String, Object> map) {        
        List<RankTier> tiers = ((Map<String, Object>)map.get("tiers"))
            .entrySet()
            .stream()
            .map(v -> RankTier.deserialize(v.getKey(), (Map<String, Object>)v.getValue()))
            .collect(Collectors.toList());
        
        RankTier defaultTier = map.containsKey("default") ? tiers
            .stream()
            .filter(v -> v.getName().equals(map.get("default")))
            .findFirst()
            .orElseThrow() : null;
        InventoryBehaviour inventory = InventoryBehaviour.valueOf(((String)map.get("inventory")).toUpperCase());
        
        return new Rank(pl, tiers, inventory, defaultTier, (String)map.get("onLose"));
    }
    
    public Rank(Plugin pl, Collection<RankTier> tiers, InventoryBehaviour inventory, RankTier defaultTier, String loseAction) {
        this.tiers = new ArrayList<>(tiers);
        this.nameToTierMap = this.tiers.stream().collect(Collectors.toMap(v -> v.getName(), v -> v));
        this.inventory = inventory;
        this.defaultTier = defaultTier;
        
        switch (loseAction) {
        case "keep":
            onLose = currTier -> currTier;
            break;
        case "lower":
            onLose = currTier -> {
                int i = this.tiers.indexOf(currTier) - 1;
                if (i < 0) i = 0;
                Bukkit.getServer().broadcastMessage(Integer.toString(i));
                
                return this.tiers.get(i);
            };
            break;
        case "lose":
            onLose = currTier -> null;
            break;
        default:
            if (loseAction.startsWith("tier_")) {
                String tierName = loseAction.substring(5);
                RankTier tier = nameToTierMap.get(tierName);
                onLose = currTier -> tier;
            }
            else throw new RuntimeException(String.format("The lose action %s was not recognised.", loseAction));
            break;
        }
        
        if (defaultTier != null) {
            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                increasePlayerTier(p, defaultTier);
            }
        }
        
        Bukkit.getPluginManager().registerEvents(this, pl);
    }
}
