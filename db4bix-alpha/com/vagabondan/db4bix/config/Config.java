/*
 * This file is part of DB4bix.
 *
 * DB4bix is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DB4bix is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DB4bix. If not, see <http://www.gnu.org/licenses/>.
 */

package com.vagabondan.db4bix.config;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vagabondan.db4bix.db.DBManager;
import com.vagabondan.db4bix.db.DBType;
import com.vagabondan.db4bix.db.adapter.Adapter;
import com.vagabondan.db4bix.scheduler.Discovery;
import com.vagabondan.db4bix.scheduler.Item;
import com.vagabondan.db4bix.scheduler.MultiColumnItem;
import com.vagabondan.db4bix.scheduler.MultiRowItem;
import com.vagabondan.db4bix.scheduler.Scheduler;
import com.vagabondan.db4bix.scheduler.SimpleItem;
import com.vagabondan.db4bix.zabbix.ZabbixSender.PROTOCOL;

public class Config {
	
	private interface Validable {	
		public boolean isValid();
	}
	
	/**
	 * Zabbix server config entry
	 */
	public static class ZServer implements Validable {
		
		private String		zbxServerHost		= null;
		private int			zbxServerPort		= 10051;
		private String 		zbxServerNameFC		= null;
		private String		proxy		= null;
		private PROTOCOL	protocol	= PROTOCOL.V32;		
		private Collection<String> definedDBNames  = null;
		
		
		/**
		 * reinit 
		 */
		private Map<String,List<String> > hosts=null;
		private Map<String,List<String> > items=null;
		private Map<String,List<String> > hostmacro=null;
		private Map<String,List<String>> hostsTemplates=null;
		private Map<String,Map<String,String>> itemConfigs = new HashMap<>();
		private String hashZabbixConfig		=null;
		
		
		
		/////////////////////////////////////////////////////////
		public String getZbxServerNameFC() {
			return zbxServerNameFC;
		}

		public void setZbxServerNameFC(String zbxServerNameFC) {
			this.zbxServerNameFC = zbxServerNameFC;
		}
		
		
		public Map<String, Map<String, String>> getItemConfigs() {
			return itemConfigs;
		}
		
		public String getHashZabbixConfig() {
			return hashZabbixConfig;
		}
		
		public Map<String,List<String> > getHosts() {
			return hosts;
		}
		
		public Map<String,List<String> > getItems() {
			return items;
		}
		
		public Map<String,List<String> > getHostmacro() {
			return hostmacro;
		}		
		////////////////////////
		public void setHashZabbixConfig(String inStr) {
			this.hashZabbixConfig=inStr;
		}
		

		public void setItemConfigs(Map<String,Map<String,String>> itemConfigs) {
			this.itemConfigs=itemConfigs;
		}
		
		public void setHosts(Map<String,List<String> > hosts) {
			this.hosts = hosts;
		}		

		public void setItems(Map<String,List<String> > items) {
			this.items = items;
		}		

		public void setHostmacro(Map<String,List<String> > hostmacro) {
			this.hostmacro = hostmacro;
		}		
		//////////////////////////////////////////////////////////////////
		

		public Collection<String> getDefinedDBNames() {
			return definedDBNames;
		}
		
		public void setDefinedDBNames(Collection<String> definedDBNames) {
			this.definedDBNames = definedDBNames;
		}
		
		public String getZServerHost() {
			return zbxServerHost;
		}
		
		public int getZServerPort() {
			return zbxServerPort;
		}
		
		public PROTOCOL getProtocol() {
			return protocol;
		}
		
		@Override
		public boolean isValid() {
			return (zbxServerPort > 0) && (zbxServerHost != null);
		}
		
		@Override
		public String toString() {
			return zbxServerHost + ":" + zbxServerPort;
		}

		
		public String getProxyConfigRequest(){
			return new String("{\"request\":\"proxy config\",\"host\":\""+getProxy()+"\"}");
		}

		public String getProxy() {
			return proxy;
		}

		public void setProxy(String proxy) {
			this.proxy = proxy;
		}

		public void addItemConfig(String itemGroupName, Map<String, String> m) {
			itemConfigs.put(itemGroupName, m);
		}
		
		public void removeItemConfig(String itemGroupName) {
			itemConfigs.remove(itemGroupName);
		}

		/**
		 * 
		 * @return set of item group names of this Zabbix Server
		 */
		public Collection<String> getItemGroupNames() {			
			return itemConfigs.keySet();
		}

		/**
		 * 
		 * @param itemGroupName
		 * @return item config
		 * @throws NullPointerException
		 */
		public Map<String,String> getItemConfigByItemGroupName(String itemGroupName){
			return itemConfigs.get(itemGroupName);
		}
		
		
		
		public String getHostByHostId(String hostid) {
			String host=null;
			for(int hid=0;hid<hosts.get("hostid").size();++hid){
				if(hostid.equals(hosts.get("hostid").get(hid))){
					host=hosts.get("host").get(hid);
					break;
				}
			}
			return host;
		}

		private String getHostMacroValue(String hostid, String macro) {
			for(int hm=0;hm<hostmacro.get("hostid").size();++hm){
				if(hostmacro.get("hostid").get(hm).equals(hostid) 
					&& hostmacro.get("macro").get(hm).equals(macro)){
					return hostmacro.get("value").get(hm);
				}
			}
			return null;
		}
		
