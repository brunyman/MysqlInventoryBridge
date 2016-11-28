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
	
	public InvMysqlInterface(Inv inv) {
		this.inv = inv;
	}
	
	public boolean hasAccount(UUID player) {
		Connection conn = inv.getDatabaseManager().getConnection();
		PreparedStatement preparedUpdateStatement = null;
		ResultSet result = null;
		try {	 
	        String sql = "SELECT `player_uuid` FROM `" + inv.getConfigHandler().getString("database.mysql.tableName") + "` WHERE `player_uuid` = ?";
	        preparedUpdateStatement = conn.prepareStatement(sql);
	        preparedUpdateStatement.setString(1, player.toString());
	        result = preparedUpdateStatement.executeQuery();
	        while (result.next()) {
	        	return true;
	        }
	      } catch (SQLException e) {
	    	  Inv.log.severe("Error: " + e.getMessage());
	      } finally {
	    	  try {
	    		  if (result != null) {
		    			result.close();
		    		}
		    		if (preparedUpdateStatement != null) {
		    			preparedUpdateStatement.close();
		    		}
	    	  } catch (Exception e) {
	    		  e.printStackTrace();
	    	  }
	      }
	      return false;
	}
	
	public boolean createAccount(UUID uuid, Player player) {
		Connection conn = inv.getDatabaseManager().getConnection();
		PreparedStatement preparedStatement = null;
		try {			 
	        String sql = "INSERT INTO `" + inv.getConfigHandler().getString("database.mysql.tableName") + "`(`player_uuid`, `player_name`, `inventory`, `armor`, `last_seen`) " + "VALUES(?, ?, ?, ?, ?)";
	        preparedStatement = conn.prepareStatement(sql);
	        preparedStatement.setString(1, uuid.toString());
	        preparedStatement.setString(2, player.getName().toString() + "");
	        preparedStatement.setString(3, "none");
	        preparedStatement.setString(4, "none");
	        preparedStatement.setString(5, String.valueOf(System.currentTimeMillis()));
	        preparedStatement.executeUpdate();
	        return true;
	      } catch (SQLException e) {
	    	  Inv.log.severe("Error: " + e.getMessage());
	      } finally {
	    	  try {
	    		  if (preparedStatement != null) {
	    			  preparedStatement.close();
	    		  }
	    	  } catch (Exception e) {
	    		  e.printStackTrace();
	    	  }
	      }
		return false;
	}
	
	public boolean setInventory(UUID uuid, Player player, String inventory, String armor) {
		if (!hasAccount(uuid)) {
			createAccount(uuid, player);
		}
		Connection conn = inv.getDatabaseManager().getConnection();
		PreparedStatement preparedUpdateStatement = null;
        try {        	
			String updateSqlExp = "UPDATE `" + inv.getConfigHandler().getString("database.mysql.tableName") + "` " + "SET `player_name` = ?" + ", `inventory` = ?" + ", `armor` = ?" + ", `last_seen` = ?" + " WHERE `player_uuid` = ?";
			preparedUpdateStatement = conn.prepareStatement(updateSqlExp);
			preparedUpdateStatement.setString(1, player.getName().toString() + "");
			preparedUpdateStatement.setString(2, inventory + "");
			preparedUpdateStatement.setString(3, armor + "");
			preparedUpdateStatement.setString(4, String.valueOf(System.currentTimeMillis()));
			preparedUpdateStatement.setString(5, uuid.toString() + "");
			preparedUpdateStatement.executeUpdate();
			return true;
		} catch (SQLException e) {
			Inv.log.severe("Error: " + e.getMessage());
		} finally {
	    	  try {
	    		  if (preparedUpdateStatement != null) {
	    			  preparedUpdateStatement.close();
	    		  }
	    	  } catch (Exception e) {
	    		  e.printStackTrace();
	    	  }
	      }
        return false;
	}
	
	public String getInventory(UUID uuid) {
		if (!hasAccount(uuid)) {
			createAccount(uuid, null);
		}
		Connection conn = inv.getDatabaseManager().getConnection();
		PreparedStatement preparedUpdateStatement = null;
		ResultSet result = null;
	      try {	 
	        String sql = "SELECT `inventory` FROM `" + inv.getConfigHandler().getString("database.mysql.tableName") + "` WHERE `player_uuid` = ?";
	        preparedUpdateStatement = conn.prepareStatement(sql);
	        preparedUpdateStatement.setString(1, uuid.toString());
	        result = preparedUpdateStatement.executeQuery();
	        while (result.next()) {
	        	return result.getString("inventory");
	        }
	      } catch (SQLException e) {
	    	  Inv.log.severe("Error: " + e.getMessage());
	      } finally {
	    	  try {
	    		  if (result != null) {
		    			result.close();
		    		}
		    		if (preparedUpdateStatement != null) {
		    			preparedUpdateStatement.close();
		    		}
	    	  } catch (Exception e) {
	    		  e.printStackTrace();
	    	  }
	      }
		return null;
	}
	
	public String getArmor(UUID uuid) {
		if (!hasAccount(uuid)) {
			createAccount(uuid, null);
		}
		Connection conn = inv.getDatabaseManager().getConnection();
		PreparedStatement preparedUpdateStatement = null;
		ResultSet result = null;
	      try {	 
	        String sql = "SELECT `armor` FROM `" + inv.getConfigHandler().getString("database.mysql.tableName") + "` WHERE `player_uuid` = ?";
	        preparedUpdateStatement = conn.prepareStatement(sql);
	        preparedUpdateStatement.setString(1, uuid.toString());
	        result = preparedUpdateStatement.executeQuery();
	        while (result.next()) {
	        	return result.getString("armor");
	        }
	      } catch (SQLException e) {
	    	  Inv.log.severe("Error: " + e.getMessage());
	      } finally {
	    	  try {
	    		  if (result != null) {
		    			result.close();
		    		}
		    		if (preparedUpdateStatement != null) {
		    			preparedUpdateStatement.close();
		    		}
	    	  } catch (Exception e) {
	    		  e.printStackTrace();
	    	  }
	      }
		return null;
	}

}
