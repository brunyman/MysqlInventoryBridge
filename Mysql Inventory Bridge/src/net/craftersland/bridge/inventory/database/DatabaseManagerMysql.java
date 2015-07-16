package net.craftersland.bridge.inventory.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import net.craftersland.bridge.inventory.Inv;

public class DatabaseManagerMysql {
	
	public Connection conn = null;
	private String tableName = "meb_inventory";
	
	// Hostname
	  private String dbHost;
	 
	  // Port -- Standard: 3306
	  private String dbPort;
	 
	  // Databankname
	  private String database;
	 
	  // Databank username
	  private String dbUser;
	 
	  // Databank password
	  private String dbPassword;

	private Inv inv;
	
	public DatabaseManagerMysql(Inv inv) {
		this.inv = inv;
		
		setupDatabase();
	}
	
	public boolean setupDatabase() {
		try {
       	 	//Load Drivers
            Class.forName("com.mysql.jdbc.Driver");
            
            dbHost = inv.getConfigHandler().getString("database.mysql.host");
            dbPort = inv.getConfigHandler().getString("database.mysql.port");
            database = inv.getConfigHandler().getString("database.mysql.databaseName");
            dbUser = inv.getConfigHandler().getString("database.mysql.user");
            dbPassword = inv.getConfigHandler().getString("database.mysql.password");
            
            String passFix = dbPassword.replaceAll("%", "%25");
            String passFix2 = passFix.replaceAll("\\+", "%2B");
            
            //Connect to database
            conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&" + "password=" + passFix2);
           
          } catch (ClassNotFoundException e) {
        	  Inv.log.severe("Could not locate drivers for mysql!");
            return false;
          } catch (SQLException e) {
        	  Inv.log.severe("Could not connect to mysql database!");
            return false;
          }
		
		//Create tables if needed
	      Statement query;
	      try {
	        query = conn.createStatement();
	        tableName = inv.getConfigHandler().getString("database.mysql.tableName");
	        
	        String accounts = "CREATE TABLE IF NOT EXISTS `" + tableName + "` (id int(10) AUTO_INCREMENT, player_uuid varchar(50) NOT NULL UNIQUE, player_name varchar(50) NOT NULL, inventory varchar(20000) NOT NULL, armor varchar(5000) NOT NULL, last_seen varchar(30) NOT NULL, PRIMARY KEY(id));";
	        query.executeUpdate(accounts);
	      } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	      }
	      Inv.log.info("Mysql has been set up!");
		return true;
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	
	public boolean closeDatabase() {
		try {
			conn.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean checkConnection() {
		try {
			if (conn == null) {
				return reConnect();
			}
			if (conn.isClosed() == true) {
				return reConnect();
			}
			if (!conn.isValid(3)) {
				return reConnect();
			}
			if (conn.isClosed() == true) {
				return reConnect();
			}
			return true;
		} catch (Exception e) {
			Inv.log.severe("Could not reconnect to Database!");
		}
		return false;
	}
	
	private boolean reConnect() {
		try {
			if (conn != null) conn.close();
			Inv.log.warning("MySQL database connection lost! Re-connecting...");
			//Load Drivers
            Class.forName("com.mysql.jdbc.Driver");
            
            dbHost = inv.getConfigHandler().getString("database.mysql.host");
            dbPort = inv.getConfigHandler().getString("database.mysql.port");
            database = inv.getConfigHandler().getString("database.mysql.databaseName");
            dbUser = inv.getConfigHandler().getString("database.mysql.user");
            dbPassword = inv.getConfigHandler().getString("database.mysql.password");
            
            String passFix = dbPassword.replaceAll("%", "%25");
            String passFix2 = passFix.replaceAll("\\+", "%2B");
            
            //Connect to database
            conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&" + "password=" + passFix2);
            Inv.log.info("Mysql re-connected succesful!");
            return true;
		} catch (Exception e) {
			Inv.log.severe("Could not re-connect to MySQL Database. Error: " + e.getMessage());
		}
		return false;
	}

}