		private String getTemplateMacroValue(String hostid, String macro) {
			// TODO Check macro resolving method in Zabbix
			String result=null;
			/**
			 * hostmacro:
			 * "hostmacro":{"fields":["hostmacroid","hostid","macro","value"],"data":[[450,11082,"{$DSN}","APISQL"],[457,11084,"{$PERF_SCHEMA}","'performance_schema'"]]},
			 * 
			 * hosts_templates:
			 * "hosts_templates":{"fields":["hosttemplateid","hostid","templateid"],"data":[[2195,11082,11084]]},
			 * */
			Set<String> templateIds=new HashSet<>();			
			for(int hm=0;hm<hostsTemplates.get("hostid").size();++hm){
				if(hostsTemplates.get("hostid").get(hm).equals(hostid)){					
					templateIds.add(hostsTemplates.get("templateid").get(hm));
				}
			}
			for (String tid:templateIds){
				 result=getHostMacroValue(tid,macro);
				 if(null!=result) break;
			}
			return result;
		}

		
		

		public void setHostsTemplates(Map<String, List<String>> hostsTemplates) {
			this.hostsTemplates=hostsTemplates;			
		}

		public Map<String, List<String>> getHostsTemplates() {			
			return hostsTemplates;
		}

		public String getMacroValue(String hostid, String macro) {
			String result=null;
			result=getHostMacroValue(hostid,macro);
			if(null==result){
				result=this.getTemplateMacroValue(hostid, macro);
			}
			return result;
		}	
	}
	
	/**
	 * Monitored database config entry
	 */
	public static class Database implements Validable {
		
		private DBType	type;
		private String	nameFC;
		private String	url;
		private String	user;
		private String	password;
		private String	instance;
		private Integer maxactive = new Integer(15);
		private Integer maxidle = new Integer(2);
		private Integer maxwaitmillis = new Integer (10000);
		private Boolean persistent = false;
		private int		queryTimeout;
		private Set<String>  itemGroupNames=new HashSet<String>();
		
		public DBType getType() {
			return type;
		}
		
		public String getDBNameFC() {
			return nameFC;
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
			return (nameFC != null) && (url != null) && (user != null) && (password != null);
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

		public Set<String> getItemGroupNames() {
			return itemGroupNames;
		}

		public void addItemGroupName(String itemGroupName) {
			itemGroupNames.add(itemGroupName);
		}
		
		public void removeItemGroupName(String itemGroupName) {
			itemGroupNames.remove(itemGroupName);
		}

	}

	/**
	 * 
	 */
	private static final Logger		LOG				= Logger.getLogger(Config.class);
	private static final String		GLOBAL_NAME			= "DB4bix";
	private static final String		GLOBAL_POOL			= "Pool";
	private static final String		GLOBAL_ZBXSRV		= "ZabbixServer";
	private static final String		GLOBAL_DB			= "DB";
	private static final String		SET_LOGLEVEL	= GLOBAL_NAME + ".LogLevel";
	private static final String		SET_LOGFILE	= GLOBAL_NAME + ".LogFile";
	private static final String		SET_LOGFILESIZE	= GLOBAL_NAME + ".LogFileSize";
	private static final String		SET_UPDATECONFIG = GLOBAL_NAME + ".UpdateConfigTimeout";
	private static final String		SET_POOL_MAXACTIVE	= GLOBAL_POOL + ".MaxActive";
	private static final String		SET_POOL_MAXIDLE	= GLOBAL_POOL + ".TimeOut";
	private static final String 	SET_QUERY_TIMEOUT = GLOBAL_POOL+ ".QueryTimeOut";
	private static final String 	ZBX_HEADER_PREFIX="ZBXD\1";
	private static final String 	ZABBIX_ITEM_CONFIG_SUFFIX			= "DB4bix.config";

	/**
	 * singleton
	 */
	private static Config						instance;
	
	
	/**
	 * Config
	 */
	private String					basedir;	
	private String 					configFile						= null;
	
	
	/**
	 * 
	 */
	private String 					configFileHash					= null; 
	private Level					logLevel						= Level.WARN;
	private String					logFile							= "./logs/DB4bix.log";
	private String					sspDir		 					= "./temp/";
	private String					logFileSize						= "1MB";
	private int						maxActive						= 10;	
	private int						queryTimeout					= 60;	// default queryTimeout
	private int						maxIdle							= 15;	// pieces
	private int						updateConfigTimeout				= 120; 	// seconds
	
	
	/**
	 * 
	 * zbxServers:
	 * 1. Config.readConfigZSRV: -  Zabbix Server: 
	 *    zbxServerHost=Address, zbxServerPort=Port, ProxyName, DBList=dbNames - 
	 * 2. main: zbxSender - 
	 * 3. Config.getItemConfigFromZabbix: -  Zabbix Server,
	 * 4. Config.getItemConfigFromZabbix: itemsJSON, hosts, items, hostmacro, itemConfigs
	 * 5. 
	 * 
	 * databases:
	 * 1. readConfigDB: 
	 * 2. loadItemConfigFromZabbix: itemGroupName
	 * 2. main: add db to dbmanager
	 * 3. 
	 * 
	 * schedulers:
	 * 1. buildServerElements: itemGroupName, time, new Scheduler(time), scheduler.addItem(itemGroupName, item)
	 * 2. main: schedulers -> worktimers
	 * 3. 
	 */
	
	//itemGroupName -> ZServer
	//itemGroupName:ZServer = N:1
	private Set<ZServer>	zbxServers;
	
	//itemGroupName -> Database
	//itemGroupName:Database = N:1
	private Set<Database>	databases;
	
	//itemGroupName->time->Scheduler
	//itemGroupName:Scheduler = 1:N
	private Map<String, Map<Integer, Scheduler>> schedulers  = new HashMap<String,Map<Integer, Scheduler>>();
	
	//itemGroupName->Timer
	//itemGroupName:workTimer = 1:1
	private static Map<String,Timer> workTimers = new HashMap<String, Timer>();
	
	
	public Map<Integer, Scheduler> getSchedulersByItemGroupName(String itemGroupName) {
		if(!schedulers.containsKey(itemGroupName)) schedulers.put(itemGroupName,new HashMap<Integer,Scheduler>());
		return schedulers.get(itemGroupName);
	}

