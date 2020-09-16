package net.craftersland.bridge.inventory;

import java.util.logging.Logger;

import net.craftersland.bridge.inventory.database.InvMysqlInterface;
import net.craftersland.bridge.inventory.database.MysqlSetup;
import net.craftersland.bridge.inventory.events.DropItem;
import net.craftersland.bridge.inventory.events.InventoryClick;
import net.craftersland.bridge.inventory.events.PlayerJoin;
import net.craftersland.bridge.inventory.events.PlayerQuit;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Inv extends JavaPlugin {
	
	public static Logger log;
	public boolean useProtocolLib = false;
	public static String pluginName = "MysqlInventoryBridge";
	//public Set<String> playersSync = new HashSet<String>();
	public static boolean is19Server = true;
	public static boolean is13Server = false;
	public static boolean isDisabling = false;
	
	private static ConfigHandler configHandler;
	private static SoundHandler sH;
	private static MysqlSetup databaseManager;
	private static InvMysqlInterface invMysqlInterface;
	private static InventoryDataHandler idH;
	private static BackgroundTask bt;
	
	@Override
    public void onEnable() {
		log = getLogger();
		getMcVersion();
    	configHandler = new ConfigHandler(this);
    	sH = new SoundHandler(this);
    	checkDependency();
    	bt = new BackgroundTask(this);
    	databaseManager = new MysqlSetup(this);
    	invMysqlInterface = new InvMysqlInterface(this);
    	idH = new InventoryDataHandler(this);
    	//Register Listeners
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerJoin(this), this);
    	pm.registerEvents(new PlayerQuit(this), this);
    	pm.registerEvents(new DropItem(this), this);
    	pm.registerEvents(new InventoryClick(this), this);
    	log.info(pluginName + " loaded successfully!");
	}
	
	@Override
    public void onDisable() {
		isDisabling = true;
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		if (databaseManager.getConnection() != null) {
			bt.onShutDownDataSave();
			databaseManager.closeConnection();
		}
		log.info(pluginName + " is disabled!");
	}
	
	public ConfigHandler getConfigHandler() {
		return configHandler;
	}
	public MysqlSetup getDatabaseManager() {
		return databaseManager;
	}
	public InvMysqlInterface getInvMysqlInterface() {
		return invMysqlInterface;
	}
	public SoundHandler getSoundHandler() {
		return sH;
	}
	public BackgroundTask getBackgroundTask() {
		return bt;
	}
	public InventoryDataHandler getInventoryDataHandler() {
		return idH;
	}
	
	private boolean getMcVersion() {
		String[] serverVersion = Bukkit.getBukkitVersion().split("-");
	    String version = serverVersion[0];
	    
	    if (version.matches("1.7.10") || version.matches("1.7.9") || version.matches("1.7.5") || version.matches("1.7.2") || version.matches("1.8.8") || version.matches("1.8.3") || version.matches("1.8.4") || version.matches("1.8")) {
	    	is19Server = false;
	    	return true;
	    } else if (version.matches("1.13") || version.matches("1.13.1") || version.matches("1.13.2")) {
	    	is13Server = true;
	    	return true;
	    } else if (version.matches("1.14") || version.matches("1.14.1") || version.matches("1.14.2") || version.matches("1.14.3") || version.matches("1.14.4")) {
	    	is13Server = true;
	    	return true;
	    } else if (version.matches("1.15") || version.matches("1.15.1") || version.matches("1.15.2")) {
	    	is13Server = true;
	    	return true;
	    } else if (version.matches("1.16") || version.matches("1.16.1") || version.matches("1.16.2") || version.matches("1.16.3")) {
	    	is13Server = true;
	    	return true;
	    }
	    return false;
	}
	
	private void checkDependency() {
		//Check dependency
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
        	useProtocolLib = true;
        	log.info("ProtocolLib dependency found.");
        } else {
        	useProtocolLib = false;
        	log.warning("ProtocolLib dependency not found. No support for modded items NBT data!");
        }
	}
}
