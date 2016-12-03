/*
 * This file is part of DBforBix.
 *
 * DBforBix is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DBforBix is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DBforBix. If not, see <http://www.gnu.org/licenses/>.
 */

package com.smartmarmot.dbforbix.config;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.smartmarmot.dbforbix.db.DBType;
import com.smartmarmot.dbforbix.zabbix.ZabbixSender.PROTOCOL;

public class Config {

	private interface Validable {

		public boolean isValid();
	}

	/**
	 * Zabbix server config entry
	 */
	public static class ZServer implements Validable {

		private String		host		= null;
		private int			port		= 10051;
		private PROTOCOL	protocol	= PROTOCOL.V14;



		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}

		public PROTOCOL getProtocol() {
			return protocol;
		}

		@Override
		public boolean isValid() {
			return (port > 0) && (host != null);
		}

		@Override
		public String toString() {
			return host + ":" + port;
		}
	}

	/**
	 * Monitored database config entry
	 */
	public static class Database implements Validable {

		private DBType	type;
		private String	name;
		private String	url;
		private String	user;
		private String	password;
		private String	instance;
		private Integer maxactive = new Integer(15);
		private Integer maxidle = new Integer(2);
		private Integer maxwaitmillis = new Integer (10000);
		private String  itemfile;
		private Boolean persistent = false;
		private int		queryTimeout;

		public DBType getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public String getURL() {
			return url;
		}


		public String getUser() {
			return user;
		}

		public String getPassword() {
			return password;
		}

		public String getInstance() {
			return instance;
		}

		@Override
		public boolean isValid() {
			return (name != null) && (url != null) && (user != null) && (password != null);
		}

		@Override
		public String toString() {
			return getType() + ":" + getURL() + " " + getInstance();
		}

		public Integer getMaxWaitMillis() {
			return maxwaitmillis;
		}

		public void setMaxWaitMillis(Integer maxwaitmillis) {
			this.maxwaitmillis = maxwaitmillis;
		}

		public Integer getMaxIdle() {
			return maxidle;
		}

		public void setMaxIdle(Integer maxidle) {
			this.maxidle = maxidle;
		}

		public String getItemFile() {
			return itemfile;
		}

		public void setItemfile(String itemfile) {
			if (!itemfile.endsWith(".xml")){
				LOG.warn("ItemFile do not end with .xml, adding the extension: "+itemfile+".xml");
				itemfile=itemfile+".xml";
			}	
			this.itemfile = itemfile;
		}

		public void setPersistence(String pers) {
			if (pers.equalsIgnoreCase("true")) {
				this.persistent = true;
			}
		}
		public Boolean getPersistence(){
			return this.persistent;
		}

		public void setMaxActive(Integer maxactive) {
			this.maxactive= maxactive;

		}

		public Integer getMaxActive() {
			return maxactive;
		}
		public int getQueryTimeout() {
			return queryTimeout;
		}

		public void setQueryTimeout(int queryTimeout) {
			this.queryTimeout = queryTimeout;
		}


	}

	private static final Logger		LOG				= Logger.getLogger(Config.class);

	private static Config			instance;

	private static final String		GLOBAL_NAME			= "DBforBix";
	private static final String		GLOBAL_POOL			= "Pool";
	private static final String		GLOBAL_ZBXSRV		= "ZabbixServer";
	private static final String		GLOBAL_DB			= "DB";

	private Map<String, ZServer>	zbxservers;
	private Map<String, Database>	databases;

	private String					basedir;

	private static final String		SET_LOGLEVEL	= GLOBAL_NAME + ".LogLevel";
	private Level					logLevel		= Level.WARN;

	private static final String		SET_LOGFILE	= GLOBAL_NAME + ".LogFile";
	private String					logFile			= "./logs/dbforbix.log";

	private static final String		SET_LOGFILESIZE	= GLOBAL_NAME + ".LogFileSize";
	private String    				logFileSize="1MB";

	private static final String		SET_PERSISTENCETYPE	= GLOBAL_NAME + ".PersistenceType";
	private String					persistenceType		= "DB";

	private static final String		SET_PERSISTENCEDIR = GLOBAL_NAME + ".PersistenceDir";
	private String					persistenceDir		 	= "./persistence/";

	private static final String		SET_POOL_MAXACTIVE	= GLOBAL_POOL + ".MaxActive";
	private int						maxActive		= 10;
	
	private static final String		SET_POOL_MAXIDLE	= GLOBAL_POOL + ".TimeOut";
	private int						maxIdle			= 15;

	private static final String 	SET_QUERY_TIMEOUT = GLOBAL_POOL+ ".QueryTimeOut";
	private int						queryTimeout= 60;
	
	private Config() {
		zbxservers = new HashMap<String, Config.ZServer>();
		databases = new HashMap<String, Config.Database>();
	}

	/**
	 * Get the system configuration
	 */
	public static Config getInstance() {
		if (instance == null)
			instance = new Config();
		return instance;
	}

	/**
	 * Reads the configuration from a properties file
	 * 
	 * @param file the file to read
	 * @throws IOException
	 */
	public void readConfig(String file) throws IOException {
		LOG.debug("Parsing config file: " + file);

		try (FileReader reader = new FileReader(file)){
			PropertiesConfiguration pcfg = new PropertiesConfiguration();
			pcfg.read(reader);
			if (pcfg.containsKey(SET_LOGLEVEL))
				logLevel = Level.toLevel(pcfg.getString(SET_LOGLEVEL), Level.INFO);
			
			if (pcfg.containsKey(SET_LOGFILE))
				logFile = pcfg.getString(SET_LOGFILE);
			
			if (pcfg.containsKey(SET_LOGFILESIZE))
				logFileSize = pcfg.getString(SET_LOGFILESIZE);

			if (pcfg.containsKey(SET_PERSISTENCETYPE))
				persistenceType = pcfg.getString(SET_PERSISTENCETYPE);
			
			if (pcfg.containsKey(SET_PERSISTENCEDIR))
				persistenceDir = pcfg.getString(SET_PERSISTENCEDIR);

			if (pcfg.containsKey(SET_POOL_MAXACTIVE))
				maxActive = pcfg.getInt(SET_POOL_MAXACTIVE);
			
			if (pcfg.containsKey(SET_POOL_MAXIDLE))
				maxIdle = pcfg.getInt(SET_POOL_MAXIDLE);

			if (pcfg.containsKey(SET_QUERY_TIMEOUT))
				queryTimeout  = Integer.parseInt(pcfg.getString(SET_QUERY_TIMEOUT));


			Iterator<?> it;
			it = pcfg.getKeys(GLOBAL_ZBXSRV);
			while (it.hasNext()) {
				String key = it.next().toString();
				String[] keyparts = key.split("\\.");
				if (keyparts.length == 3)
					readConfigZSRV(keyparts[0], keyparts[1], keyparts[2], pcfg.getString(key));
			}

			it = pcfg.getKeys(GLOBAL_DB);
			while (it.hasNext()) {
				String key = it.next().toString();
				String[] keyparts = key.split("\\.");
				if (keyparts.length == 3)
					readConfigDB(keyparts[0], keyparts[1], keyparts[2], pcfg.getString(key));
			}

		}
		catch (ConfigurationException e) {
			throw new IOException("Error in configuration: " + e.getLocalizedMessage(), e);
		}
	}

	/**
	 * Read configuration value as zabbix server config
	 */
	private void readConfigZSRV(String group, String name, String key, String value) {
		ZServer zsrv = zbxservers.get(name);
		if (zsrv == null)
			zsrv = new ZServer();
		if ("Address".equalsIgnoreCase(key))
			zsrv.host = value;
		else if ("Port".equalsIgnoreCase(key)) {
			try {
				zsrv.port = Integer.parseInt(value);
			}
			catch (NumberFormatException ex) {
				LOG.error("Could not parse port number", ex);
			}
		}
		else
			LOG.info("Invalid config item: " + group + "." + name + "." + key);
		zbxservers.put(name, zsrv);
	}


	private void readConfigDB(String group, String name, String key, String value) {
		Database dbsrv = databases.get(name);
		if (dbsrv == null)
			dbsrv = new Database();
		/* set defaults
		 */


		if ("Type".equalsIgnoreCase(key))
			dbsrv.type = DBType.fromString(value);
		else if ("Name".equalsIgnoreCase(key))
			dbsrv.name = value;
		else if ("Url".equalsIgnoreCase(key))
			dbsrv.url = value;
		else if ("Instance".equalsIgnoreCase(key))
			dbsrv.instance = value;
		else if ("User".equalsIgnoreCase(key))
			dbsrv.user = value;
		else if ("Password".equalsIgnoreCase(key))
			dbsrv.password = value;
		else if ("MaxWaitMillis".equalsIgnoreCase(key))
			dbsrv.setMaxWaitMillis(Integer.parseInt(value));
		else if ("MaxActive".equalsIgnoreCase(key))
			dbsrv.setMaxActive(Integer.parseInt(value));
		else if ("MaxIdle".equalsIgnoreCase(key))
			dbsrv.setMaxIdle(Integer.parseInt(value));
		else if ("ItemFile".equalsIgnoreCase(key))
			dbsrv.setItemfile(value);
		else if ("Persistence".equalsIgnoreCase(key))
			dbsrv.setPersistence(value);
		else
			LOG.info("Invalid config item: " + group + "." + name + "." + key);
		databases.put(name, dbsrv);
	}

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public String getBasedir() {
		return basedir;
	}

	public Level getLogLevel() {
		return logLevel;
	}

	public String getLogFile() {
		return logFile;
	}

	public String getSPDir() {
		return persistenceDir;
	}

	public String getSPType() {
		return persistenceType;
	}

	public String getLogFileSize() {
		return logFileSize;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	/**
	 * @return a list of all VALID zabbix server configurations
	 */
	public Collection<ZServer> getZabbixServers() {
		Collection<ZServer> validServers = zbxservers.values();
		CollectionUtils.filter(validServers, new Predicate <Config.ZServer>() {

			@Override
			public boolean evaluate(Config.ZServer object) {
				return ((ZServer) object).isValid();
			}
		});
		return validServers;
	}

	/**
	 * @return a list of all VALID database configurations
	 */
	public Collection<Database> getDatabases() {
		Collection<Database> validDatabases = databases.values();
		CollectionUtils.filter(validDatabases, new Predicate<Database>() {

			@Override
			public boolean evaluate(Database object) {
				return ((Database) object).isValid();
			}
		});
		return validDatabases;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Config:\n");
		builder.append("\n");
		builder.append("BaseDir:\t").append(getBasedir()).append("\n");
		builder.append("LogLevel:\t").append(getLogLevel()).append("\n");
		builder.append("LogFile:\t").append(getLogFile()).append("\n");
		builder.append("LogFileSize:\t").append(getLogFileSize()).append("\n");
		builder.append("PersistenceType:\t").append(getSPType()).append("\n");
		builder.append("PersistenceDir:\t").append(getSPDir()).append("\n");
		
		builder.append("\n");
		for (ZServer zsrv: zbxservers.values())
			builder.append("-- Zabbix:\t").append(zsrv).append("\n");
		for (Database db: databases.values())
			builder.append("-- Database:\t").append(db).append("\n");
		return builder.toString();
	}

	public int getMaxWait() {
		// TODO Auto-generated method stub
		return this.getMaxWait();
	}

	public int getMaxSize() {
		// TODO Auto-generated method stub
		return this.getMaxSize();
	}

	public int getQueryTimeout() {
		// TODO Auto-generated method stub
		return this.queryTimeout;
	}

}
