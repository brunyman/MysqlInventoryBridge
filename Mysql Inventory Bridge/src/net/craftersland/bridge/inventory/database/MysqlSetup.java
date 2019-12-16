package net.craftersland.bridge.inventory.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import net.craftersland.bridge.inventory.Inv;

import org.bukkit.Bukkit;

public class MysqlSetup {
	
	private Connection conn = null;
	private Inv eco;
	
	public MysqlSetup(Inv eco) {
		this.eco = eco;
		connectToDatabase();
		setupDatabase();
		updateTables();
		databaseMaintenanceTask();
	}
	
	public void connectToDatabase() {
		Inv.log.info("Connecting to the database...");
		try {
       	 	//Load Drivers
            Class.forName("com.mysql.jdbc.Driver");
            Properties properties = new Properties();
            properties.setProperty("user", eco.getConfigHandler().getString("database.mysql.user"));
            properties.setProperty("password", eco.getConfigHandler().getString("database.mysql.password"));
            properties.setProperty("autoReconnect", "true");
            properties.setProperty("verifyServerCertificate", "false");
            properties.setProperty("useSSL", eco.getConfigHandler().getString("database.mysql.sslEnabled"));
            properties.setProperty("requireSSL", eco.getConfigHandler().getString("database.mysql.sslEnabled"));
            //Connect to database
            conn = DriverManager.getConnection("jdbc:mysql://" + eco.getConfigHandler().getString("database.mysql.host") + ":" + eco.getConfigHandler().getString("database.mysql.port") + "/" + eco.getConfigHandler().getString("database.mysql.databaseName"), properties);
           
          } catch (ClassNotFoundException e) {
        	  Inv.log.severe("Could not locate drivers for mysql! Error: " + e.getMessage());
            return;
          } catch (SQLException e) {
        	  Inv.log.severe("Could not connect to mysql database! Error: " + e.getMessage());
            return;
          }
		Inv.log.info("Database connection successful!");
	}
	
	public void setupDatabase() {
		if (conn != null) {
			PreparedStatement query = null;
		      try {	        
		        String data = "CREATE TABLE IF NOT EXISTS `" + eco.getConfigHandler().getString("database.mysql.tableName") + "` (id int(10) AUTO_INCREMENT, player_uuid char(36) NOT NULL UNIQUE, player_name varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL, inventory LONGTEXT NOT NULL, armor LONGTEXT NOT NULL, sync_complete varchar(5) NOT NULL, last_seen char(13) NOT NULL, PRIMARY KEY(id));";
		        query = conn.prepareStatement(data);
		        query.execute();
		      } catch (SQLException e) {
		        e.printStackTrace();
		        Inv.log.severe("Error creating tables! Error: " + e.getMessage());
		      } finally {
		    	  try {
		    		  if (query != null) {
		    			  query.close();
		    		  }
		    	  } catch (Exception e) {
		    		  e.printStackTrace();
		    	  }
		      }
		}
	}
	
	public Connection getConnection() {
		checkConnection();
		return conn;
	}
	
	public void checkConnection() {
		try {
			if (conn == null) {
				Inv.log.warning("Connection failed. Reconnecting...");
				reConnect();
			}
			if (!conn.isValid(3)) {
				Inv.log.warning("Connection is idle or terminated. Reconnecting...");
				reConnect();
			}
			if (conn.isClosed() == true) {
				Inv.log.warning("Connection is closed. Reconnecting...");
				reConnect();
			}
		} catch (Exception e) {
			Inv.log.severe("Could not reconnect to Database! Error: " + e.getMessage());
		}
	}
	
