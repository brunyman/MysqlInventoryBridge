package net.craftersland.bridge.inventory;

import java.io.File;
import java.util.logging.Logger;

import net.craftersland.bridge.inventory.database.DatabaseManagerMysql;
import net.craftersland.bridge.inventory.database.InvMysqlInterface;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Inv extends JavaPlugin {
	
	public static Logger log;
	
	private ConfigHandler configHandler;
	private DatabaseManagerMysql databaseManager;
	private InvMysqlInterface invMysqlInterface;
	
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
    	
    	//Register Listeners
    	PluginManager pm = getServer().getPluginManager();
    	pm.registerEvents(new PlayerHandler(this), this);
    	
    	log.info("MysqlInventoryBridge has been successfully loaded!");
	}
	
	@Override
    public void onDisable() {
		if (this.isEnabled()) {
			//Closing database connection
			if (databaseManager.getConnection() != null) {
				log.info("Closing MySQL connection...");
				databaseManager.closeDatabase();
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

}
