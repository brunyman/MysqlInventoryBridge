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
		final String syncArmor = inv.getConfigHandler().getString("General.syncArmorEnabled");
		delay = Integer.parseInt(inv.getConfigHandler().getString("General.loginSyncDelay")) / 1000;
		
		//Added a small delay to prevent the onDisconnect handler overlapping onLogin on a BungeeCord configuration when switching servers.
		Bukkit.getServer().getScheduler().runTaskLaterAsynchronously(inv, new Runnable() {
			@Override
			public void run() {
				if (inv.getInvMysqlInterface().hasAccount(event.getUniqueId()) == false) return;
				final Player p = Bukkit.getPlayer(event.getUniqueId());
				UUID playerUUID = event.getUniqueId();
				
				if (inv.getInvMysqlInterface().getInventory(playerUUID) == "none") return;
				
				final String rawInv = inv.getInvMysqlInterface().getInventory(playerUUID);
				final String rawArmor = inv.getInvMysqlInterface().getArmor(playerUUID);
				
				Bukkit.getScheduler().runTask(inv, new Runnable() {
					@Override
					public void run() {
						try {
							if (syncArmor == "true") {
								Inventory inventory = null;
								ItemStack[] invStacks = null;
								if (inv.useProtocolLib == true && inv.getConfigHandler().getString("General.enableModdedItemsSupport").matches("true")) {
									invStacks = InventoryUtils.restoreModdedStacks(rawInv);
									p.getInventory().clear();
									p.getInventory().setContents(invStacks);
									p.getInventory().setArmorContents(InventoryUtils.restoreModdedStacks(rawArmor));
									p.updateInventory();
								} else {
									inventory = InventoryUtils.fromBase64(rawInv);
									p.getInventory().clear();
									p.getInventory().setContents(inventory.getContents());
									p.getInventory().setArmorContents(InventoryUtils.itemStackArrayFromBase64(rawArmor));
									p.updateInventory();
								}
							} else {
								Inventory inventory = null;
								ItemStack[] invStacks = null;
								if (inv.useProtocolLib == true && inv.getConfigHandler().getString("General.enableModdedItemsSupport").matches("true")) {
									invStacks = InventoryUtils.restoreModdedStacks(rawInv);
									p.getInventory().clear();
									p.getInventory().setContents(invStacks);
									p.updateInventory();
								} else {
									inventory = InventoryUtils.fromBase64(rawInv);
									p.getInventory().clear();
									p.getInventory().setContents(inventory.getContents());
									p.updateInventory();
								}
							}
							
						} catch (Exception e) {
							
						}
					}
				});
				inv.getInvMysqlInterface().setInventory(playerUUID, p, "none", "none");
				
			}
		}, delay * 20L + 5L);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onDisconnect(final PlayerQuitEvent event) {
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(inv, new Runnable() {
			@Override
			public void run() {
				String syncArmor = inv.getConfigHandler().getString("General.syncArmorEnabled");
				final Player p = event.getPlayer();
				final Inventory inventory = p.getInventory();
				ItemStack[] armor = p.getInventory().getArmorContents();
				
				if (syncArmor == "true") {
					if (inv.useProtocolLib == true && inv.getConfigHandler().getString("General.enableModdedItemsSupport").matches("true")) {
						inv.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.saveModdedStacksData(inventory.getContents()), InventoryUtils.saveModdedStacksData(armor));
					} else {
						inv.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.toBase64(inventory), InventoryUtils.itemStackArrayToBase64(armor));
					}
					
					Bukkit.getScheduler().runTask(inv, new Runnable() {
						@Override
						public void run() {
							p.getInventory().setHelmet(new ItemStack(Material.AIR));
							p.getInventory().setChestplate(new ItemStack(Material.AIR));
							p.getInventory().setLeggings(new ItemStack(Material.AIR));
							p.getInventory().setBoots(new ItemStack(Material.AIR));
							inventory.clear();
						}
					});
				} else {
					if (inv.useProtocolLib == true && inv.getConfigHandler().getString("General.enableModdedItemsSupport").matches("true")) {
						inv.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.saveModdedStacksData(inventory.getContents()), "none");
					} else {
						inv.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.toBase64(inventory), "none");
					}
					Bukkit.getScheduler().runTask(inv, new Runnable() {
						@Override
						public void run() {
							inventory.clear();
						}
					});
				}
			}
		}, 5L);
		
	}

}
