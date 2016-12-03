package com.smartmarmot.common;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.smartmarmot.dbforbix.config.Config;
import com.smartmarmot.dbforbix.zabbix.ZabbixItem;


//H2 File Database Example shows about storing the database contents into file system.


public class PersistentDB implements Persistence{
	private static PersistentDB instance; 

	private static final String DB_DRIVER = "org.h2.Driver";
	//private static final String DB_CONNECTION = "jdbc:h2:/opt/dbforbix/persistentdb";
	private static String DB_CONNECTION = "jdbc:h2:";
	private static final String DB_USER = "";
	private static final String DB_PASSWORD = "";
	private static final Logger	LOG	= Logger.getLogger(StackSingletonPersistent.class);
	private String DIRECTORY = Config.getInstance().getSPDir(); 

	private void init() throws ClassNotFoundException {
		Class.forName("org.h2.Driver");
		//DeleteDbFiles.execute("~", "test", true);
		
		Config config = Config.getInstance();
		if (DIRECTORY.startsWith("./"))
			DIRECTORY = DIRECTORY.replaceFirst(".", config.getBasedir());
		DIRECTORY.replace("//","/");
		
		
		File file = new File(DIRECTORY);
		if (!file.exists()) {
			LOG.log(Level.ERROR, "PersistentDB - Persistence directory "+DIRECTORY +" does not exists");
			boolean success = (new File(DIRECTORY)).mkdirs();
			if (success) {
				LOG.log(Level.INFO, "PersistentDB - Persistence directory "+DIRECTORY +" created");
			} else{
				LOG.log(Level.ERROR, "PersistentDB - Persistence directory "+DIRECTORY +" creation failed");
			}
		
		}
		String url = PersistentDB.DB_CONNECTION+DIRECTORY+"itemdb";
		PersistentDB.DB_CONNECTION=url+";TRACE_LEVEL_FILE=1;TRACE_MAX_FILE_SIZE=5";
		Connection conn;
		try {
			LOG.log(Level.INFO,"PersistentDB - initialization");
			conn = DriverManager.getConnection(url + ";LOG=0");
			Statement stat = conn.createStatement();
			stat.execute("CREATE TABLE IF NOT EXISTS ITEM(IDX BIGINT PRIMARY KEY, "
					+ "HOST VARCHAR(255), "
					+ "KEY VARCHAR(255), "
					+ "VALUE VARCHAR(4096),"
					+ "CLOCK BIGINT)");
			conn.commit();
			stat.close();
			conn.close();
			LOG.log(Level.INFO,"PersistentDB - initialization completed");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOG.log(Level.ERROR,"PersistentDB - initialization error:"+e.getMessage());
		}

	}



	public static synchronized PersistentDB getInstance() {
		if (instance == null) {
			instance = new PersistentDB();
			try {
				instance.init();
			} catch (ClassNotFoundException e) {
				LOG.log(Level.ERROR, "PersistentDB - initialization error:"+e.getMessage());
			}
		}
		return instance;
	}




	public  void push(ZabbixItem zItem) {
		Connection connection = getDBConnection();
		PreparedStatement insertPreparedStatement = null;
		PreparedStatement selectPreparedStatement = null;

		String InsertQuery = "INSERT INTO ITEM (IDX,HOST,KEY,VALUE,CLOCK) values" + "(?,?,?,?,?)";
		String SelectMaxIDX = "SELECT COALESCE(MAX(IDX),0) as IDX from ITEM";
		String SelectMaxQuery = "SELECT IDX,HOST,KEY,VALUE,CLOCK from ITEM where IDX=?";
		try {
			connection.setAutoCommit(false);
			selectPreparedStatement = connection.prepareStatement(SelectMaxIDX);
			ResultSet rs = selectPreparedStatement.executeQuery();
			Long maxIDX = 0L;
			while (rs.next()) {
				maxIDX = rs.getLong("IDX");

			}
			rs.close();

			if (maxIDX > 0L ){
				selectPreparedStatement = connection.prepareStatement(SelectMaxQuery);
				selectPreparedStatement.setLong(1, maxIDX);
				rs = selectPreparedStatement.executeQuery();
				while (rs.next()) {
					LOG.log(Level.DEBUG,"PersistentDB - Last item present in PersistentDB "+
							" IDX "+rs.getLong("IDX")+
							" Host="+rs.getString("HOST")+
							" Key="+rs.getString("KEY")+
							" Value="+rs.getString("VALUE")+
							" Clock="+rs.getLong("CLOCK"));
				}
			}
			rs.close();
			insertPreparedStatement = connection.prepareStatement(InsertQuery);
			insertPreparedStatement.setLong(1,maxIDX+1L);
			insertPreparedStatement.setString(2, zItem.getHost());
			insertPreparedStatement.setString(3, zItem.getKey());
			insertPreparedStatement.setString(4, zItem.getValue());
			insertPreparedStatement.setLong(5, zItem.getClock());
			insertPreparedStatement.executeUpdate();
			insertPreparedStatement.close();


			insertPreparedStatement.close();
			connection.commit();
			connection.close();

		} catch (SQLException e) {
			LOG.log(Level.ERROR, "PersistentDB - Error " + e.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				LOG.log(Level.ERROR, "PersistentDB - Error " + e.getMessage());
			}
		}
	}


