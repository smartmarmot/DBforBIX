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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.Map.Entry;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.log4j.Logger;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartmarmot.dbforbix.config.element.IConfigurationElement;
import com.smartmarmot.dbforbix.config.item.ConfigurationItem;
import com.smartmarmot.dbforbix.config.item.ConfigurationItemParserFactory;
import com.smartmarmot.dbforbix.config.item.IConfigurationItem;
import com.smartmarmot.dbforbix.config.item.IConfigurationItemParser;
import com.smartmarmot.dbforbix.db.DBManager;
import com.smartmarmot.dbforbix.db.DBType;
import com.smartmarmot.dbforbix.db.adapter.DBAdapter;
import com.smartmarmot.dbforbix.scheduler.Scheduler;
import com.smartmarmot.common.utils.DBforBIXHelper;

public class Config {

	interface Validable {

		public boolean isValid();
	}
	
	/**
	 * 
	 */
	private static final Logger		LOG					= Logger.getLogger(Config.class);
	private static final String		GLOBAL_NAME			= "DBforBIX";
	private static final String		GLOBAL_POOL			= "Pool";
	private static final String		GLOBAL_ZBXSRV		= "ZabbixServer";
	private static final String		GLOBAL_DB			= "DB";
	private static final String		SET_UPDATECONFIG 	= GLOBAL_NAME + ".UpdateConfigTimeout";
	private static final String		SET_POOL_MAXACTIVE	= GLOBAL_POOL + ".MaxActive";
	private static final String		SET_POOL_MAXIDLE	= GLOBAL_POOL + ".MaxIdle";
	private static final String 	SET_LOGIN_TIMEOUT 	= GLOBAL_POOL+ ".LoginTimeOut";
	private static final String 	ZBX_HEADER_PREFIX	= "ZBXD\1";
	private static final String		SET_PERSISTENCETYPE	= GLOBAL_NAME + ".PersistenceType";
	private static final String		SET_PERSISTENCEDIR 	= GLOBAL_NAME + ".PersistenceDir";
	
	
	
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
//	private Level					logLevel						= Level.WARN;
//	private String					logFile							= "./logs/DBforBix.log";
//	private String					sspDir		 					= "./temp/";
//	private String					logFileSize						= "1MB";
	private int						maxActive						= 10;	
	private int						loginTimeout					= 60;	// default queryTimeout
	private int						maxIdle							= 15;	// pieces
	private int						updateConfigTimeout				= 120; 	// seconds
	private String					persistenceType					= "DB";
	private String					persistenceDir		 			= "./persistence/";
	
	
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
	 * 2. loadItemConfigFromZabbix: configurationUID
	 * 2. main: add db to dbmanager
	 * 3. 
	 * 
	 * schedulers:
	 * 1. buildServerElements: configurationUID, time, new Scheduler(time), scheduler.addItem(configurationUID, item)
	 * 2. main: schedulers -> worktimers
	 * 3. 
	 */
	
	//configurationUID -> ZServer
	//configurationUID:ZServer = N:1
	private Set<ZabbixServer>	_zabbixServers;
	
	//configurationUID -> Database
	//configurationUID:Database = N:1
	private Set<Database>	databases;
	
	//configurationUID->time->Scheduler
	//configurationUID:Scheduler = 1:N
	private Map<String, Map<Integer, Scheduler>> schedulers  = new HashMap<String,Map<Integer, Scheduler>>();
	
	//configurationUID->Timer
	//configurationUID:workTimer = 1:1
	private static Map<String,Timer> workTimers = new HashMap<String, Timer>();

	public Map<Integer, Scheduler> getSchedulersByConfigurationUID(String configurationUID) {
		if(!schedulers.containsKey(configurationUID)) schedulers.put(configurationUID,new HashMap<Integer,Scheduler>());
		return schedulers.get(configurationUID);
	}

	public void addScheduler(String configurationUID, HashMap<Integer, Scheduler> scheduler) {
		this.schedulers.put(configurationUID,scheduler);
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
		_zabbixServers = new HashSet<ZabbixServer>();
		databases = new HashSet<Database>();
	}
	
