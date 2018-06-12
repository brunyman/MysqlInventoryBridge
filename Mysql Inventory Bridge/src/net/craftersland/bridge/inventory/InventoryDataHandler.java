package net.craftersland.bridge.inventory;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.craftersland.bridge.inventory.objects.DatabaseInventoryData;
import net.craftersland.bridge.inventory.objects.InventorySyncData;
import net.craftersland.bridge.inventory.objects.InventorySyncTask;

public class InventoryDataHandler {
	
	private Inv pd;
	private Set<Player> playersInSync = new HashSet<Player>();
	private Set<Player> playersDisconnectSave = new HashSet<Player>();
	
	public InventoryDataHandler(Inv pd) {
		this.pd = pd;
	}
	
	public boolean isSyncComplete(Player p) {
		if (playersInSync.contains(p) == true) {
			return true;
		} else {
			return false;
		}
	}
	
	private void dataCleanup(Player p) {
		playersInSync.remove(p);
		playersDisconnectSave.remove(p);
	}
	
	public void setPlayerData(final Player p, DatabaseInventoryData data, InventorySyncData syncData, boolean cancelTask) {
		if (playersInSync.contains(p) == false) {
			//Inventory and Armor sync for lower mc versions then 1.9
			if (Inv.is19Server == false) {
				setInventory(p, data, syncData);
				if (pd.getConfigHandler().getBoolean("General.syncArmorEnabled") == true) {
					setArmor(p, data, syncData);
				}
			} else {
				//Inventory and Armor sync for 1.9 and up.
				if (pd.getConfigHandler().getBoolean("General.syncArmorEnabled") == true) {
					setInventory(p, data, syncData);
				} else if (pd.getConfigHandler().getBoolean("General.syncArmorEnabled") == false) {
					//TODO fix
					setInventoryNew(p, data, syncData);
				}
			}
			pd.getInvMysqlInterface().setSyncStatus(p, "false");
			Bukkit.getScheduler().runTaskLaterAsynchronously(pd, new Runnable() {

				@Override
				public void run() {
					playersInSync.add(p);
				}
				
			}, 2L);
			data = null;
		}
	}
	
