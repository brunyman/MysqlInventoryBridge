package net.craftersland.bridge.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BackgroundTask {
	
	private Inv m;
	
	public BackgroundTask(Inv m) {
		this.m = m;
		runTask();
	}
	
	private void runTask() {
		if (m.getConfigHandler().getBoolean("General.saveDataTask.enabled") == true) {
			Inv.log.info("Data save task is enabled.");
			Bukkit.getScheduler().runTaskTimerAsynchronously(m, new Runnable() {

				@Override
				public void run() {
					runSaveData();
				}
				
			}, m.getConfigHandler().getInteger("General.saveDataTask.interval") * 60 * 20L, m.getConfigHandler().getInteger("General.saveDataTask.interval") * 60 * 20L);
		} else {
			Inv.log.info("Data save task is disabled.");
		}
	}
	
	private void runSaveData() {
		if (m.getConfigHandler().getBoolean("General.saveDataTask.enabled") == true) {
			if (Bukkit.getOnlinePlayers().isEmpty() == false) {
				List<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
				if (m.getConfigHandler().getBoolean("General.saveDataTask.hideLogMessages") == false) {
					Inv.log.info("Saving online players data...");
				}
				for (Player p : onlinePlayers) {
					if (p.isOnline() == true) {
						saveData(p);
					}
				}
				if (m.getConfigHandler().getBoolean("General.saveDataTask.hideLogMessages") == false) {
					Inv.log.info("Data save complete for " + onlinePlayers.size() + " players.");
				}
				onlinePlayers.clear();
			}
		}
	}
	
	public void saveData(Player p) {
		if (m.playersSync.contains(p.getName()) == true) {
			Inventory inventory = p.getInventory();
			ItemStack[] armor = p.getInventory().getArmorContents();
			
			if (m.getConfigHandler().getBoolean("General.syncArmorEnabled") == true) {
				if (m.useProtocolLib == true && m.getConfigHandler().getBoolean("General.enableModdedItemsSupport") == true) {
					m.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.saveModdedStacksData(inventory.getContents()), InventoryUtils.saveModdedStacksData(armor));
				} else {
					m.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.itemStackArrayToBase64(inventory.getContents()), InventoryUtils.itemStackArrayToBase64(armor));
				}
			} else {
				if (m.useProtocolLib == true && m.getConfigHandler().getBoolean("General.enableModdedItemsSupport") == true) {
					m.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.saveModdedStacksData(inventory.getContents()), "none");
				} else {
					m.getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.itemStackArrayToBase64(inventory.getContents()), "none");
				}
			}
		}
	}
	
	public void onShutDownDataSave() {
		Inv.log.info("Saving online players data...");
		List<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		
		for (Player p : onlinePlayers) {
			if (p.isOnline() == true) {
				saveData(p);
			}
		}
		Inv.log.info("Data save complete for " + onlinePlayers.size() + " players.");
	}

}
