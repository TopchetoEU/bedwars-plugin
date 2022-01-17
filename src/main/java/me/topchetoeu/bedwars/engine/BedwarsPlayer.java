package me.topchetoeu.bedwars.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.datafixers.util.Pair;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;

import me.topchetoeu.bedwars.Utility;
import me.topchetoeu.bedwars.engine.trader.dealTypes.RankedDealType;
import me.topchetoeu.bedwars.messaging.MessageUtility;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.world.entity.EnumItemSlot;

import org.bukkit.entity.Explosive;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import me.topchetoeu.bedwars.InventoryUtility;
import me.topchetoeu.bedwars.Main;

public class BedwarsPlayer implements Listener, AutoCloseable {
    private OfflinePlayer player;
    private Team team;
    private boolean dead = true;
    private boolean deathPending = false;
    private boolean revivalPending = false;
    private boolean spectator = false;
    private Map<String, String> deathMessages;
    private float revivalTimer = 0;
    private int offenceTimer = 0;
    private BukkitTask offenceTask = null;
    private BukkitTask invisTask = null;
    private BukkitTask reviveTask = null;
    
    private int kills;
    private int finalKills;
    private int beds;
    private int deaths;
    private boolean invisible = false;    
    
    private OfflinePlayer offender = null;
    
    private void updateInvisibility(Player p) {
        net.minecraft.world.item.ItemStack helmetItem = CraftItemStack.asNMSCopy(getOnlinePlayer().getInventory().getHelmet());
        net.minecraft.world.item.ItemStack chestplateItem = CraftItemStack.asNMSCopy(getOnlinePlayer().getInventory().getChestplate());
        net.minecraft.world.item.ItemStack leggingsItem = CraftItemStack.asNMSCopy(getOnlinePlayer().getInventory().getLeggings());
        net.minecraft.world.item.ItemStack bootsItem = CraftItemStack.asNMSCopy(getOnlinePlayer().getInventory().getBoots());

        if (invisible) {
            helmetItem = chestplateItem = leggingsItem = bootsItem = null;
        }


        List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> items = new ArrayList<>();

        items.add(new Pair<EnumItemSlot,net.minecraft.world.item.ItemStack>(EnumItemSlot.c, helmetItem));
        items.add(new Pair<EnumItemSlot,net.minecraft.world.item.ItemStack>(EnumItemSlot.d, chestplateItem));
        items.add(new Pair<EnumItemSlot,net.minecraft.world.item.ItemStack>(EnumItemSlot.e, leggingsItem));
        items.add(new Pair<EnumItemSlot,net.minecraft.world.item.ItemStack>(EnumItemSlot.f, bootsItem));

        int id = getOnlinePlayer().getEntityId();
        
        PacketPlayOutEntityEquipment packet = new PacketPlayOutEntityEquipment(id, items);
        
        ((CraftPlayer)p).getHandle().b.a(packet);
    }
    
