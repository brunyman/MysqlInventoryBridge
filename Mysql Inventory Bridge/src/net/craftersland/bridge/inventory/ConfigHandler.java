package net.craftersland.bridge.inventory;

import java.io.File;

public class ConfigHandler {
	
	private Inv inv;
	
	public ConfigHandler(Inv inv) {
		this.inv = inv;
		
		if (!(new File("plugins"+System.getProperty("file.separator")+"MysqlInventoryBridge"+System.getProperty("file.separator")+"config.yml").exists())) {
			Inv.log.info("No config file found! Creating new one...");

			inv.saveDefaultConfig();
		}
		try {

			inv.getConfig().load(new File("plugins"+System.getProperty("file.separator")+"MysqlInventoryBridge"+System.getProperty("file.separator")+"config.yml"));
		} catch (Exception e) {
			Inv.log.info("Could not load config file!");
			e.printStackTrace();
		}
	}
	
	public String getString(String key) {
		if (!inv.getConfig().contains(key)) {
			inv.getLogger().severe("Could not locate '"+key+"' in the config.yml inside of the MysqlInventoryBridge folder! (Try generating a new one by deleting the current)");
			return "errorCouldNotLocateInConfigYml:"+key;
		} else {
			return inv.getConfig().getString(key);
		}
	}

}
