package net.craftersland.bridge.inventory;

import java.io.File;

public class ConfigHandler {
	
	private Inv inv;
	
	public ConfigHandler(Inv inv) {
		this.inv = inv;
		loadConfig();
	}
	
	public void loadConfig() {
		File pluginFolder = new File(inv.getDataFolder().getAbsolutePath());
		if (pluginFolder.exists() == false) {
    		pluginFolder.mkdir();
    	}
		File configFile = new File(inv.getDataFolder() + System.getProperty("file.separator") + "config.yml");
		if (configFile.exists() == false) {
			Inv.log.info("No config file found! Creating new one...");
			inv.saveDefaultConfig();
		}
    	try {
    		Inv.log.info("Loading the config file...");
    		inv.getConfig().load(configFile);
    	} catch (Exception e) {
    		Inv.log.severe("Could not load the config file! You need to regenerate the config! Error: " + e.getMessage());
			e.printStackTrace();
    	}
	}
	
	public String getString(String key) {
		if (!inv.getConfig().contains(key)) {
			inv.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Inv.pluginName + " folder! (Try generating a new one by deleting the current)");
			return "errorCouldNotLocateInConfigYml:" + key;
		} else {
			return inv.getConfig().getString(key);
		}
	}
	
	public String getStringWithColor(String key) {
		if (!inv.getConfig().contains(key)) {
			inv.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Inv.pluginName + " folder! (Try generating a new one by deleting the current)");
			return "errorCouldNotLocateInConfigYml:" + key;
		} else {
			return inv.getConfig().getString(key).replaceAll("&", "§");
		}
	}
	
	public Integer getInteger(String key) {
		if (!inv.getConfig().contains(key)) {
			inv.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Inv.pluginName + " folder! (Try generating a new one by deleting the current)");
			return null;
		} else {
			return inv.getConfig().getInt(key);
		}
	}
	
	public Boolean getBoolean(String key) {
		if (!inv.getConfig().contains(key)) {
			inv.getLogger().severe("Could not locate " + key + " in the config.yml inside of the " + Inv.pluginName + " folder! (Try generating a new one by deleting the current)");
			return null;
		} else {
			return inv.getConfig().getBoolean(key);
		}
	}

}