	public boolean reConnect() {
		try {            
            long start = 0;
			long end = 0;
			
		    start = System.currentTimeMillis();
		    Inv.log.info("Attempting to establish a connection to the MySQL server!");
            Class.forName("com.mysql.jdbc.Driver");
            Properties properties = new Properties();
            properties.setProperty("user", eco.getConfigHandler().getString("database.mysql.user"));
            properties.setProperty("password", eco.getConfigHandler().getString("database.mysql.password"));
            properties.setProperty("autoReconnect", "true");
            properties.setProperty("verifyServerCertificate", "false");
            properties.setProperty("useSSL", eco.getConfigHandler().getString("database.mysql.sslEnabled"));
            properties.setProperty("requireSSL", eco.getConfigHandler().getString("database.mysql.sslEnabled"));
            //properties.setProperty("useUnicode", "true");
            //properties.setProperty("characterEncoding", "utf8");
            //properties.setProperty("characterSetResults", "utf8");
            //properties.setProperty("connectionCollation", "utf8mb4_unicode_ci");
            conn = DriverManager.getConnection("jdbc:mysql://" + eco.getConfigHandler().getString("database.mysql.host") + ":" + eco.getConfigHandler().getString("database.mysql.port") + "/" + eco.getConfigHandler().getString("database.mysql.databaseName"), properties);
		    end = System.currentTimeMillis();
		    Inv.log.info("Connection to MySQL server established!");
		    Inv.log.info("Connection took " + ((end - start)) + "ms!");
            return true;
		} catch (Exception e) {
			Inv.log.severe("Error re-connecting to the database! Error: " + e.getMessage());
			return false;
		}
	}
	
	public void closeConnection() {
		try {
			Inv.log.info("Closing database connection...");
			conn.close();
			conn = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void updateTables() {
		if (conn != null) {
			DatabaseMetaData md = null;
	    	ResultSet rs1 = null;
	    	ResultSet rs2 = null;
	    	ResultSet rs3 = null;
	    	PreparedStatement query1 = null;
	    	PreparedStatement query2 = null;
	    	PreparedStatement query3 = null;
			try {
				md = conn.getMetaData();
				rs1 = md.getColumns(null, null, eco.getConfigHandler().getString("database.mysql.tableName"), "inventory");
	            if (rs1.next()) {
	            	if (rs1.getString("TYPE_NAME").matches("VARCHAR")) {
	            		String data = "ALTER TABLE `" + eco.getConfigHandler().getString("database.mysql.tableName") + "` MODIFY inventory LONGTEXT NOT NULL;";
				        query1 = conn.prepareStatement(data);
				        query1.execute();
	            	}
			    } else {
			        
			    }
	            rs2 = md.getColumns(null, null, eco.getConfigHandler().getString("database.mysql.tableName"), "armor");
	            if (rs2.next()) {
	            	if (rs2.getString("TYPE_NAME").matches("VARCHAR")) {
	            		String data = "ALTER TABLE `" + eco.getConfigHandler().getString("database.mysql.tableName") + "` MODIFY armor LONGTEXT NOT NULL;";
				        query2 = conn.prepareStatement(data);
				        query2.execute();
	            	}
			    } else {
			        
			    }
	            rs3 = md.getColumns(null, null, eco.getConfigHandler().getString("database.mysql.tableName"), "sync_complete");
	            if (rs3.next()) {
			    	
			    } else {
			        String data = "ALTER TABLE `" + eco.getConfigHandler().getString("database.mysql.tableName") + "` ADD sync_complete varchar(5) NOT NULL DEFAULT 'true';";
			        query3 = conn.prepareStatement(data);
			        query3.execute();
			    }
			} catch (Exception e) {
				Inv.log.severe("Error updating table! Error: " + e.getMessage());
			} finally {
	    		try {
	    			if (query1 != null) {
	    				query1.close();
	    			}
	    			if (rs1 != null) {
	    				rs1.close();
	    			}
	    			if (query2 != null) {
	    				query2.close();
	    			}
	    			if (rs2 != null) {
	    				rs2.close();
	    			}
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    		}
	    	}
		}
	}
	
	private void databaseMaintenanceTask() {
		if (eco.getConfigHandler().getBoolean("database.maintenance.enabled") == true) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(eco, new Runnable() {

				@Override
				public void run() {
					if (conn != null) {
						long inactivityDays = Long.parseLong(eco.getConfigHandler().getString("database.maintenance.inactivity"));
						long inactivityMils = inactivityDays * 24 * 60 * 60 * 1000;
						long curentTime = System.currentTimeMillis();
						long inactiveTime = curentTime - inactivityMils;
						Inv.log.info("Database maintenance task started...");
						PreparedStatement preparedStatement = null;
						try {
							String sql = "DELETE FROM `" + eco.getConfigHandler().getString("database.mysql.tableName") + "` WHERE `last_seen` < ?";
							preparedStatement = conn.prepareStatement(sql);
							preparedStatement.setString(1, String.valueOf(inactiveTime));
							preparedStatement.execute();
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								if (preparedStatement != null) {
									preparedStatement.close();
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						Inv.log.info("Database maintenance complete!");
					}
				}
				
			}, 100 * 20L);
		}
	}

}
