package net.craftersland.bridge.inventory;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.logging.Logger;

import net.craftersland.bridge.inventory.database.DatabaseManagerMysql;
import net.craftersland.bridge.inventory.database.InvMysqlInterface;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Inv extends JavaPlugin {
	
	public static Logger log;
	public boolean useProtocolLib;
	public HashMap<String, Boolean> playersSync = new HashMap<String, Boolean>();
	
	private ConfigHandler configHandler;
	private DatabaseManagerMysql databaseManager;
	private InvMysqlInterface invMysqlInterface;
	private boolean enabled = false;
	
	@Override
    public void onEnable() {
		log = getLogger();
		log.info("Loading MysqlInventoryBridge v"+getDescription().getVersion()+"... ");
		
		//Load Configuration
    	configHandler = new ConfigHandler(this);
    	
    	//Setup Database
    	log.info("Using MySQL as Datasource...");
    	databaseManager = new DatabaseManagerMysql(this);
    	invMysqlInterface = new InvMysqlInterface(this);
		
		//Create MysqlExperienceBridge folder
    	(new File("plugins"+System.getProperty("file.separator")+"MysqlInventoryBridge")).mkdir();
    	
    	if (databaseManager.getConnection() == null)
    	{
    		getServer().getPluginManager().disablePlugin(this);
            return;
    	}
    	
    	//Check dependency
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
        	useProtocolLib = true;
        	log.info("ProtocolLib dependency found.");
        } else {
        	useProtocolLib = false;
        	log.warning("ProtocolLib dependency not found. No support for modded items NBT data!");
        }
    	
        if (getConfigHandler().getString("database.maintenance.enabled").matches("true")) {
    		runMaintenance();
    	}
        
    	//Register Listeners
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerHandler(this), this);
    	
    	enabled = true;
    	log.info("MysqlInventoryBridge has been successfully loaded!");
	}
	
	@Override
    public void onDisable() {
		if (enabled == true) {
			//Closing database connection
			if (databaseManager.getConnection() != null) {
				savePlayerData();
				log.info("Closing MySQL connection...");
				databaseManager.closeConnection();
			}
		}
		log.info("MysqlInventoryBridge has been disabled");
	}
	
	public ConfigHandler getConfigHandler() {
		return configHandler;
	}
	
	public DatabaseManagerMysql getDatabaseManager() {
		return databaseManager;
	}
	
	public InvMysqlInterface getInvMysqlInterface() {
		return invMysqlInterface;
	}
	
    public void runMaintenance() {
		
		Bukkit.getScheduler().runTaskLaterAsynchronously(this, new Runnable() {
			@Override
			public void run() {
				if (databaseManager.getConnection() == null) return;
				getDatabaseManager().checkConnection();
				log.info("Database maintenance task started...");
				
				long inactivityDays = Long.parseLong(getConfigHandler().getString("database.maintenance.inactivity"));
				long inactivityMils = inactivityDays * 24 * 60 * 60 * 1000;
				long curentTime = System.currentTimeMillis();
				long inactiveTime = curentTime - inactivityMils;
				String tableName = getConfigHandler().getString("database.mysql.tableName");
				
				try {
					String sql = "DELETE FROM `" + tableName + "` WHERE `last_seen` <?";
					PreparedStatement preparedStatement = databaseManager.getConnection().prepareStatement(sql);
					preparedStatement.setString(1, String.valueOf(inactiveTime));
					
					preparedStatement.executeUpdate();
				} catch (Exception e) {
					log.severe("Error: " + e.getMessage());
				}
				
				log.info("Database maintenance task ended.");
			}
		}, 400L);	
	}
    
    private void savePlayerData() {
    	if (Bukkit.getOnlinePlayers().isEmpty() == true) return;
		if (databaseManager.checkConnection() == false) return;
		log.info("Saving players data...");
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (playersSync.containsKey(p.getName()) == false) return;
			if (useProtocolLib == true && getConfigHandler().getString("General.enableModdedItemsSupport").matches("true")) {
				getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.saveModdedStacksData(p.getInventory().getContents()), InventoryUtils.saveModdedStacksData(p.getInventory().getArmorContents()));
			} else {
				getInvMysqlInterface().setInventory(p.getUniqueId(), p, InventoryUtils.itemStackArrayToBase64(p.getInventory().getContents()), InventoryUtils.itemStackArrayToBase64(p.getInventory().getArmorContents()));
			}
		}
    }

}
