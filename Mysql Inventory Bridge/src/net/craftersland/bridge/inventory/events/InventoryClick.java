package net.craftersland.bridge.inventory.events;

import net.craftersland.bridge.inventory.Inv;

import org.bukkit.entity.Player;
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
		Player p = (Player) event.getWhoClicked();
		if (pd.getInventoryDataHandler().isSyncComplete(p) == false) {
			event.setCancelled(true);
		}
	}

}
