package net.craftersland.bridge.inventory;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
						m.getInventoryDataHandler().onDataSaveFunction(p, false, "false", null, null);
					}
				}
				if (m.getConfigHandler().getBoolean("General.saveDataTask.hideLogMessages") == false) {
					Inv.log.info("Data save complete for " + onlinePlayers.size() + " players.");
				}
				onlinePlayers.clear();
			}
		}
	}
	
	public void onShutDownDataSave() {
		Inv.log.info("Saving online players data...");
		List<Player> onlinePlayers = new ArrayList<Player>(Bukkit.getOnlinePlayers());
		
		for (Player p : onlinePlayers) {
			if (p.isOnline() == true) {
				m.getInventoryDataHandler().onDataSaveFunction(p, false, "true", null, null);
			}
		}
		Inv.log.info("Data save complete for " + onlinePlayers.size() + " players.");
	}

}
