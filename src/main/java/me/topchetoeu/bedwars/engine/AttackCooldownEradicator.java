package me.topchetoeu.bedwars.engine;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AttackCooldownEradicator implements Listener{
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerJoinEvent e){
        setAttackSpeed(e.getPlayer(), 32);
    }

    private void setAttackSpeed(Player player, double attackSpeed){
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);

        attribute.setBaseValue(attackSpeed);
        player.saveData();
    }

    public static AttackCooldownEradicator init(JavaPlugin pl) {
        AttackCooldownEradicator instance = new AttackCooldownEradicator();
        Bukkit.getPluginManager().registerEvents(instance, pl);
        return instance;
    }
}
