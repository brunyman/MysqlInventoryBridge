package net.craftersland.bridge.inventory.events;

import net.craftersland.bridge.inventory.Inv;
import net.craftersland.bridge.inventory.InventoryUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoin implements Listener {
	
	private Inv inv;
	
	public PlayerJoin(Inv inv) {
		this.inv = inv;
	}
	
	@EventHandler
	public void onLogin(final PlayerJoinEvent event) {		
		//Added a small delay to prevent the onDisconnect handler overlapping onLogin on a BungeeCord configuration when switching servers.
		Bukkit.getScheduler().runTaskLaterAsynchronously(inv, new Runnable() {
			@Override
			public void run() {
				if (inv.getInvMysqlInterface().hasAccount(event.getPlayer().getUniqueId()) == false) {
					inv.playersSync.add(event.getPlayer().getName());
				} else {
					final Player p = event.getPlayer();
					final String rawInv = inv.getInvMysqlInterface().getInventory(p.getUniqueId());
					final String rawArmor = inv.getInvMysqlInterface().getArmor(p.getUniqueId());
					if (rawInv.matches("none") == false && rawArmor.matches("none") == false) {
						ItemStack[] armor = null;
						ItemStack[] inventory = null;
						if (inv.getConfigHandler().getBoolean("General.enableModdedItemsSupport") == true && inv.useProtocolLib == true) {
							armor = InventoryUtils.restoreModdedStacks(rawArmor);
							inventory = InventoryUtils.restoreModdedStacks(rawInv);
							if (armor == null) {
								try {
									armor = InventoryUtils.itemStackArrayFromBase64(rawArmor);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							if (inventory == null) {
								try {
									inventory = InventoryUtils.fromBase64(rawInv).getContents();
								} catch (Exception e) {
									e.printStackTrace();
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
								e.printStackTrace();
							}
						}
						final ItemStack[] finalArmor = armor;
						final ItemStack[] finalInventory = inventory;
						
						Bukkit.getScheduler().runTaskLater(inv, new Runnable() {
							@Override
							public void run() {
								try {							
									if (inv.getConfigHandler().getBoolean("General.syncArmorEnabled") == true) {
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
									e.printStackTrace();
								}
								if (inv.getConfigHandler().getString("ChatMessages.syncComplete").matches("") == false) {
									p.sendMessage(inv.getConfigHandler().getString("ChatMessages.syncComplete").replaceAll("&", "§"));
									inv.getSoundHandler().sendLevelUpSound(p);
									p.updateInventory();
								}
								inv.playersSync.add(event.getPlayer().getName());
							}
						}, 5L);
					} else {
						inv.playersSync.add(event.getPlayer().getName());
					}
				}
			}
		}, inv.getConfigHandler().getInteger("General.loginSyncDelay") / 1000 * 20L + 5L);
	}

}
