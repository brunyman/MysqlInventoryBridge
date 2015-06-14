package net.craftersland.bridge.inventory;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerHandler implements Listener {
	
	private Inv inv;
	private int delay = 1;
	
	public PlayerHandler(Inv inv) {
		this.inv = inv;
	}
	
	@EventHandler
	public void onLogin(final AsyncPlayerPreLoginEvent event) {
		if (inv.getInvMysqlInterface().hasAccount(event.getUniqueId()) == false) return;
		final String syncArmor = inv.getConfigHandler().getString("General.syncArmorEnabled");
		delay = Integer.parseInt(inv.getConfigHandler().getString("General.loginSyncDelay")) / 1000;
		
		//Added a small delay to prevent the onDisconnect handler overlapping onLogin on a BungeeCord configuration when switching servers.
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(inv, new Runnable() {
			@Override
			public void run() {
				Player p = Bukkit.getPlayer(event.getUniqueId());
				UUID playerUUID = event.getUniqueId();
				String lastPlayed = String.valueOf(p.getLastPlayed());
				
				if (inv.getInvMysqlInterface().getInventory(playerUUID) == "none") return;
				
				String rawInv = inv.getInvMysqlInterface().getInventory(playerUUID);
				String rawArmor = inv.getInvMysqlInterface().getArmor(playerUUID);
				
				try {
					if (syncArmor == "true") {
						Inventory inventory = InventoryUtils.fromBase64(rawInv);
						p.getInventory().clear();
						p.getInventory().setContents(inventory.getContents());
						p.getInventory().setArmorContents(InventoryUtils.itemStackArrayFromBase64(rawArmor));
						p.updateInventory();
					} else {
						Inventory inventory = InventoryUtils.fromBase64(rawInv);
						p.getInventory().clear();
						p.getInventory().setContents(inventory.getContents());
						p.updateInventory();
					}
					inv.getInvMysqlInterface().setInventory(playerUUID, p, "none", "none", lastPlayed);
					
				} catch (Exception e) {
					
				}
				
			}
		}, delay * 20L + 5L);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onDisconnect(final PlayerQuitEvent event) {
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(inv, new Runnable() {
			@Override
			public void run() {
				String syncArmor = inv.getConfigHandler().getString("General.syncArmorEnabled");
				Player p = event.getPlayer();
				Inventory inventory = p.getInventory();
				ItemStack[] armor = p.getInventory().getArmorContents();
				String lastPlayed = String.valueOf(p.getLastPlayed());
				
				if (syncArmor == "true") {
					inv.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.toBase64(inventory), InventoryUtils.itemStackArrayToBase64(armor), lastPlayed);	
					
					p.getInventory().setHelmet(new ItemStack(Material.AIR));
					p.getInventory().setChestplate(new ItemStack(Material.AIR));
					p.getInventory().setLeggings(new ItemStack(Material.AIR));
					p.getInventory().setBoots(new ItemStack(Material.AIR));
					inventory.clear();
				} else {
					inv.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.toBase64(inventory), "none", lastPlayed);
					inventory.clear();
				}
			}
		}, 5L);
		
	}

}