	/**
	 * reset Config instance
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
			LOG.error("Something has happenned reading file: " + e.getLocalizedMessage());
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
			if (pcfg.containsKey(SET_PERSISTENCETYPE))
				persistenceType = pcfg.getString(SET_PERSISTENCETYPE);			
			if (pcfg.containsKey(SET_PERSISTENCEDIR))
				persistenceDir = pcfg.getString(SET_PERSISTENCEDIR);
			if (pcfg.containsKey(SET_UPDATECONFIG))
				setUpdateConfigTimeout(pcfg.getInt(SET_UPDATECONFIG));
			if (pcfg.containsKey(SET_POOL_MAXACTIVE))
				maxActive = pcfg.getInt(SET_POOL_MAXACTIVE);
			if (pcfg.containsKey(SET_POOL_MAXIDLE))
				maxIdle = pcfg.getInt(SET_POOL_MAXIDLE);
			if (pcfg.containsKey(SET_LOGIN_TIMEOUT))
				loginTimeout  = Integer.parseInt(pcfg.getString(SET_LOGIN_TIMEOUT));				
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
		ZabbixServer zabbixServer = getZServerByNameFC(name);
		if (zabbixServer == null) {
			zabbixServer = new ZabbixServer();
			zabbixServer.setZbxServerNameFC(name);
			_zabbixServers.add(zabbixServer);
		}
		if ("Address".equalsIgnoreCase(key)) zabbixServer.zbxServerHost = value;
		else if ("Port".equalsIgnoreCase(key)) {
			try {
				zabbixServer.zbxServerPort = Integer.parseInt(value);
			}
			catch (NumberFormatException ex) {
				LOG.error("Could not parse zbxServerPort number", ex);
			}
		}
		else if("ProxyName".equalsIgnoreCase(key)) zabbixServer.proxy=value;
		else if("ConfigSuffix".equalsIgnoreCase(key)) zabbixServer.zabbixConfigurationItemSuffix=value;
		else if("DBList".equalsIgnoreCase(key)) zabbixServer.setDefinedDBNames((new ArrayList<String>(Arrays.asList(value.replaceAll("\\s","").toUpperCase().split(",")))));		
		else
			LOG.info("Invalid config item: " + group + "." + name + "." + key);		
	}


	/**
	 * Return ZServer instance by nameFC in file config (FC)
	 * @param nameFC - nameFC of Zabbix Server in File Config
	 * @return ZServer instance with given nameFC or null
	 */
	private ZabbixServer getZServerByNameFC(String nameFC) {
		ZabbixServer result=null;
		for(ZabbixServer zs:_zabbixServers){
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
			dbsrv.setDBNameFC(name);
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
		else if ("QueryTimeout".equalsIgnoreCase(key))
			dbsrv.setQueryTimeout(Integer.parseInt(value));		
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
		 newconfig.getZabbixConfigurationItems();
		 
		 
		 Set<String> configurationUIDs=oldconfig.getSetOfConfigurationUIDs();
		 Set<String> newConfigurationUIDs=newconfig.getSetOfConfigurationUIDs();
		 
		 /**
		  * candidates for update:
		  * i.e. zbxServerHost:zbxServerPort, proxy, host, db, item key 
		  * are the same
		  */
		 Set<String> toUpdate=new HashSet<>(configurationUIDs);		 
		 toUpdate.retainAll(newConfigurationUIDs);
		 Set<String> toRemoveFromUpdate=new HashSet<>();
		 for (String configurationUID:toUpdate){
			ZabbixServer zabbixServer=oldconfig.getZabbixServerByConfigurationUID(configurationUID);
			ZabbixServer newZabbixServer=newconfig.getZabbixServerByConfigurationUID(configurationUID);
			IConfigurationItem configurationItem=zabbixServer.getConfigurationItemByConfigurationUID(configurationUID);
			IConfigurationItem newConfigurationItem=newZabbixServer.getConfigurationItemByConfigurationUID(configurationUID);
			String hashParam=configurationItem.getHashParam();
			String newHashParam=newConfigurationItem.getHashParam();
			if(hashParam.equals(newHashParam)) 
				toRemoveFromUpdate.add(configurationUID);
		 }
		 toUpdate.removeAll(toRemoveFromUpdate);
		 
		 Set<String> toDelete=new HashSet<String>(configurationUIDs);
		 toDelete.removeAll(newConfigurationUIDs);
		 
		 Set<String> toAdd=new HashSet<String>(newConfigurationUIDs);
		 toAdd.removeAll(configurationUIDs);
		 
		
		 if(!toUpdate.isEmpty()||!toAdd.isEmpty()||!toDelete.isEmpty()){
			 /**
			  * stop schedulers that are to be deleted and updated
			  */
			 stopSchedulers(toDelete);
			 stopSchedulers(toUpdate);
			 
			 /**
			  * Build new Items
			  */
			 newconfig.buildConfigurationElementsAndSchedulers();
			 
			 
			 /**
			  * delete items configs
			  */			 
			 deleteItemConfigs(toDelete);
			 
			 /**
			  * add item configs
			  */
			 addConfigurationItems(newconfig,toAdd);
	
			 /**
			  * update item configs
			  */			 
			 updateItemConfigs(newconfig,toUpdate);
			 
			 
			 /**
			  * Open new connections to new DBs and Launch schedulers
			  */
			 startChecks();
		 }		 
		 return false;
	}

	private void stopSchedulers(Set<String> configurationUIDs) {
		for(String ign:configurationUIDs){
			/**
			 * clean workTimers
			 */
			workTimers.get(ign).cancel();
			workTimers.remove(ign);

			/**
			 * Clean schedulers
			 */
			for(Entry<Integer, Scheduler> s:getSchedulersByConfigurationUID(ign).entrySet()){
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
			/**
			 * Create adapter only if there are items for this DB
			 */
			if( 0 < db.getConfigurationUIDs().size() ){
				DBAdapter adapter=manager.getDatabaseByName(db.getDBNameFC());
				if(null==adapter){
					manager.addDatabase(db);
					adapter=manager.getDatabaseByName(db.getDBNameFC());
//					try {
//						adapter.getConnection();
//					} catch (ClassNotFoundException | SQLException e) {
//						e.printStackTrace();
//					}
				}
				launchSchedulers(db.getConfigurationUIDs());
			}
		}
	}

	
	private void launchSchedulers(Set<String> configurationUIDs) {
		for (String configurationUID:configurationUIDs){
			if(!workTimers.containsKey(configurationUID)){
				Timer workTimer=new Timer(configurationUID);
				workTimers.put(configurationUID,workTimer);
				int i = 0;								
				for (Entry<Integer, Scheduler> element: getSchedulersByConfigurationUID(configurationUID).entrySet()) {
					LOG.info("creating worker("+configurationUID+") for timing: " + element.getKey());
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
	
	
	private void addConfigurationItems(Config newconfig, Set<String> newConfigurationUIDs) {
		for (String newConfigurationUID:newConfigurationUIDs){
			if(this!=newconfig){	
				/**
				 * Add/update ZServers
				 */
				ZabbixServer newZabbixServer=newconfig.getZabbixServerByConfigurationUID(newConfigurationUID);
				ZabbixServer zabbixServer=this.getZServerByNameFC(newZabbixServer.getZbxServerNameFC());
				if (null == zabbixServer){// just add
					LOG.error("Can't find ZServer by nameFC: "+newZabbixServer.getZbxServerNameFC());
					zabbixServer=newZabbixServer;
					_zabbixServers.add(zabbixServer);
				}else{// update since pointer to existing ZServer may appear in code
					IConfigurationItem newConfigurationItem=newZabbixServer.getConfigurationItemByConfigurationUID(newConfigurationUID);
					zabbixServer.setHashZabbixConfig(newZabbixServer.getHashZabbixConfig());
					zabbixServer.setHosts(newZabbixServer.getHosts());
					zabbixServer.setItems(newZabbixServer.getItems());
					zabbixServer.setHostmacro(newZabbixServer.getHostmacro());
					zabbixServer.setHostsTemplates(newZabbixServer.getHostsTemplates());
					zabbixServer.addConfigurationItem(newConfigurationItem);
				}
				
				/**
				 * Add/update Databases
				 */
				Database newDatabase=newconfig.getDatabaseByConfigurationUID(newConfigurationUID);
				Database database = this.getDatabaseByNameFC(newDatabase.getDBNameFC());
				if(null==database){//add
					LOG.debug("Add new DB to databases: "+newDatabase.getDBNameFC());
					database=newDatabase;
					databases.add(database);
				}else{//update
					database.addConfigurationUID(newConfigurationUID);
				}
				
				/**
				 * Add schedulers
				 */
				schedulers.put(newConfigurationUID, newconfig.getSchedulersByConfigurationUID(newConfigurationUID));
			}
		}
	}
	

	/**
	 * Delete item group names entities from configuration 
	 * @param configurationUIDs set of item group names to delete
	 */
	private void deleteItemConfigs(Collection<String> configurationUIDs) {
		/**
		 * zbxServers:
		 * 4. Config.getConfigurationItemsFromZabbix: itemsJSON, hosts, configurationElements, hostmacro, configurationItems
		 * 5. 
		 * 
		 * databases:
		 * 1. loadConfigurationItemFromZabbix: configurationUID
		 * 2. 
		 * 
		 * schedulers:
		 * 1. buildServerElements: configurationItem -> Schedulers, configurationUID, time, new Scheduler(time), scheduler.addConfigurationElement(configurationUID, configurationElement)
		 * 2. 
		 */		
		
		for(String configurationUID:configurationUIDs){			
			/**
			 * Clean zbxServers
			 */
			ZabbixServer zs=	this.getZabbixServerByConfigurationUID(configurationUID);
			zs.removeConfigurationItem(configurationUID);			
			
			/**
			 * Clean databases
			 */
			Database db=this.getDatabaseByConfigurationUID(configurationUID);
			db.removeConfigurationUID(configurationUID);
		}
		
		/**
		 * Update DBManager
		 */
		DBManager.getInstance().clean(configurationUIDs);
		
		/**
		 * find databases without any configurationUID and remove them from collection
		 */
		java.util.function.Predicate<Database> dbPredicate=(Database db)-> db.getConfigurationUIDs().isEmpty();
		databases.removeIf(dbPredicate);
		
	
	}

	private Database getDatabaseByConfigurationUID(String configurationUID) {
		Database result=null;
		for(Database db:databases){
			if(db.getConfigurationUIDs().contains(configurationUID)) {
				result=db;
				break;
			}
		}
		return result;
	}

	/**
	 * Update changed configuration items
	 * @param newconfig config instance to launch schedulers from
	 * @param configurationUIDs set of group names which have to be updated
	 */
	private void updateItemConfigs(Config newConfig, Set<String> configurationUIDs) {
		deleteItemConfigs(configurationUIDs);
		addConfigurationItems(newConfig,configurationUIDs);
	}

	/**
	 * 
	 * @param zabbixServers collection of Zabbix Servers
	 * @return set of strings representing item group names
	 */
	private Set<String> getSetOfConfigurationUIDs() {
		Set<String> result=new HashSet<String>();
		for(ZabbixServer zs:getZabbixServers()) result.addAll(zs.getConfigurationUIDs());
		return result;
	}

	
	
	private ZabbixServer getZabbixServerByConfigurationUID(String configurationUID){
		ZabbixServer result=null;		
		for(ZabbixServer zs:getZabbixServers()){
			if(zs.getConfigurationUIDs().contains(configurationUID)){
				result=zs;
				break;
			}
		}				
		if(null==result) throw new NullPointerException("Failed to find Zabbix Server for given configuration UID: "+configurationUID);		
		return result;
	}
	

	/**
	 * Prepare byte array as a request to Zabbix Server
	 * @param json string to be sent to Zabbix Server as proxy request
	 * @return byte array representation of request ready to be sent
	 */
	public byte[] getRequestToZabbixServer(String json){
		String str=new String(ZBX_HEADER_PREFIX + "________"+json);
		//byte[] data=str.getBytes();
		byte[] data;
		try {
			/**
			 * For those who want to use russian and other unicode characters
			 */
			data = str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			LOG.error("Problem with encoding json "+e.getLocalizedMessage());
			data = str.getBytes();
		}
		
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
	 * @return little-endian encoded number in byte array
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
	 * Exception if response from Zabbix is empty.
	 */
	public class ZBXBadResponseException extends Exception
	{
		private static final long serialVersionUID = 6490352403263167340L;
		//Parameterless Constructor
	    public ZBXBadResponseException() {super("Zabbix Server returned empty response!");}
	    //Constructors that accept parameters
	    public ZBXBadResponseException(String msg) { super(msg); }  
	    public ZBXBadResponseException(Throwable cause) { super(cause); }  
	    public ZBXBadResponseException(String msg, Throwable cause) { super(msg, cause); } 
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
			zabbix.setSoTimeout(30000);
			
			zabbix.connect(new InetSocketAddress(host, port));
			OutputStream os = zabbix.getOutputStream();
			
			data=getRequestToZabbixServer(json);
			
			//send request
			os.write(data);
			os.flush();

			//read response
			in = zabbix.getInputStream();
			
			//convert response to string (expecting json)
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
			if(resp.isEmpty())
				throw new ZBXBadResponseException("Zabbix Server ("+host+":"+port+") has returned empty response for request:\n"+json);
			
		}
		catch (ZBXBadResponseException respEx){
			LOG.error(respEx.getLocalizedMessage());
		}
		catch (Exception ex) {
			LOG.error("Error getting data from Zabbix server ("+host+":"+port+"): " + ex.getMessage());
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
	 * Read configuration items from all configured Zabbix Servers
	 */
	public void getZabbixConfigurationItems(){
		//Get collection of all Zabbix Server instances that we should fill
		Collection<ZabbixServer> zabbixServers=null;
		try{
			zabbixServers = getZabbixServers();
		}catch (Exception ex) {
			LOG.error("Error getting list of all valid Zabbix server configurations: " + ex.getMessage());
		}


		//filling cycle
		for (ZabbixServer zabbixServer: zabbixServers){
			String zabbixResponse=new String();
			zabbixResponse=requestZabbix(zabbixServer.zbxServerHost, zabbixServer.zbxServerPort,zabbixServer.getProxyConfigRequest());
			zabbixServer.setHashZabbixConfig(Config.calculateMD5Sum(zabbixResponse));

			try{
				//get and parse json data into json object
				JSONObject zabbixResponseJSON=JSON.parseObject(zabbixResponse);

				//check response validity
				if(zabbixResponseJSON.containsKey("response") && zabbixResponseJSON.getString("response").contains("failed"))
					throw new ZBXBadResponseException("Zabbix Server ("+zabbixServer+") has returned failed response with reason: "+zabbixResponseJSON.getString("info")+"\nRequest: "+zabbixServer.getProxyConfigRequest());

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

				//fill ZServer structures with data
				zabbixServer.setHosts(zJSONObject2Map(zabbixResponseJSON.getJSONObject("hosts")));
				zabbixServer.setItems(zJSONObject2Map(zabbixResponseJSON.getJSONObject("items")));
				zabbixServer.setHostmacro(zJSONObject2Map(zabbixResponseJSON.getJSONObject("hostmacro")));
				zabbixServer.setHostsTemplates(zJSONObject2Map(zabbixResponseJSON.getJSONObject("hosts_templates")));				

			}
			catch (ZBXBadResponseException e){
				LOG.error(e.getLocalizedMessage());
			}
			catch (Exception ex){
				LOG.error("Error parsing json objects from Zabbix Server ("+zabbixServer+"): " + ex.getLocalizedMessage());
			}
			
			
			//get short references on internal data structures of current ZServer
			//hosts structure (example):
			//hostid=[11082], host=[APISQL], nameFC=[APISQL], status=[0]
			//
			//items structure (example):
			//status=[0, 0], 
			//type=[11, 11], value_type=[4, 3],
			//hostid=[11082, 11082], itemid=[143587, 143588],  
			//key_=[DBforBix.config[mysql.database.discovery], db.odbc.select[sessions,{$DSN}]], 
			//params=[<XML config>, sessions], 
			//delay=[120, 120]
			//			
			Map<String,List<String> > hosts=zabbixServer.getHosts();
			Map<String,List<String> > items=zabbixServer.getItems();			
			

			try{
				
				/**
				 * Filter out disabled hosts
				 */
				Set<String> hostFilter=new HashSet<>();
				List<String> statuses=hosts.get("status");
				for(int i=0;i<statuses.size();++i){
					if(!"0".equals(statuses.get(i)))
						hostFilter.add(hosts.get("hostid").get(i));
				}
				
				
				/**
				 * fill configuration items collection for current zabbixServer:
				 * iterate over all items looking for configuration item suffixes (DBforBIX.config by default, can be redefined in DBforBix configuration file on per Zabbix Server principle)
				 */
				boolean foundConfigurationItemSuffix=false;
				for(int it=0;it<items.get("key_").size();++it){
					String key=items.get("key_").get(it);
					if(key.contains(zabbixServer.getConfigurationItemSuffix())){
						foundConfigurationItemSuffix=true;
						String hostid=items.get("hostid").get(it);
						if(hostFilter.contains(hostid)) 
							continue;
						String host=zabbixServer.getHostByHostId(hostid);
						
						/**
						 * substitute macro for getting db name
						 */
						String db=key.split(",")[1].split("]")[0].trim().toUpperCase();						
						if(DBforBIXHelper.isMacro(db)){
							db=zabbixServer.getMacroValue(hostid,db);
						}
						
						
						/**
						 * Construct configurationUID - stands for Unique IDentifier of Zabbix CONFIGURATION item - unique identifier for configuration item across all Zabbix Servers within DbforBIX instance
						 * Why we need new name for this entity:
						 * 1. It's the important key identifier across the whole DBforBIX instance. We should uniquely identify each configuration item from all Zabbix Servers within whole DBforBIX instance!
						 * 2. Why not use configurationItemName or something like this?  It is ambiguous because items in Zabbix have their own defined names.						 * 
						 * So let it be configurationUID.
						 */
						String configurationUID=constructConfigurationUID(zabbixServer,host,db,key);
						
						/**
						 * Register configuration item
						 */
						/*
						//TODO refactor the container of configuration item data from Map to ZabbixItem! Using Map is dangerous because of misprints!
						Map<String,String> mConfigurationItem = new HashMap<String,String>();
						String param=items.get("params").get(it);//Hint for items structure: Map->List[it]
						mConfigurationItem.put("param", param);						
						//Note! Hash together: configuration item XML as is and the result of macros substitution in XML						 							
						mConfigurationItem.put("hashParam", Config.calculateMD5Sum(param)+Config.calculateMD5Sum(substituteMacros(param,zabbixServer,hostid)));
						mConfigurationItem.put("hostid", hostid);
						mConfigurationItem.put("host", host);
						mConfigurationItem.put("db", db);
						mConfigurationItem.put("key_", key);
						mConfigurationItem.put("configurationUID",configurationUID);
						*/
						String param=items.get("params").get(it);//Hint for items structure: Map->List[it]
						//Note! Hash together: configuration item XML as is and the result of macros substitution in XML
						String hashParam = Config.calculateMD5Sum(param)+Config.calculateMD5Sum(DBforBIXHelper.substituteMacros(param,zabbixServer,hostid));
						//prefix is not necessary part of configuration item, and will be added some later
						IConfigurationItem configurationItem = new ConfigurationItem(
								configurationUID,
								zabbixServer,
								host, hostid, db, key,
								param, hashParam
								);
						zabbixServer.addConfigurationItem(configurationItem);
						

						/**
						 * Fill Dbs config with configurationUID Set<String> itemGrouName.
						 * FC stnds for File Config (DBforBIX configuration file).
						 * Propagate absence of DB in file config to Zabbix Web interface
						 */
						Database dbFC=this.getDatabaseByNameFC(db);
						if(null==dbFC){
							dbFC=new Database();
							dbFC.setDBNameFC(db);
							dbFC.url="---propagate error---";
							dbFC.user="---propagate error---";
							dbFC.password="---propagate error---";
							dbFC.type=DBType.DB_NOT_DEFINED;
							databases.add(dbFC);
						}
						dbFC.addConfigurationUID(configurationUID);						
					}
				}
				
				if(!foundConfigurationItemSuffix) LOG.warn("No items with configuration suffix (DBforBIX.config by default) were found on Zabbix Server "+zabbixServer+"! "
						+ "Please check DBforBIX configuration file for string ZabbixServer.<YourZabbixInstanceName>.ConfigSuffix=<YourConfigSuffix> and define it correctly. "
						+ "Then check configuration items in your Zabbix Server web interface: they should contain <YourConfigSuffix> in their item keys, e.g.item key: discovery.<YourConfigSuffix>[tralala,<DBDataSourceName>]."
						+ "Also check that host-owner of configuration item is monitored through Zabbix Proxy name corresponding your DBforBIX parameter "
						+ "ZabbixServer.<YourZabbixInstanceName>.ProxyName=... in DBforBIX configuration file.");
				LOG.debug("Done reading configuration items from Zabbix Server "+zabbixServer);
			}
			catch (Exception ex){
				LOG.error("Error getting item Zabbix Config from Zabbix Server ("+zabbixServer+"): " + ex.getLocalizedMessage());
			}
		}		
	}


	public static String calculateMD5Sum(String inStr) {
		MessageDigest hasher = null;
		try{
			hasher = java.security.MessageDigest.getInstance("MD5");
		}
		catch(NoSuchAlgorithmException e){
			LOG.error("Wrong hashing algorithm name provided while getting instance of MessageDigest: " + e.getLocalizedMessage());
		}
		return (new HexBinaryAdapter()).marshal(hasher.digest(inStr.getBytes()));
	}



	/**
	 * Constructs configurationUID - unique identifier for configuration item across all Zabbix Servers within DbforBIX instance
	 * @param zabbixServer - ZServer instance
	 * @param host - host within Zabbix Server
	 * @param db - database name (Data Source Name)
	 * @param key - configuration item key (should contain suffix like DBforBIX.config or other that you've defined in DBforBIX configuration file for this Zabbix Server)
	 * @return string - configurationUID - cross Zabbix Server unique identifier of Zabbix configuration item
	 */
	private String constructConfigurationUID(ZabbixServer zabbixServer, String host, String db, String key) {
		return new String(zabbixServer.toString()+"/"+zabbixServer.getProxy()+"/"+host+"/"+db+"/"+key);
	}


	public void buildConfigurationElementsAndSchedulers() {			
		//result for hosts:
		// hostid=[11082], host=[APISQL], nameFC=[APISQL], status=[0]
		//result for items:
		//status=[0, 0], 
		//type=[11, 11], value_type=[4, 3],
		//hostid=[11082, 11082], itemid=[143587, 143588],  
		//key_=[db.odbc.discovery[sessions,{$DSN}], db.odbc.select[sessions,{$DSN}]], 
		//params=[select machine, count(1) N from v$session, sessions], 
		//delay=[120, 120],

		Collection<ZabbixServer> zabbixServers=null;
		try{
			zabbixServers = getZabbixServers();
		}catch (Exception ex) {
			LOG.error("Error getting Zabbix servers collection: " + ex.getLocalizedMessage());
		}
		
		for (ZabbixServer zabbixServer: zabbixServers){
			for(Entry<String, IConfigurationItem> configurationItemEntry:zabbixServer.getConfigurationItems().entrySet()){
				String configurationUID=configurationItemEntry.getKey();
				IConfigurationItem configurationItem=configurationItemEntry.getValue();
				LOG.debug("Building configuration elements for "+configurationUID);
				try {
					String config=configurationItem.getParam();
					config=DBforBIXHelper.substituteMacros(config,zabbixServer,configurationItem.getHostid());
					IConfigurationItemParser configurationItemParser = ConfigurationItemParserFactory.getConfigurationItemParser(config);
					Set<IConfigurationElement> configurationElements = configurationItemParser.buildConfigurationElements();					
					configurationItem.addConfigurationElements(configurationElements);
					buildSchedulers(configurationElements);
				}
				catch (Exception ex) {
					LOG.error("Error while loading config item "+configurationItemEntry, ex);
					LOG.error("Skipping "+configurationItemEntry);
				}
			}
		}
		
	}
	
	
	private void buildSchedulers(Set<IConfigurationElement> _configurationElements) {
		for(IConfigurationElement configurationElement:_configurationElements){		
			String configurationUID=configurationElement.getConfigurationUID();
			Map<Integer,Scheduler> schedulers=getSchedulersByConfigurationUID(configurationUID);
			int time=configurationElement.getTime();
			if (!schedulers.containsKey(time)) {
				LOG.debug("creating item scheduler with time " + time);
				schedulers.put(time, new Scheduler(time));
			}
			Scheduler scheduler = schedulers.get(time);
			scheduler.addConfigurationElement(configurationElement);			
		}		
	}
	
	

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public String getBasedir() {
		return basedir;
	}

	public String getSPDir() {
		return persistenceDir;
	}

	public String getSPType() {
		return persistenceType;
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
	public Collection<ZabbixServer> getZabbixServers() {
		Collection<ZabbixServer> validServers = _zabbixServers;
		CollectionUtils.filter(validServers, new Predicate <ZabbixServer>() {			
			@Override
			public boolean evaluate(ZabbixServer object) {
				return ((ZabbixServer) object).isValid();
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
		for (ZabbixServer zsrv: _zabbixServers)
			builder.append("-- Zabbix:\t").append(zsrv).append("\n");
		for (Database db: databases)
			builder.append("-- Database:\t").append(db).append("\n");
		return builder.toString();
	}

	public int getLoginTimeout() {
		return this.loginTimeout;
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
