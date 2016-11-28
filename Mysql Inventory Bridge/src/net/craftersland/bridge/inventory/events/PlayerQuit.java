package net.craftersland.bridge.inventory.events;

import net.craftersland.bridge.inventory.Inv;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {
	
	private Inv inv;
	
	public PlayerQuit(Inv inv) {
		this.inv = inv;
	}
	
	@EventHandler
	public void onDisconnect(final PlayerQuitEvent event) {
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(inv, new Runnable() {
			@Override
			public void run() {
				inv.getBackgroundTask().saveData(event.getPlayer());
				inv.playersSync.remove(event.getPlayer().getName());
			}
		}, 5L);
		
	}

}
