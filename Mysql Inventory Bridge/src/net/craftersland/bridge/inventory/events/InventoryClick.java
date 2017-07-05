package net.craftersland.bridge.inventory.events;

import net.craftersland.bridge.inventory.Inv;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClick implements Listener {
	
	private Inv pd;
	
	public InventoryClick(Inv pd) {
		this.pd = pd;
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (pd.playersSync.contains(event.getWhoClicked().getName()) == false) {
			event.setCancelled(true);
		}
	}

}
