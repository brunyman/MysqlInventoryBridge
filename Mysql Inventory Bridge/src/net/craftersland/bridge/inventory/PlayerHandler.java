package net.craftersland.bridge.inventory;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerHandler implements Listener {
	
	private Inv inv;
	private int delay = 1;
	private HashMap<String, Boolean> playersSync = new HashMap<String, Boolean>();
	
	public PlayerHandler(Inv inv) {
		this.inv = inv;
	}
	
	@EventHandler
	public void onLogin(final PlayerJoinEvent event) {
		final String syncArmor = inv.getConfigHandler().getString("General.syncArmorEnabled");
		delay = Integer.parseInt(inv.getConfigHandler().getString("General.loginSyncDelay")) / 1000;
		
		//Added a small delay to prevent the onDisconnect handler overlapping onLogin on a BungeeCord configuration when switching servers.
		Bukkit.getScheduler().runTaskLaterAsynchronously(inv, new Runnable() {
			@Override
			public void run() {
				if (inv.getInvMysqlInterface().hasAccount(event.getPlayer().getUniqueId()) == false) return;
				final Player p = event.getPlayer();
				UUID playerUUID = event.getPlayer().getUniqueId();
				if (inv.getInvMysqlInterface().getInventory(playerUUID).matches("none") && inv.getInvMysqlInterface().getArmor(playerUUID).matches("none")) return;
				final String rawInv = inv.getInvMysqlInterface().getInventory(playerUUID);
				final String rawArmor = inv.getInvMysqlInterface().getArmor(playerUUID);
				ItemStack[] armor = null;
				ItemStack[] inventory = null;
				if (inv.getConfigHandler().getString("General.enableModdedItemsSupport").matches("true") && inv.useProtocolLib == true) {
					armor = InventoryUtils.restoreModdedStacks(rawArmor);
					inventory = InventoryUtils.restoreModdedStacks(rawInv);
					if (armor == null) {
						try {
							armor = InventoryUtils.itemStackArrayFromBase64(rawArmor);
						} catch (Exception e) {
						}
					}
					if (inventory == null) {
						try {
							inventory = InventoryUtils.fromBase64(rawInv).getContents();
						} catch (Exception e) {
						}
					}
				} else {
					try {
						armor = InventoryUtils.itemStackArrayFromBase64(rawArmor);
						inventory = InventoryUtils.itemStackArrayFromBase64(rawInv);
						if (inventory == null) {
							inventory = InventoryUtils.fromBase64(rawInv).getContents();
						}
					} catch (Exception e) {
					}
				}
				final ItemStack[] finalArmor = armor;
				final ItemStack[] finalInventory = inventory;
				
				Bukkit.getScheduler().runTaskLater(inv, new Runnable() {
					@Override
					public void run() {
						try {							
							if (syncArmor == "true") {
								p.getInventory().setArmorContents(finalArmor);
								if (p.getInventory().getArmorContents() != finalArmor) {
									p.getInventory().setArmorContents(finalArmor);
								}
							}
							p.getInventory().setContents(finalInventory);
							if (p.getInventory().getContents() != finalInventory) {
								p.getInventory().setContents(finalInventory);
							}
						} catch (Exception e) {
						}
						if (inv.getConfigHandler().getString("ChatMessages.syncComplete").matches("") == false) {
							p.sendMessage(inv.getConfigHandler().getString("ChatMessages.syncComplete").replaceAll("&", "§"));
							p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 1);
							p.updateInventory();
						}
						playersSync.put(p.getName(), true);
					}
				}, 5L);
				inv.getInvMysqlInterface().setInventory(playerUUID, p, "none", "none");
			}
		}, delay * 20L + 5L);
	}
	
	@EventHandler
	public void onDisconnect(final PlayerQuitEvent event) {
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(inv, new Runnable() {
			@Override
			public void run() {
				String syncArmor = inv.getConfigHandler().getString("General.syncArmorEnabled");
				final Player p = event.getPlayer();
				if (playersSync.containsKey(p.getName()) == false) return;
				final Inventory inventory = p.getInventory();
				ItemStack[] armor = p.getInventory().getArmorContents();
				
				if (syncArmor.matches("true")) {
					if (inv.useProtocolLib == true && inv.getConfigHandler().getString("General.enableModdedItemsSupport").matches("true")) {
						inv.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.saveModdedStacksData(inventory.getContents()), InventoryUtils.saveModdedStacksData(armor));
					} else {
						inv.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.itemStackArrayToBase64(inventory.getContents()), InventoryUtils.itemStackArrayToBase64(armor));
					}
					if (inv.getConfigHandler().getString("General.disableItemClear").matches("false")) {
						Bukkit.getScheduler().runTaskLater(inv, new Runnable() {
							@Override
							public void run() {
								p.getInventory().setHelmet(new ItemStack(Material.AIR));
								p.getInventory().setChestplate(new ItemStack(Material.AIR));
								p.getInventory().setLeggings(new ItemStack(Material.AIR));
								p.getInventory().setBoots(new ItemStack(Material.AIR));
								p.getInventory().clear();
								p.saveData();
							}
						}, 5L);
					}
				} else {
					if (inv.useProtocolLib == true && inv.getConfigHandler().getString("General.enableModdedItemsSupport").matches("true")) {
						inv.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.saveModdedStacksData(inventory.getContents()), "none");
					} else {
						inv.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.itemStackArrayToBase64(inventory.getContents()), "none");
					}
					if (inv.getConfigHandler().getString("General.disableItemClear").matches("false")) {
						Bukkit.getScheduler().runTaskLater(inv, new Runnable() {
							@Override
							public void run() {
								p.getInventory().clear();
								p.saveData();
							}
						}, 5L);
					}
				}
			}
		}, 5L);
		
	}

}
