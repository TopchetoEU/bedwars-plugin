package me.topchetoeu.bedwars.engine.trader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.io.Files;

import me.topchetoeu.bedwars.Main;
import net.minecraft.server.v1_8_R3.NBTTagCompound;

public class Traders implements Listener {
	public static Traders instance = null;
	
	private List<UUID> villagers = new ArrayList<>();
	private File file;
	
	private void write() {
		 BufferedWriter writer;
		try {
			writer = Files.newWriter(file, Charset.defaultCharset());
		 
			 for (UUID uuid : villagers) {
				 writer.write(uuid.toString() + "\n");
			}
			 writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Villager summonVillager(Location loc) {
		Villager vil = (Villager)loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);
		
		net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) vil).getHandle();
	    NBTTagCompound tag = nmsEntity.getNBTTag();
	    if (tag == null) {
	        tag = new NBTTagCompound();
	    }
		
	    nmsEntity.c(tag);
	    tag.setInt("NoAI", 1);
	    nmsEntity.f(tag);
	    
	    nmsEntity.b(true);
		
		villagers.add(vil.getUniqueId());
		
		write();
	
		return vil;
	}
	
	@EventHandler
	private void onEntityInteract(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() instanceof Villager) {
			Villager v = (Villager)e.getRightClicked();
			
			if (villagers.stream().anyMatch(_v -> _v.equals(v.getUniqueId()))) {
				e.setCancelled(true);

				File favsDir = new File(Main.getInstance().getDataFolder(), "favourites");
				
				new TraderGUI(favsDir, e.getPlayer()).open();
			}
		}
	}
	@EventHandler
	private void onUse(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK &&
			e.hasItem() &&
			e.getItem().hasItemMeta() &&
			e.getItem().getItemMeta().hasDisplayName() &&
			e.getItem().getItemMeta().getDisplayName() == "§rTrader spawner") {
			if (e.getItem().getType() == Material.MONSTER_EGG) {
				int yaw = (int)e.getPlayer().getLocation().getYaw() - 45;
				if (yaw < 0) yaw += 360;
				
				yaw = yaw / 90 * 90;
				yaw -= 90;
				
				
				Location loc = new Location(
					e.getClickedBlock().getLocation().getWorld(),
					e.getClickedBlock().getLocation().getBlockX() + .5,
					e.getClickedBlock().getLocation().getBlockY(),
					e.getClickedBlock().getLocation().getBlockZ() + .5,
					yaw, 0
				);
				
				if (e.getBlockFace() == BlockFace.DOWN) loc.setY(loc.getY() - 2); 
				if (e.getBlockFace() == BlockFace.UP) loc.setY(loc.getY() + 1);
				if (e.getBlockFace() == BlockFace.SOUTH) loc.setZ(loc.getZ() + 1);
				if (e.getBlockFace() == BlockFace.NORTH) loc.setZ(loc.getZ() - 1);
				if (e.getBlockFace() == BlockFace.EAST) loc.setX(loc.getX() + 1);
				if (e.getBlockFace() == BlockFace.WEST) loc.setX(loc.getX() - 1);
				
				summonVillager(loc);
				
				e.getPlayer().sendMessage("Trader spawned!");
				e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.VILLAGER_YES, 1, 1);
			}
		}
	}
	@EventHandler
	private void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Villager) {
			Villager v = (Villager)e.getEntity();
				
			if (villagers.stream().anyMatch(_v -> _v.equals(v.getUniqueId()))) {
				e.setCancelled(true);
			}
		}
	}
	@EventHandler
	private void onEntityDamageEntity(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			Player p = (Player)e.getDamager();
			
			if (e.getEntity() instanceof Villager) {
				Villager v = (Villager)e.getEntity();
				
				if (villagers.stream().anyMatch(_v -> _v.equals(v.getUniqueId()))) {
					e.setCancelled(true);
					
					ItemStack hand = p.getInventory().getItemInHand();
					
					if (hand != null &&
						hand.hasItemMeta() &&
						hand.getItemMeta().hasDisplayName() &&
						hand.getItemMeta().getDisplayName().equals("§rTrader eradicator")) {
						if (hand.getType() == Material.STICK) {
							villagers.remove(v.getUniqueId());
							write();
							v.remove();
							p.playSound(p.getLocation(), Sound.VILLAGER_DEATH, 1, 1);
							p.sendMessage("Trader removed!");
						}
					}
				}
			}
		}
	}
	
	
	
	public Traders(File tradersFile) throws IOException {
		if (!tradersFile.exists()) tradersFile.createNewFile();
		villagers = Files.readLines(tradersFile, Charset.defaultCharset())
			.stream()
			.map(v -> UUID.fromString(v))
			.collect(Collectors.toList());
		
		file = tradersFile;
		
		Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
	}
}