	public void onDataSaveFunction(Player p, Boolean datacleanup, String syncStatus, ItemStack[] inventoryDisconnect, ItemStack[] armorDisconnect) {
		if (playersDisconnectSave.contains(p) == true) {
			if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
				Inv.log.info("Inventory Debug - Save Data - Canceled - " + p.getName());
			}
			return;
		}
		if (datacleanup == true) {
			playersDisconnectSave.add(p);
		}
		boolean isPlayerInSync = playersInSync.contains(p);
		if (isPlayerInSync == true) {
			String inv = "none";
			String armor = "none";
			if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
				Inv.log.info("Inventory Debug - Save Data - Start - " + p.getName());
			}
			try {
				if (inventoryDisconnect != null) {
					if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
						Inv.log.info("Inventory Debug - Set Data - Saving disconnect inventory - " + p.getName());
					}
					inv = encodeItems(inventoryDisconnect);
				} else {
					if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
						Inv.log.info("Inventory Debug - Set Data - Saving inventory - " + p.getName());
					}
					inv = encodeItems(p.getInventory().getContents());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (pd.getConfigHandler().getBoolean("General.syncArmorEnabled") == true) {
				try {
					if (inventoryDisconnect != null) {
						armor = encodeItems(armorDisconnect);
					} else {
						armor = encodeItems(p.getInventory().getArmorContents());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			pd.getInvMysqlInterface().setData(p, inv, armor, syncStatus);
		}
		if (datacleanup == true) {
			dataCleanup(p);
		}
	}
	
	public void onJoinFunction(final Player p) {
		if (Inv.isDisabling == false) {
			if (playersInSync.contains(p) == false) {
				if (pd.getInvMysqlInterface().hasAccount(p) == true) {
					final InventorySyncData syncData = new InventorySyncData();
					backupAndReset(p, syncData);
					DatabaseInventoryData data = pd.getInvMysqlInterface().getData(p);
					if (data.getSyncStatus().matches("true")) {
						setPlayerData(p, data, syncData, false);
					} else {
						new InventorySyncTask(pd, System.currentTimeMillis(), p, syncData).runTaskTimerAsynchronously(pd, 10L, 10L);
					}
				} else {
					playersInSync.add(p);
					onDataSaveFunction(p, false, "false", null, null);
				}
			}
		}
	}
	
	private void backupAndReset(Player p, InventorySyncData syncData) {
		syncData.setBackupInventory(p.getInventory().getContents());
		syncData.setBackupArmor(p.getInventory().getArmorContents());
		p.setItemOnCursor(null);
		p.getInventory().clear();
		p.updateInventory();
		if (pd.getConfigHandler().getBoolean("General.syncArmorEnabled") == true) {
			syncData.setBackupArmor(p.getInventory().getArmorContents());
			p.getInventory().setHelmet(null);
			p.getInventory().setChestplate(null);
			p.getInventory().setLeggings(null);
			p.getInventory().setBoots(null);
			p.updateInventory();
		}
	}
	
	public ItemStack[] getInventory(Player p) {
		return p.getInventory().getContents();
	}
	
	public ItemStack[] getArmor(Player p) {
		if (pd.getConfigHandler().getBoolean("General.syncArmorEnabled") == true) {
			return p.getInventory().getArmorContents();
		} else {
			return null;
		}
	}
	
	private void setInventory(final Player p, DatabaseInventoryData data, InventorySyncData syncData) {
		if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
			Inv.log.info("Inventory Debug - Set Data - Start- " + p.getName());
		}
		if (data.getRawInventory().matches("none") == false) {
			if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
				Inv.log.info("Inventory Debug - Set Data - Loading inventory - " + p.getName());
			}
			try {
				p.getInventory().setContents(decodeItems(data.getRawInventory()));
				if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
					Inv.log.info("Inventory Debug - Set Data - Inventory set - " + p.getName());
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (syncData.getBackupInventory() != null) {
					if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
						Inv.log.info("Inventory Debug - Set Data - Loading backup inventory - " + p.getName());
					}
					p.getInventory().setContents(syncData.getBackupInventory());
					p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessage.inventorySyncError"));
					pd.getSoundHandler().sendPlingSound(p);
					p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessage.inventorySyncBackup"));
				} else {
					if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
						Inv.log.info("Inventory Debug - Set Data - No backup inventory found! - " + p.getName());
					}
				}
			}
		} else {
			if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
				Inv.log.info("Inventory Debug - Set Data - Restoring local inventory - " + p.getName());
			}
			p.getInventory().setContents(syncData.getBackupInventory());
		}
		p.updateInventory();
	}
	
	private void setInventoryNew(final Player p, DatabaseInventoryData data, InventorySyncData syncData) {
		if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
			Inv.log.info("Inventory Debug - Set Data - Start- " + p.getName());
		}
		if (data.getRawInventory().matches("none") == false) {
			if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
				Inv.log.info("Inventory Debug - Set Data - Loading inventory - " + p.getName());
			}
			try {
				p.getInventory().setContents(decodeItems(data.getRawInventory()));
				p.getInventory().setArmorContents(syncData.getBackupArmor());
				if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
					Inv.log.info("Inventory Debug - Set Data - Inventory set - " + p.getName());
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (syncData.getBackupInventory() != null) {
					if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
						Inv.log.info("Inventory Debug - Set Data - Loading backup inventory - " + p.getName());
					}
					p.getInventory().setContents(syncData.getBackupInventory());
					p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessage.inventorySyncError"));
					pd.getSoundHandler().sendPlingSound(p);
					p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessage.inventorySyncBackup"));
				} else {
					if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
						Inv.log.info("Inventory Debug - Set Data - No backup inventory found! - " + p.getName());
					}
				}
			}
		} else {
			if (pd.getConfigHandler().getBoolean("Debug.InventorySync") == true) {
				Inv.log.info("Inventory Debug - Set Data - Restoring local inventory - " + p.getName());
			}
			p.getInventory().setContents(syncData.getBackupInventory());
		}
		p.updateInventory();
	}
	
	private void setArmor(final Player p, DatabaseInventoryData data, InventorySyncData syncData) {
		if (data.getRawArmor().matches("none") == false) {
			try {
				p.getInventory().setArmorContents(decodeItems(data.getRawArmor()));
			} catch (Exception e) {
				e.printStackTrace();
				p.getInventory().setArmorContents(syncData.getBackupArmor());
				p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessage.armorSyncError"));
				pd.getSoundHandler().sendPlingSound(p);
				p.sendMessage(pd.getConfigHandler().getStringWithColor("ChatMessage.armorSyncBackup"));
			}
		} else {
			p.getInventory().setArmorContents(syncData.getBackupArmor());
		}
		p.updateInventory();
	}
	
	public String encodeItems(ItemStack[] items) {
		if (pd.useProtocolLib == true && pd.getConfigHandler().getBoolean("General.enableModdedItemsSupport") == true) {
			return InventoryUtils.saveModdedStacksData(items);
		} else {
			return InventoryUtils.itemStackArrayToBase64(items);
		}
	}
	
	public ItemStack[] decodeItems(String data) throws Exception {
		if (pd.useProtocolLib == true && pd.getConfigHandler().getBoolean("General.enableModdedItemsSupport") == true) {
			ItemStack[] it = InventoryUtils.restoreModdedStacks(data);
			if (it == null) {
				it = InventoryUtils.itemStackArrayFromBase64(data);
			}
			return it;
		} else {
			return InventoryUtils.itemStackArrayFromBase64(data);
		}
	}

}