    private void updateInvisiblity() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (Game.inGame(player) && !team.hasPlayer(player)) {
                updateInvisibility(player);
            }
        }
    }
    private void removeInvis() {
        invisible = false;
        updateInvisiblity();
        if (isOnline()) getOnlinePlayer().removePotionEffect(PotionEffectType.INVISIBILITY);
        if (invisTask != null) {
            invisTask.cancel();
            invisTask = null;
        }
    }
    
    public OfflinePlayer getPlayer() {
        return player;
    }
    public Player getOnlinePlayer() {
        if (player.isOnline()) return player.getPlayer();
        else return null;
    }
    public Team getTeam() {
        return team;
    }
    
    public boolean isDead() {
        return dead;
    }
    public boolean isSpectator() {
        return spectator;
    }
    public boolean isOnline() {
        return player.isOnline();
    }
    
    public int getRegularKills() {
        return kills;
    }
    public int getFinalKills() {
        return finalKills;
    }
    public int getKills() {
        return kills + finalKills;
    }
    public int getBeds() {
        return beds;
    }
    public int getDeaths() {
        return deaths;
    }
    
    
    public float getRevivalTimer() {
        return revivalTimer;
    }
    public float increaseRevivalTimer(float amount) {
        return revivalTimer += amount;
    }
    public void resetRevivalTimer() {
        revivalTimer = 0;
    }
    public void kill(String deathMsg) {
        if (dead) return;
        if (player.isOnline()) {
            Bukkit.getServer().broadcastMessage(deathMsg);
            BedwarsPlayer bwOffender = null;
            if (offender != null && Game.isStarted() && Game.instance.isPlaying(offender)) {
                bwOffender = Game.instance.getPlayer(offender);
                if (offender.isOnline()) {
                    Player p = offender.getPlayer();
                    
                    ItemStack[] inv = p.getInventory().getContents();
        
                    getOnlinePlayer().getInventory().forEach(i -> {
                        if (i != null) {
                            if (i.getType() == Material.IRON_INGOT ||
                                    i.getType() == Material.GOLD_INGOT ||
                                    i.getType() == Material.EMERALD ||
                                    i.getType() == Material.DIAMOND) {
                                InventoryUtility.giveItem(inv, i);
                            }
                        }
                    });
                    
                    p.getInventory().setContents(inv);
                    p.updateInventory();
                }
            }
            
            for(PotionEffect effect : getOnlinePlayer().getActivePotionEffects())
            {
                getOnlinePlayer().removePotionEffect(effect.getType());
            }
            
            if (team.hasBed()) {
                dead = true;
                removeInvis();
                getOnlinePlayer().setGameMode(GameMode.SPECTATOR);
                getOnlinePlayer().setHealth(20);
                RankedDealType.getDefinedRanks().values().forEach((v) -> {
                    v.onDeath(getOnlinePlayer());
                });
                    
                revivalTimer = 5;
                
                reviveTask = Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
                    if (!player.isOnline()) {
                        dead = false;
                        revivalTimer = 0;
                        deathPending = true;
                        reviveTask.cancel();
                        reviveTask = null;
                    }
                    Utility.sendTitle(player.getPlayer(),
                        MessageUtility.parser("player.died.title").parse(),
                        MessageUtility.parser("player.died.subtitle").variable("time", "%.2f".formatted(revivalTimer)).parse(),
                        0, 4, 5
                    );
                    revivalTimer -= 0.1;
                    if (revivalTimer <= 0) {
                        revive();
                    }
                }, 0, 2);
                
                offender = null;
                
                if (bwOffender != null) bwOffender.kills++;
            }
            else {
                eliminate();
                if (bwOffender != null) bwOffender.finalKills++;
            }
            ScoreboardManager.updateAll();
        }
        else deathPending = true;
    }
    public void revive() {
        if (!dead) return;
        
        dead = false;
        spectator = false;
        if (reviveTask != null) {
            reviveTask.cancel();
            reviveTask = null;
            revivalTimer = 0;
        }
        if (player.isOnline()) {
            player.getPlayer().setGameMode(GameMode.SURVIVAL);
            player.getPlayer().teleport(team.getTeamColor().getSpawnLocation());
            player.getPlayer().getInventory().clear();
            player.getPlayer().setHealth(20);
            WoodenSword.update(this, player.getPlayer().getInventory());
            RankedDealType.getDefinedRanks().values().forEach((v) -> {
                v.refreshInv(getOnlinePlayer());
            });
            
            RankedDealType.refreshPlayer(getOnlinePlayer());
        }
        else revivalPending = true;
    }
    public void eliminate() {
        if (spectator) return;
        if (!dead) {
            // TODO make some more spectator features
            player.getPlayer().setGameMode(GameMode.SPECTATOR);
            player.getPlayer().setHealth(20);
            dead = true;
        }
        Bukkit.getServer().broadcastMessage(String.format("%s was eliminated.", player.getName()));
        
        if (team.decreaseRemainingPlayers() > 0) {
            Utility.sendTitle(
                getOnlinePlayer(),
                MessageUtility.parser("player.solo.eliminated.title").parse(),
                MessageUtility.parser("player.solo.eliminated.subtitle").parse(),
                5, 40, 10
            );
        }
        else
            Bukkit.getServer().broadcastMessage(String.format("Team %s was eliminated.", team.getTeamColor().getName()));
            if (team.getPlayersCount() == 1) {
                Utility.sendTitle(
                    getOnlinePlayer(),
                    MessageUtility.parser("player.solo.eliminated.title").parse(),
                    MessageUtility.parser("player.solo.eliminated.subtitle").parse(),
                    5, 40, 10
                );
            }
            else
            Utility.sendTitle(
                getOnlinePlayer(),
                MessageUtility.parser("player.team.eliminated.title").parse(),
                MessageUtility.parser("player.team.eliminated.subtitle").parse(),
                5, 40, 10
            );
    
        spectator = true;
    }
    public void close() {
        if (reviveTask != null) {
            reviveTask.cancel();
        }

        offenceTask.cancel();
        
        if (isOnline()) {
            for(PotionEffect effect : getOnlinePlayer().getActivePotionEffects()) {
                getOnlinePlayer().removePotionEffect(effect.getType());
            }
            getOnlinePlayer().setGameMode(GameMode.SPECTATOR);
            getOnlinePlayer().getInventory().clear();
            getOnlinePlayer().getEnderChest().clear();
        }        
        
        HandlerList.unregisterAll(this);
        
        offenceTask = null;
        reviveTask = null;
        team = null;
        player = null;
        deathMessages = null;
    }
    
    @EventHandler
    private void onLogout(PlayerQuitEvent e) {
        if (equals(e.getPlayer())) {
            if (!team.hasBed() ) {
                eliminate();
            }
            else if (!dead) deathPending = true;
        }
    }
    @EventHandler
    private void onLogin(PlayerJoinEvent e) {
        if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
            player = e.getPlayer();
            Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                if (deathPending) {
                    if (!revivalPending) kill(DeathMessage.getMsg(player, offender, deathMessages, "generic"));
                    else revivalPending = false;
                    deathPending = false;
                }
                if (revivalPending) {
                    if (!deathPending) revive();
                    revivalPending = false;
                }
                ScoreboardManager.update(e.getPlayer());
            }, 3);
        }
        
        updateInvisiblity();
    }
    @EventHandler
    private void onEntityDamageEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player)e.getEntity();
            if (equals(p)) {
                AttributeInstance attribute = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
                attribute.setBaseValue(16);
                p.saveData();
                if (e.getDamager() instanceof Player) {
                    offender = (OfflinePlayer)e.getDamager();
                }
                if (e.getDamager() instanceof Projectile) {
                    Projectile projectile = (Projectile)e.getDamager();
                    if (projectile.getShooter() instanceof Player) {
                        OfflinePlayer shooter = (OfflinePlayer)projectile.getShooter();
                        if (equals(shooter) && e.getDamager() instanceof Fireball) e.setDamage(0);
                        offender = shooter;
                    }
                }
                if (e.getDamager() instanceof Explosive) {
                    e.setDamage(e.getDamage() / 3);
                    if (e.getFinalDamage() >= p.getHealth()) {
                        e.setDamage(0);
                        kill(DeathMessage.getMessage(e.getCause(), player, offender, deathMessages));
                    }
                }
                
                if (offender != null) offenceTimer = 15;
            }
        }
    }
    @EventHandler
    private void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player)e.getEntity();
            if (equals(p)) {
                if (e.getCause() == DamageCause.ENTITY_EXPLOSION) {
                    return;
                }
                if (e.getFinalDamage() >= p.getHealth()) {
                    e.setDamage(0);
                    kill(DeathMessage.getMessage(e.getCause(), player, offender, deathMessages));
                }
            }
        }
    }
    @EventHandler
    private void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
         
        if (e.getCause() == TeleportCause.ENDER_PEARL) {
            e.setCancelled(true);
 
            player.teleport(e.getTo());
        }
    }
    private void onInvisExpire() {
        removeInvis();
    }
    
    @EventHandler
    private void onMove(PlayerMoveEvent e) {
        if (e.getPlayer() instanceof Player) {
            if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                if (e.getTo().getY() < 40) {
                    e.setTo(e.getTo().add(0, 100, 0));
                    e.getPlayer().playSound(e.getTo(), Sound.ENTITY_PLAYER_HURT, 1, 1);
                    kill(DeathMessage.getMessage(DamageCause.VOID, player, offender, deathMessages));
                }
            }
        }
    }
    @EventHandler
    private void onThrow(PlayerDropItemEvent e) {
        if (equals(e.getPlayer())) {
             if (e.getItemDrop().getItemStack().getType() == Material.WOODEN_SWORD) e.setCancelled(true);
             else WoodenSword.update(this, e.getPlayer().getInventory());
        }
    }
    @EventHandler
    private void onPickup(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player)e.getEntity();
            if (WoodenSword.isOtherSword(e.getItem().getItemStack().getType()))
                p.getInventory().remove(Material.WOODEN_SWORD);
        }
    }
    @EventHandler
    private void onConsume(PlayerItemConsumeEvent e) {
        if (e.getPlayer() instanceof Player) {
            if (e.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                if (e.getItem().getItemMeta() instanceof PotionMeta) {
                    PotionMeta meta = (PotionMeta)e.getItem().getItemMeta();
                    
                    meta.getCustomEffects().forEach(eff -> {
                        Utility.applyPotionEffect(getOnlinePlayer(), eff);
                
                        if (eff.getType().equals(PotionEffectType.INVISIBILITY)) {
    
                            if (invisible) {
                                invisTask.cancel();
                            }
                            
                            invisTask = Bukkit.getScheduler().runTaskLater(Main.getInstance(),
                                () -> onInvisExpire(),
                                eff.getDuration()
                            );
                            invisible = true;
                            updateInvisiblity();
                        }
                    });
                    
                    e.getItem().setType(Material.AIR);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    private void onInventory(InventoryClickEvent e) {
        if (e.getCursor() != null && e.getCursor().getType() == Material.WOODEN_SWORD) {
            if (e.getClickedInventory() != e.getWhoClicked().getInventory()) e.setCancelled(true);
        }
        else if (e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.WOODEN_SWORD) {
            if (e.getClick() == ClickType.SHIFT_LEFT || e.getClick() == ClickType.SHIFT_RIGHT) e.setCancelled(true);
        }
    }
    
    public OfflinePlayer getOffender() {
        return offender;
    }
    
    public BedwarsPlayer(OfflinePlayer p, Team t) {
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
        team = t;
        player = p;
        deathMessages = DeathMessage.getMessages(p);
        offenceTask = Bukkit.getServer().getScheduler().runTaskTimer(Main.getInstance(), () -> {
            if (offenceTimer > 0) {
                offenceTimer--;
                if (offenceTimer == 0) offender = null;
            }
        }, 0, 20);
        
        if (p.isOnline()) {
            dead = true;
            revive();
        }
        else {
            dead = false;
            kill(DeathMessage.getMsg(p, null, deathMessages, "generic"));
        }
    }

    public boolean equals(UUID p) {
        if (p == null) return false;
        return player.getUniqueId().equals(p);
    }
    public boolean equals(OfflinePlayer p) {
        if (p == null) return false;
        return player.getUniqueId().equals(p.getUniqueId());
    }
}