	public void addScheduler(String itemGroupName, HashMap<Integer, Scheduler> scheduler) {
		this.schedulers.put(itemGroupName,scheduler);
	}
	
	public void clearSchedulers(){
		for(Entry<String, Map<Integer, Scheduler>> s:schedulers.entrySet()){
			for(Scheduler sch:s.getValue().values()){
				sch.cancel();				
			}			
		}
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		schedulers.clear();
	}

	private Config() {
		zbxServers = new HashSet<Config.ZServer>();
		databases = new HashSet<Config.Database>();
	}
	
	/**
	 * reinit Config instance
	 * @return new Config instance
	 */
	public Config reset() {
		if (workTimers != null)
			for(Entry<String, Timer> element:workTimers.entrySet())	element.getValue().cancel();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		workTimers.clear();
		clearSchedulers();
		Config newconfig=new Config();
		newconfig.setBasedir(getBasedir());
		newconfig.setConfigFile(getConfigFile());
		instance=newconfig;
		return instance;
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
	 * calculates hash for config file
	 * @throws NullPointerException - if hash is null
	 */
	private void calculateFileConfigHash() throws NullPointerException {
		MessageDigest md = null;
		byte[] b=new byte[2048];
		try{
			md = MessageDigest.getInstance("MD5");
		}
		catch(NoSuchAlgorithmException e){
			LOG.error("Wrong algorithm provided while getting instance of MessageDigest: " + e.getMessage());
		}
		/**
		 * try with resources. Autoclosing after exitting try block
		 */
		try (InputStream is = Files.newInputStream(Paths.get(getConfigFile())); DigestInputStream dis = new DigestInputStream(is, md)){
		  while(dis.read(b)>=0);
		} catch (IOException e) {
			LOG.error("Something has happenned reading file: " + e.getMessage());
		}
		try{
			setFileConfigHash((new HexBinaryAdapter()).marshal(md.digest()));
		} catch(Exception e){
			LOG.error("Something has happenned converting md5 sum to string: " + e.getLocalizedMessage());
		}
		if(null==getFileConfigHash()) throw new NullPointerException("Hash for config file is null!");
	}
	
	/**
	 * Reads the configuration from a properties file
	 * 
	 * @throws IOException
	 */
	public void readFileConfig() throws IOException,NullPointerException {
		LOG.debug("Parsing config file: " + configFile);
				
		calculateFileConfigHash();
		
		try (FileReader reader = new FileReader(configFile)){
	     		PropertiesConfiguration pcfg = new PropertiesConfiguration();	     		
		     	pcfg.read(reader);		     	
		     	
		     	
			if (pcfg.containsKey(SET_LOGLEVEL))
				logLevel = Level.toLevel(pcfg.getString(SET_LOGLEVEL), Level.INFO);
			if (pcfg.containsKey(SET_LOGFILE))
				logFile = pcfg.getString(SET_LOGFILE);
			if (pcfg.containsKey(SET_LOGFILESIZE))
				setLogFileSize(pcfg.getString(SET_LOGFILESIZE));
			
			if (pcfg.containsKey(SET_UPDATECONFIG))
				setUpdateConfigTimeout(pcfg.getInt(SET_UPDATECONFIG));
			
			
			
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
		ZServer zsrv = getZServerByNameFC(name);
		if (zsrv == null) {
			zsrv = new ZServer();
			zsrv.setZbxServerNameFC(name);
			zbxServers.add(zsrv);
		}
		if ("Address".equalsIgnoreCase(key)) zsrv.zbxServerHost = value;
		else if ("Port".equalsIgnoreCase(key)) {
			try {
				zsrv.zbxServerPort = Integer.parseInt(value);
			}
			catch (NumberFormatException ex) {
				LOG.error("Could not parse zbxServerPort number", ex);
			}
		}
		else if("ProxyName".equalsIgnoreCase(key)) zsrv.proxy=value;		
		else if("DBList".equalsIgnoreCase(key)) zsrv.setDefinedDBNames((new ArrayList<String>(Arrays.asList(value.replaceAll("\\s","").toUpperCase().split(",")))));		
		else
			LOG.info("Invalid config item: " + group + "." + name + "." + key);		
	}
	
	/**
	 * Return ZServer instance by nameFC in file config (FC)
	 * @param nameFC - nameFC of Zabbix Server in File Config
	 * @return ZServer instance with given nameFC or null
	 */
	private ZServer getZServerByNameFC(String nameFC) {
		ZServer result=null;
		for(ZServer zs:zbxServers){
			if(zs.getZbxServerNameFC().equals(nameFC)){
				result=zs;
				break;
			}
		}
		//if(null==result) throw new NullPointerException("Failed to find among zbxServers Zabbix Server for given Zabbix Server nameFC: "+nameFC);		
		return result;
	}

	private void readConfigDB(String group, String name, String key, String value) {
		Database dbsrv = this.getDatabaseByNameFC(name);
		if (dbsrv == null){
			dbsrv = new Database();
			dbsrv.nameFC=name;
			databases.add(dbsrv);
		}
		
		if ("Type".equalsIgnoreCase(key))
			dbsrv.type = DBType.fromString(value);
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
		else if ("Persistence".equalsIgnoreCase(key))
			dbsrv.setPersistence(value);
		else
			LOG.info("Invalid config item: " + group + "." + name + "." + key);
	}
	
	
	private Database getDatabaseByNameFC(String nameFC) {
		Database result=null;
		for(Database db:databases){
			if(db.getDBNameFC().equals(nameFC)){
				result=db;
				break;
			}
		}
		//if(null==result) throw new NullPointerException("Failed to find among zbxServers Zabbix Server for given Zabbix Server nameFC: "+nameFC);		
		return result;
	}

	/**
	 * compares config file hashes: previous and actual
	 * @return true or false
	 */
	public boolean checkConfigChanges() {
		Config newconfig = new Config();
		Config oldconfig = this;
		newconfig.setBasedir(oldconfig.getBasedir());
		newconfig.setConfigFile(oldconfig.getConfigFile());
		try{
			newconfig.readFileConfig();
		}
		catch (IOException e) {
			LOG.error("Error in config: " + e.getLocalizedMessage());
			return false;
		}
		catch(NullPointerException e){
			LOG.error("Failed to calculate hash for config file: "+e.getLocalizedMessage());
			return false;
		}
		
		 boolean configFileChanged=(0==newconfig.getFileConfigHash().compareTo(oldconfig.getFileConfigHash()))?false:true;
		 LOG.debug("Is config changed: "+configFileChanged);
		 if(configFileChanged) return true;
		 
		 /**
		  * Update configuration from Zabbix Servers
		  */
		 newconfig.getItemConfigFromZabbix();
		 
		 
		 Set<String> itemGroupNames=oldconfig.getSetOfItemGroupNames();
		 Set<String> newItemGroupNames=newconfig.getSetOfItemGroupNames();
		 
		 /**
		  * candidates for update:
		  * i.e. zbxServerHost:zbxServerPort, proxy, host, db, item key 
		  * are the same
		  */
		 Set<String> toUpdate=new HashSet<>(itemGroupNames);		 
		 toUpdate.retainAll(newItemGroupNames);
		 Set<String> toRemoveFromUpdate=new HashSet<>();
		 for (String itemGroupName:toUpdate){
			ZServer zabbixServer=oldconfig.getZabbixServerByItemGroupName(itemGroupName);
			ZServer newZabbixServer=newconfig.getZabbixServerByItemGroupName(itemGroupName);
			Map<String,String> itemConfig=zabbixServer.getItemConfigByItemGroupName(itemGroupName);
			Map<String,String> newItemConfig=newZabbixServer.getItemConfigByItemGroupName(itemGroupName);
			String hashParam=itemConfig.get("hashParam");
			String newHashParam=newItemConfig.get("hashParam");
			if(hashParam.equals(newHashParam)) 
				toRemoveFromUpdate.add(itemGroupName);
		 }
		 toUpdate.removeAll(toRemoveFromUpdate);
		 
		 Set<String> toDelete=new HashSet<String>(itemGroupNames);
		 toDelete.removeAll(newItemGroupNames);
		 
		 Set<String> toAdd=new HashSet<String>(newItemGroupNames);
		 toAdd.removeAll(itemGroupNames);
		 
		
		 if(!toUpdate.isEmpty()||!toAdd.isEmpty()||!toDelete.isEmpty()){
			 newconfig.buildItems();
			 
			 /**
			  * delete items configs
			  */
			 stopSchedulers(toDelete);
			 deleteItemConfigs(toDelete);
			 
			 /**
			  * add item configs
			  */
			 addItemConfigs(newconfig,toAdd);
	
			 /**
			  * update item configs
			  */
			 stopSchedulers(toUpdate);
			 updateItemConfigs(newconfig,toUpdate);
			 
			 
			 /**
			  * Launch schedulers
			  */
			 launchSchedulers(toAdd);
			 launchSchedulers(toUpdate);
		 }
		 
		 return false;
	}

	private void stopSchedulers(Set<String> itemGroupNames) {
		for(String ign:itemGroupNames){
			/**
			 * clean workTimers
			 */
			workTimers.get(ign).cancel();
			workTimers.remove(ign);

			/**
			 * Clean schedulers
			 */
			for(Entry<Integer, Scheduler> s:getSchedulersByItemGroupName(ign).entrySet()){
				s.getValue().cancel();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			schedulers.remove(ign);		
		}
	}

	public void startChecks() {
		DBManager manager = DBManager.getInstance();
		for (Database db:getDatabases()){
			manager.addDatabase(db);
			Adapter adapter=manager.getDatabaseByName(db.getDBNameFC());
			try {
				adapter.createConnection();
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
			launchSchedulers(db.getItemGroupNames());
		}
	}

	
	private void launchSchedulers(Set<String> itemGroupNames) {
		for (String itemGroupName:itemGroupNames){
			if(!workTimers.containsKey(itemGroupName)){
				Timer workTimer=new Timer(itemGroupName);
				workTimers.put(itemGroupName,workTimer);
				int i = 0;								
				for (Entry<Integer, Scheduler> element: getSchedulersByItemGroupName(itemGroupName).entrySet()) {
					LOG.info("creating worker("+itemGroupName+") for timing: " + element.getKey());
					i++;
					workTimer.scheduleAtFixedRate(element.getValue(), 500 + i * 500, (long)(element.getKey() * 1000));									
				}				
//				try {
//					//Thread.sleep(5000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
		}
	}
	
	
	private void addItemConfigs(Config newconfig, Set<String> newItemGroupNames) {
		for (String newItemGroupName:newItemGroupNames){
			if(this!=newconfig){	
				/**
				 * Add/update ZServers
				 */
				ZServer newZabbixServer=newconfig.getZabbixServerByItemGroupName(newItemGroupName);
				ZServer zabbixServer=this.getZServerByNameFC(newZabbixServer.getZbxServerNameFC());
				if (null == zabbixServer){// just add
					LOG.error("Can't find ZServer by nameFC: "+newZabbixServer.getZbxServerNameFC());
					zabbixServer=newZabbixServer;
					zbxServers.add(zabbixServer);
				}else{// update since pointer to existing ZServer may appear in code
					Map<String,String> newItemConfig=newZabbixServer.getItemConfigByItemGroupName(newItemGroupName);
					zabbixServer.setHashZabbixConfig(newZabbixServer.getHashZabbixConfig());
					zabbixServer.setHosts(newZabbixServer.getHosts());
					zabbixServer.setItems(newZabbixServer.getItems());
					zabbixServer.setHostmacro(newZabbixServer.getHostmacro());
					zabbixServer.setHostsTemplates(newZabbixServer.getHostsTemplates());
					zabbixServer.addItemConfig(newItemGroupName, newItemConfig);
				}
				
				/**
				 * Add/update Databases
				 */
				Database newDatabase=newconfig.getDatabaseByItemGroupName(newItemGroupName);
				Database database = this.getDatabaseByNameFC(newDatabase.getDBNameFC());
				if(null==database){//add
					LOG.error("Can't find Database by nameFC: "+newDatabase.getDBNameFC());
					database=newDatabase;
					databases.add(database);
				}else{//update
					database.addItemGroupName(newItemGroupName);					
				}
				
				/**
				 * Add schedulers
				 */
				schedulers.put(newItemGroupName, newconfig.getSchedulersByItemGroupName(newItemGroupName));
			}
		}
	}
	

	/**
	 * Delete item group names entities from configuration 
	 * @param itemGroupNames set of item group names to delete
	 */
	private void deleteItemConfigs(Set<String> itemGroupNames) {
		/**
		 * zbxServers:
		 * 4. Config.getItemConfigFromZabbix: itemsJSON, hosts, items, hostmacro, itemConfigs
		 * 5. 
		 * 
		 * databases:
		 * 1. loadItemConfigFromZabbix: itemGroupName
		 * 2. 
		 * 
		 * schedulers:
		 * 1. buildServerElements: itemConfig -> Schedulers, itemGroupName, time, new Scheduler(time), scheduler.addItem(itemGroupName, item)
		 * 2. 
		 */		
		
		for(String ign:itemGroupNames){
//			/**
//			 * clean workTimers
//			 */
//			workTimers.get(ign).cancel();
//			workTimers.remove(ign);
//
//			/**
//			 * Clean schedulers
//			 */
//			for(Entry<Integer, Scheduler> s:getSchedulersByItemGroupName(ign).entrySet()){
//				s.getValue().cancel();
//			}
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			schedulers.remove(ign);
			
			/**
			 * Clean zbxServers
			 */
			ZServer zs=	this.getZabbixServerByItemGroupName(ign);
			zs.removeItemConfig(ign);
			//if(zs.getItemConfigs().isEmpty()) zbxServers.remove(zs);
			
			
			/**
			 * Clean databases
			 */
			Database db=this.getDatabaseByItemGroupName(ign);
			db.removeItemGroupName(ign);
		}		
	}

	private Database getDatabaseByItemGroupName(String itemGroupName) {
		Database result=null;
		for(Database db:databases){
			if(db.getItemGroupNames().contains(itemGroupName)) {
				result=db;
				break;
			}
		}
		return result;
	}

	/**
	 * Update changed item Configs
	 * @param newconfig config instance to launch schedulers from
	 * @param itemGroupNames set of group names which have to be updated
	 */
	private void updateItemConfigs(Config newConfig, Set<String> itemGroupNames) {
		deleteItemConfigs(itemGroupNames);
		addItemConfigs(newConfig,itemGroupNames);
	}

	/**
	 * 
	 * @param zabbixServers collection of Zabbix Servers
	 * @return set of strings representing item group names
	 */
	private Set<String> getSetOfItemGroupNames() {
		Set<String> result=new HashSet<String>();
		for(ZServer zs:getZabbixServers()) result.addAll(zs.getItemGroupNames());
		return result;
	}

	
	
	private ZServer getZabbixServerByItemGroupName(String itemGroupName){
		ZServer result=null;		
		for(ZServer zs:getZabbixServers()){
			if(zs.getItemGroupNames().contains(itemGroupName)){
				result=zs;
				break;
			}
		}				
		if(null==result) throw new NullPointerException("Failed to find Zabbix Server for given item group nameFC: "+itemGroupName);		
		return result;
	}
	

	/**
	 * Prepare byte array as a request to Zabbix Server
	 * @param json string to be sent to Zabbix Server as proxy request
	 * @return byte array representation of request ready to be sent
	 */
	public byte[] getRequestToZabbixServer(String json){
		String str=new String(ZBX_HEADER_PREFIX + "________"+json);
		byte[] data=str.getBytes();
		
		//get size of json request in little-endian format
		byte[] leSize=null;
		leSize=getNumberInLEFormat8B(json.length());
		if(leSize.length != 8) {
			LOG.error("getZabbixProxyRequest():leSize has "+leSize.length +" != 8 bytes!");
			return null;
		}
		
		for(int i=0;i<8;++i) data[i+5]=leSize[i];
		//LOG.debug("getZabbixProxyRequest(): data: "+ new String(data));
		return data;
		
	}
	
	/**
	 * encode in low-endian format
	 * @param l - number in current OS format
	 * @return low-endian encoded number in byte array
	 */
	private byte[] getNumberInLEFormat8B(long l) {
		byte[] leL=new byte[8];
		for(int i=0;i<8;++i){
			leL[i]=(byte) (l & 0xFF);
			l>>=8;			
		}
		return leL;
	}

	
	/**
	 * modify zabbix entity to java Map
	 * @param zEntity - zabbix entity
	 * @return - converted to Map entity
	 */
	private Map<String, List<String>> zJSONObject2Map(JSONObject zEntity) {
		Map<String,List<String> > m=new HashMap<String,List<String> >();
		JSONArray data=zEntity.getJSONArray("data");
		JSONArray fields=zEntity.getJSONArray("fields");
		for(int i=0;i<fields.size();++i){
			m.put(fields.getString(i),new ArrayList<String>());
			for(int j=0;j<data.size();++j){
				m.get(fields.getString(i)).add(j, data.getJSONArray(j).getString(i));
			}
		}
		return m;
	}
	
	
	/**
	 * Send request to Zabbix Server:
	 * @param host - Zabbix Server
	 * @param port - Zabbix Server Port
	 * @param json - body of request in json format
	 * @return - body of response in json format
	 */
	public String requestZabbix(String host, int port, String json){
		byte[] response = new byte[2048];
		Socket zabbix = null;
		OutputStreamWriter out = null;
		InputStream in = null;			
		byte[] data=null;
		String resp=new String();
		try {
			zabbix = new Socket();
			//TODO socket timeout has to be read from config file
			zabbix.setSoTimeout(5000);
			
			zabbix.connect(new InetSocketAddress(host, port));
			OutputStream os = zabbix.getOutputStream();
			
			data=getRequestToZabbixServer(json);
			
			//send request
			os.write(data);
			os.flush();

			//read response
			in = zabbix.getInputStream();
			
			int pos1=13;
			int bRead=0;
			while(true){
				bRead = in.read(response);
				//LOG.debug("read="+read+"\nresponse="+new String(response));
				if(bRead<=0) break;			
				//remove binary header
				resp+=new String(Arrays.copyOfRange(response, pos1, bRead));
				pos1=0;
			}
			//LOG.debug("requestZabbix(): resp: "+ resp);
			//resp=resp.substring(13);//remove binary header
			
		}
		catch (Exception ex) {
			LOG.error("requestZabbix(): Error getting data from Zabbix server - " + ex.getMessage());
		}
		finally {
			if (in != null)
				try {
					in.close();
				}
				catch (IOException e) {}
			if (out != null)
				try {
					out.close();
				}
				catch (IOException e) {}
			if (zabbix != null)
				try {
					zabbix.close();
				}
				catch (IOException e) {}
		}
		
		return resp;
	}

	
	
	/**
	 * Fill item configs for all configured Zabbix Servers
	 */
	public void getItemConfigFromZabbix(){
		Collection<ZServer> zServers=null;		
		try{
			zServers = getZabbixServers();
		}catch (Exception ex) {
			LOG.error("Error getting Zabbix server config - " + ex.getMessage());
		}
		
		for (ZServer zs: zServers){
			String resp=new String();
			resp=requestZabbix(zs.zbxServerHost, zs.zbxServerPort,zs.getProxyConfigRequest());

			zs.setHashZabbixConfig(Config.calculateMD5Sum(resp));

			
			try{//parse json
				//resp=resp.substring(resp.indexOf("{"));
				//resp=resp.substring(13);
				//LOG.debug(resp);
				JSONObject o=JSON.parseObject(resp);

				/**result for hosts:
				 * {ipmi_privilege=[2], tls_psk_identity=[], tls_accept=[1], hostid=[11082], tls_issuer=[],
				 * ipmi_password=[], ipmi_authtype=[-1], ipmi_username=[], host=[APISQL], nameFC=[APISQL], tls_connect=[1],
				 * tls_psk=[], tls_subject=[], status=[0]}
				 * 
				 * result for items:
				 * {trapper_hosts=[, ], snmpv3_authprotocol=[0, 0], snmpv3_securityname=[, ], flags=[1, 2], password=[, ], interfaceid=[null, null], snmpv3_authpassphrase=[, ], snmpv3_privprotocol=[0, 0],snmp_oid=[, ], delay_flex=[, ], publickey=[, ], ipmi_sensor=[, ], logtimefmt=[, ],
				 * authtype=[0, 0], mtime=[0, 0], snmp_community=[, ], snmpv3_securitylevel=[0, 0], privatekey=[, ], lastlogsize=[0, 0], zbxServerPort=[, ], data_type=[0, 0], snmpv3_privpassphrase=[, ], snmpv3_contextname=[, ], username=[, ]}
				 * status=[0, 0],
				 * type=[11, 11], value_type=[4, 3],
				 * hostid=[11082, 11082], itemid=[143587, 143588],  
				 * key_=[db.odbc.discovery[sessions,{$DSN}], db.odbc.select[sessions,{$DSN}]],
				 * params=[select machine, count(1) N from v$session, sessions],
				 * delay=[120, 120],
				 * 
				 * hostmacro:
				 * "hostmacro":{"fields":["hostmacroid","hostid","macro","value"],"data":[[450,11082,"{$DSN}","APISQL"],[457,11084,"{$PERF_SCHEMA}","'performance_schema'"]]},
				 * 
				 * hosts_templates:
				 * "hosts_templates":{"fields":["hosttemplateid","hostid","templateid"],"data":[[2195,11082,11084]]},
				 * 
				 * 
				 * */
				
				zs.setHosts(zJSONObject2Map(o.getJSONObject("hosts")));
				zs.setItems(zJSONObject2Map(o.getJSONObject("items")));
				zs.setHostmacro(zJSONObject2Map(o.getJSONObject("hostmacro")));
				zs.setHostsTemplates(zJSONObject2Map(o.getJSONObject("hosts_templates")));
				
				
			}
			catch (Exception ex){
				System.out.println("Error parsing json objects - " + ex.getLocalizedMessage());
			}
			
			//references
			Map<String,List<String> > hosts=zs.getHosts();
			Map<String,List<String> > items=zs.getItems();
			Map<String,List<String> > hostmacro=zs.getHostmacro();

			

			try{
				//result for hosts:
				// hostid=[11082], host=[APISQL], nameFC=[APISQL], status=[0]
				//
				//result for items:
				//status=[0, 0], 
				//type=[11, 11], value_type=[4, 3],
				//hostid=[11082, 11082], itemid=[143587, 143588],  
				//key_=[DB4bix.config[mysql.database.discovery], db.odbc.select[sessions,{$DSN}]], 
				//params=[<XML config>, sessions], 
				//delay=[120, 120],
				//			
				
				/**
				 * Get disabled hosts
				 */
				Set<String> hostFilter=new HashSet<>();
				List<String> statuses=hosts.get("status");
				for(int i=0;i<statuses.size();++i){
					if(!"0".equals(statuses.get(i)))
						hostFilter.add(hosts.get("hostid").get(i));
				}
				
				
				/**
				 * fill itemConfigs collection
				 */
				for(int it=0;it<items.get("key_").size();++it){
					String key=items.get("key_").get(it);
					if(key.contains(ZABBIX_ITEM_CONFIG_SUFFIX)){
						String hostid=items.get("hostid").get(it);
						if(hostFilter.contains(hostid)) 
							continue;
						String host=zs.getHostByHostId(hostid);						
						/**
						 * substitute macro for getting db name
						 */
						String db=key.split(",")[1].split("]")[0].trim().toUpperCase();						
						if(isMacro(db)){
							db=zs.getMacroValue(hostid,db);
						}
						if(zs.definedDBNames.contains(db)){
							Map<String,String> m = new HashMap<String,String>();
							String param=items.get("params").get(it);//Map->List[it]
							m.put("param", param);
							/**
							 * Getting text representation of md5 hash of substituted
							 */							
							m.put("hashParam", Config.calculateMD5Sum(param)+Config.calculateMD5Sum(substituteMacros(param,zs,hostid)));
							m.put("hostid", hostid);
							m.put("host", host);
							m.put("db", db);
							m.put("key_", key);
							/**
							 * Construct itemGroupName
							 */
							String itemGroupName=constructItemGroupName(zs,host,db,key);
							
							m.put("itemGroupName",itemGroupName);
							zs.addItemConfig(itemGroupName,m);// shortcut for itemConfig
							
							/**
							 * fill Dbs config with itemGroupName Set<String> itemGrouName.
							 */
							this.getDatabaseByNameFC(db).addItemGroupName(itemGroupName);							
						}
					}
				}
				LOG.debug("Got item config from Zabbix Server "+zs);
			}
			catch (Exception ex){
				LOG.error("Error getting item Zabbix Config from "+zs+": " + ex.getLocalizedMessage());
			}
		}		
	}


	public static String calculateMD5Sum(String inStr) {
		MessageDigest hasher = null;
		try{
			hasher = java.security.MessageDigest.getInstance("MD5");
		}
		catch(NoSuchAlgorithmException e){
			LOG.error("Wrong algorithm nameFC provided while getiing instance of MessageDigest: " + e.getLocalizedMessage());
		}
		return (new HexBinaryAdapter()).marshal(hasher.digest(inStr.getBytes()));
	}

	private String substituteMacros(String inStr, ZServer zs, String hostid) {
		String result=new String(inStr);
		try{// substitute macro
			int iStart=0;
			int iEnd=0;
			iStart=result.indexOf("{$");
			while (-1!=iStart){
				iEnd=result.indexOf('}', iStart);
				if(-1!=iEnd){							
					String macro=result.substring(iStart, ++iEnd);
					if(isMacro(macro)){
						String macroValue=zs.getMacroValue(hostid,macro);
						if(null!=macroValue){
							result=result.replace(macro, macroValue);
							iEnd=iEnd-macro.length()+macroValue.length();
						}
					}							
				} else	break;						
				iStart=result.indexOf("{$",iEnd);
			}
		}
		catch (Exception ex){
			LOG.error("Error substituting macros - " + ex.getLocalizedMessage());
		}
		return result;
	}

	private String constructItemGroupName(ZServer zs, String host, String db, String key) {
		return new String(zs.toString()+"/"+zs.getProxy()+"/"+host+"/"+db+"/"+key);
	}


	public void buildItems() {
			
		//result for hosts:
		// hostid=[11082], host=[APISQL], nameFC=[APISQL], status=[0]
		//result for items:
		//status=[0, 0], 
		//type=[11, 11], value_type=[4, 3],
		//hostid=[11082, 11082], itemid=[143587, 143588],  
		//key_=[db.odbc.discovery[sessions,{$DSN}], db.odbc.select[sessions,{$DSN}]], 
		//params=[select machine, count(1) N from v$session, sessions], 
		//delay=[120, 120],
		//System.out.println("items.hostid: "+items.get("hostid"));
		//System.out.println("items.type: "+items.get("type"));
		//System.out.println("items.params: "+items.get("params"));
		//System.out.println("items.itemid: "+items.get("itemid"));
		//System.out.println("items.key_: "+items.get("key_"));
		//System.out.println("hosts: "+hosts);

		Collection<ZServer> zServers=null;
		try{
			zServers = getZabbixServers();
		}catch (Exception ex) {
			LOG.error("Error getting Zabbix servers collection - " + ex.getLocalizedMessage());
		}
		
		for (ZServer zs: zServers){			
			for(Entry<String, Map<String, String>> ic:zs.getItemConfigs().entrySet()){
				LOG.debug("buildItems: "+zs+" --> "+ic.getKey());
				try {					
					String param=ic.getValue().get("param");					
					param="<!DOCTYPE parms SYSTEM \""+getBasedir()+"/items/param.dtd\">"+param;
					Document doc = DocumentHelper.parseText(param);
					Element root = doc.getRootElement();
					String prefix = root.attributeValue("prefix");			
					for (Object srv: root.elements("server")) {
						if (srv instanceof Element) buildItemsAndSchedulers((Element) srv, ic.getValue(), prefix , zs);
					}
	//				for (Object db: root.elements("database")) {
	//					if (db instanceof Element) buildDatabaseElements((Element) db, itemGroupName, prefix);
	//				}
				}
				catch (Exception ex) {
					LOG.error("Error while loading config item "+ic, ex);
					LOG.error("Skipping "+ic);
				}
			}
		}
	}
	
	
	private void buildItemsAndSchedulers(Element e, Map<String,String> itemConfig, String prefix,ZServer zs) {
		String itemGroupName=itemConfig.get("itemGroupName");
		Map<Integer,Scheduler> schedulers=getSchedulersByItemGroupName(itemGroupName);
		for (Object itm: e.elements()) {
			if (itm instanceof Element) {
				Element itmE = (Element) itm;
				int time = 60;
				try {
					time = Integer.parseInt(itmE.attributeValue("time"));
				}
				catch (NumberFormatException ex) {
					LOG.warn("invalid time value: " + itmE.attributeValue("time"));
				}				
				if (!schedulers.containsKey(time)) {
					LOG.debug("creating item scheduler with time " + time);
					schedulers.put(time, new Scheduler(time));
				}
				Scheduler itemSch = schedulers.get(time);
				String query=itmE.getTextTrim();
				
				query=substituteMacros(query,zs,itemConfig.get("hostid"));
				switch (itmE.getName()) {
					case "discovery": {
						Discovery item = new Discovery(prefix + itmE.attributeValue("item"), query, itemConfig, zs);
						String nameList = itmE.attributeValue("names", "");
						String names[] = nameList.split("\\|");
						if (names != null && names.length > 0)
							item.setAltNames(names);
						//if(!item.setZServer(zs)) LOG.debug("buildServerElements: we haven't not actually registered ZServer "+zs.toString()+" for item "+item.getName());
						itemSch.addItem(itemGroupName, item);
					}
					break;
					
					case "query": {
						Item item = new SimpleItem(prefix + itmE.attributeValue("item"), query,itmE.attributeValue("nodata"), itemConfig, zs);
						itemSch.addItem(itemGroupName, item);
					}
					break;
					
					case "multiquery": {
						String itemList = itmE.attributeValue("items", "");
						String items[] = itemList.split("\\|");
						Item item;
						if (itmE.attributeValue("type", "column").equalsIgnoreCase("column"))
							item = new MultiColumnItem(prefix, items, query, itemConfig, zs);
						else
							item = new MultiRowItem(prefix, items, query, itemConfig, zs);
						itemSch.addItem(itemGroupName, item);
					}
					break;
				}
			}
		}
	}

	private boolean isMacro(String macro) {
		String macroMask="^\\{\\$[a-zA-Z0-9_-]+\\}$";		
		return macro.matches(macroMask);
	}

//	private void buildDatabaseElements(Element e, String groupName, String prefix) {
//		for (Object itm: e.elements()) {
//			if (itm instanceof Element) {
//				Element itmE = (Element) itm;
//				int time = 60;
//				try {
//					time = Integer.parseInt(itmE.attributeValue("time"));
//				}
//				catch (NumberFormatException ex) {
//					LOG.warn("invalid time value: " + itmE.attributeValue("time"));
//				}
//				if (!scheduler.containsKey(time)) {
//					LOG.debug("creating item scheduler with time " + time);
//					scheduler.put(time, new Scheduler(time));
//				}
//				Scheduler itemSch = scheduler.get(time);
//				switch (itmE.getName()) {
//					case "discovery": {
//						Discovery item = new Discovery(prefix + itmE.attributeValue("item"), itmE.getTextTrim());
//						String nameList = itmE.attributeValue("names", "");
//						String names[] = nameList.split("\\|");
//						if (names != null && names.length > 0)
//							item.setAltNames(names);
//						itemSch.addItem(groupName, item);
//					}
//					break;
//					
//					case "query": {
//						Item item = new SimpleItem(prefix + itmE.attributeValue("item"), itmE.getTextTrim(), itmE.attributeValue("nodata"));
//						itemSch.addItem(groupName, item);
//					}
//					break;
//					
//					case "multiquery": {
//						String itemList = itmE.attributeValue("items", "");
//						String items[] = itemList.split("\\|");
//						Item item;
//						if (itmE.attributeValue("type", "column").equalsIgnoreCase("column"))
//							item = new MultiColumnItem(prefix, items, itmE.getTextTrim());
//						else
//							item = new MultiRowItem(prefix, items, itmE.getTextTrim());
//						itemSch.addItem(groupName, item);
//					}
//					break;
//				}
//			}
//		}
//	}
//	
	
	
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
	
	public String getSSPDir() {
		return sspDir;
	}
	
	public String getLogFileSize() {
		return logFileSize;
	}
	
	public void setLogFileSize(String s) {
		logFileSize=s;
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
		Collection<ZServer> validServers = zbxServers;
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
		Collection<Database> validDatabases = databases;
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
		builder.append("\n");
		for (ZServer zsrv: zbxServers)
			builder.append("-- Zabbix:\t").append(zsrv).append("\n");
		for (Database db: databases)
			builder.append("-- Database:\t").append(db).append("\n");
		return builder.toString();
	}


	public int getQueryTimeout() {
		return this.queryTimeout;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public String getFileConfigHash() {
		return configFileHash;
	}

	private void setFileConfigHash(String fileConfigHash) {
		this.configFileHash = fileConfigHash;
	}

	public static Map<String,Timer> getWorkTimers() {
		return workTimers;
	}

	public static void setWorkTimers(Map<String,Timer> workTimers) {
		Config.workTimers = workTimers;
	}

	public int getUpdateConfigTimeout() {
		return updateConfigTimeout;
	}

	public void setUpdateConfigTimeout(int updateConfigTimeout) {
		this.updateConfigTimeout = updateConfigTimeout;
	}


}
