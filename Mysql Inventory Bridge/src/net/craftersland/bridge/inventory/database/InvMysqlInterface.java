package net.craftersland.bridge.inventory.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.craftersland.bridge.inventory.Inv;

public class InvMysqlInterface {
	
	private Inv inv;
	private Connection conn;
	private String tableName = "meb_inventory";
	
	public InvMysqlInterface(Inv inv) {
		this.inv = inv;
		this.conn = ((DatabaseManagerMysql)inv.getDatabaseManager()).getConnection();
	}
	
	public boolean hasAccount(UUID player) {
		inv.getDatabaseManager().checkConnection();
		try {
	    	  tableName = inv.getConfigHandler().getString("database.mysql.tableName");
	 
	        String sql = "SELECT `player_uuid` FROM `" + tableName + "` WHERE `player_uuid` = ?";
	        PreparedStatement preparedUpdateStatement = conn.prepareStatement(sql);
	        preparedUpdateStatement.setString(1, player.toString());
	        
	        
	        ResultSet result = preparedUpdateStatement.executeQuery();
	 
	        while (result.next()) {
	        	return true;
	        }
	      } catch (SQLException e) {
	    	  Inv.log.severe("Error: " + e.getMessage());
	      }
	      return false;
	}
	
	public boolean createAccount(UUID uuid, Player player) {
		inv.getDatabaseManager().checkConnection();
		try {
			tableName = inv.getConfigHandler().getString("database.mysql.tableName");
			 
	        String sql = "INSERT INTO `" + tableName + "`(`player_uuid`, `player_name`, `inventory`, `armor`, `last_seen`) " + "VALUES(?, ?, ?, ?, ?)";
	        PreparedStatement preparedStatement = conn.prepareStatement(sql);
	        
	        preparedStatement.setString(1, uuid.toString());
	        preparedStatement.setString(2, player.getName().toString() + "");
	        preparedStatement.setString(3, "none");
	        preparedStatement.setString(4, "none");
	        preparedStatement.setString(5, String.valueOf(System.currentTimeMillis()));
	        
	        preparedStatement.executeUpdate();
	        return true;
	      } catch (SQLException e) {
	    	  Inv.log.severe("Error: " + e.getMessage());
	      }
		return false;
	}
	
	public boolean setInventory(UUID uuid, Player player, String inventory, String armor) {
		if (!hasAccount(uuid)) {
			createAccount(uuid, player);
		}
		
        try {
        	tableName = inv.getConfigHandler().getString("database.mysql.tableName");
        	
			String updateSqlExp = "UPDATE `" + tableName + "` " + "SET `player_name` = ?" + ", `inventory` = ?" + ", `armor` = ?" + ", `last_seen` = ?" + " WHERE `player_uuid` = ?";
			PreparedStatement preparedUpdateStatement = conn.prepareStatement(updateSqlExp);
			preparedUpdateStatement.setString(1, player.getName().toString() + "");
			preparedUpdateStatement.setString(2, inventory + "");
			preparedUpdateStatement.setString(3, armor + "");
			preparedUpdateStatement.setString(4, String.valueOf(System.currentTimeMillis()));
			preparedUpdateStatement.setString(5, uuid.toString() + "");
			
			preparedUpdateStatement.executeUpdate();
			return true;
		} catch (SQLException e) {
			Inv.log.severe("Error: " + e.getMessage());
		}
        return false;
	}
	
	public String getInventory(UUID uuid) {
		if (!hasAccount(uuid)) {
			createAccount(uuid, null);
		}
		
	      try {
	    	  tableName = inv.getConfigHandler().getString("database.mysql.tableName");
	 
	        String sql = "SELECT `inventory` FROM `" + tableName + "` WHERE `player_uuid` = ?";
	        
	        PreparedStatement preparedUpdateStatement = conn.prepareStatement(sql);
	        preparedUpdateStatement.setString(1, uuid.toString());
	        ResultSet result = preparedUpdateStatement.executeQuery();
	 
	        while (result.next()) {
	        	return result.getString("inventory");
	        }
	      } catch (SQLException e) {
	    	  Inv.log.severe("Error: " + e.getMessage());
	      }
		return null;
	}
	
	public String getArmor(UUID uuid) {
		if (!hasAccount(uuid)) {
			createAccount(uuid, null);
		}
		
	      try {
	    	  tableName = inv.getConfigHandler().getString("database.mysql.tableName");
	 
	        String sql = "SELECT `armor` FROM `" + tableName + "` WHERE `player_uuid` = ?";
	        
	        PreparedStatement preparedUpdateStatement = conn.prepareStatement(sql);
	        preparedUpdateStatement.setString(1, uuid.toString());
	        ResultSet result = preparedUpdateStatement.executeQuery();
	 
	        while (result.next()) {
	        	return result.getString("armor");
	        }
	      } catch (SQLException e) {
	    	  Inv.log.severe("Error: " + e.getMessage());
	      }
		return null;
	}

}