	public  Long size() {
		Connection connection = getDBConnection();
		PreparedStatement preparedStmt = null;
		String SelectMaxIDX = "SELECT COALESCE(MAX(IDX),0) as IDX from ITEM";
		Long maxIDX = 0L;
		try {
			connection.setAutoCommit(false);
			preparedStmt = connection.prepareStatement(SelectMaxIDX);
			ResultSet rs = preparedStmt.executeQuery();
			while (rs.next()) {
				maxIDX = rs.getLong("IDX");

			}

			rs.close();
			connection.close();

		} catch (SQLException e) {
			LOG.log(Level.ERROR, "PersistentDB - Error " + e.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				LOG.log(Level.ERROR, "PersistentDB - Error " + e.getMessage());
			}
		}
		return maxIDX;
	}

	public  ZabbixItem pop() {
		Connection connection = getDBConnection();
		PreparedStatement preparedStmt = null;
		ZabbixItem zItem = null;


		String SelectMaxIDX = "SELECT COALESCE(MAX(IDX),0) as IDX from ITEM";
		String SelectMaxQuery = "SELECT IDX,HOST,KEY,VALUE,CLOCK from ITEM where IDX=?";
		String deleteIDX = "DELETE FROM ITEM WHERE IDX=?";
		try {
			connection.setAutoCommit(false);
			preparedStmt = connection.prepareStatement(SelectMaxIDX);
			ResultSet rs = preparedStmt.executeQuery();
			Long maxIDX = 0L;
			while (rs.next()) {
				maxIDX = rs.getLong("IDX");

			}
			rs.close();

			if (maxIDX > 0L ){
				preparedStmt = connection.prepareStatement(SelectMaxQuery);
				preparedStmt.setLong(1, maxIDX);
				rs = preparedStmt.executeQuery();
				rs.next();

				zItem= new ZabbixItem(rs.getString("HOST"), 
						rs.getString("KEY"), 
						rs.getString("VALUE"), 
						rs.getLong("CLOCK"));
				LOG.log(Level.DEBUG, "PersistentDB - Last Item in database"+
						" Host="+ zItem.getHost()+
						" Key="+ zItem.getKey()+
						" Value="+ zItem.getValue()+
						" Clock="+ zItem.getClock().toString());
				rs.close();
				preparedStmt = connection.prepareStatement(deleteIDX);
				preparedStmt.setLong(1, maxIDX);
				preparedStmt.executeUpdate();

			}

			preparedStmt.close();
			connection.commit();
			connection.close();
		} catch (SQLException e) {
			LOG.log(Level.ERROR, "PersistentDB - Error " + e.getLocalizedMessage());
		} catch (Exception e) {
			LOG.log(Level.ERROR, "PersistentDB - Error " + e.getMessage());
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				LOG.log(Level.ERROR, "PersistentDB - Error " + e.getMessage());
			}
		}
		return zItem;
	}




	private static Connection getDBConnection() {
		Connection dbConnection = null;
		try {
			Class.forName(DB_DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		try {
			dbConnection = DriverManager.getConnection(DB_CONNECTION, DB_USER,
					DB_PASSWORD);
			return dbConnection;
		} catch (SQLException e) {
			LOG.log(Level.ERROR, "PersistentDB - Error " + e.getMessage());
		}
		return dbConnection;
	}





	public static void main(String[] args) throws Exception {
		try {
			System.out.println("Size ="+PersistentDB.getInstance().size());
			ZabbixItem zi= new ZabbixItem ("host","key","value",0L);
			PersistentDB.getInstance().push(zi);
			zi= new ZabbixItem ("host1","key1","value1",1L);
			PersistentDB.getInstance().push(zi);
			zi= new ZabbixItem ("host2","key2","value2",2L);
			PersistentDB.getInstance().push(zi);
			zi= new ZabbixItem ("host3","key3","value3",3L);
			PersistentDB.getInstance().push(zi);
			System.out.println("Size ="+PersistentDB.getInstance().size());
			while (PersistentDB.getInstance().size() >0 ){
				zi=PersistentDB.getInstance().pop();
				System.out.println(" Host="+zi.getHost()+" Key="+zi.getKey()+" Value="+zi.getValue()+" Clock="+zi.getClock());
			}


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}	

