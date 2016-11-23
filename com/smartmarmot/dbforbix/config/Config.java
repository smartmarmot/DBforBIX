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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.smartmarmot.dbforbix.db.DBType;
import com.smartmarmot.dbforbix.scheduler.Discovery;
import com.smartmarmot.dbforbix.scheduler.Item;
import com.smartmarmot.dbforbix.scheduler.MultiColumnItem;
import com.smartmarmot.dbforbix.scheduler.MultiRowItem;
import com.smartmarmot.dbforbix.scheduler.Scheduler;
import com.smartmarmot.dbforbix.scheduler.SimpleItem;
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
		private PROTOCOL	protocol	= PROTOCOL.V32;
		private String itemsJSON		= null;
		private Map<String,List<String> > hosts=null;
		private Map<String,List<String> > items=null;
		private Map<String,List<String> > hostmacro=null;
		private Map<String,String> itemConfig = new HashMap<String,String>();
		
		
		public String getItemsJSON() {
			return itemsJSON;
		}

		public void setItemsJSON(String itemsJSON) {
			this.itemsJSON = itemsJSON;
		}

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

		public Map<String,String> getItemConfig() {
			return itemConfig;
		}

		public void setItemConfig(Map<String,String> itemConfig) {
			this.itemConfig = itemConfig;
		}

		public Map<String,List<String> > getHosts() {
			return hosts;
		}

		public void setHosts(Map<String,List<String> > hosts) {
			this.hosts = hosts;
		}

		public Map<String,List<String> > getItems() {
			return items;
		}

		public void setItems(Map<String,List<String> > items) {
			this.items = items;
		}

		public Map<String,List<String> > getHostmacro() {
			return hostmacro;
		}

		public void setHostmacro(Map<String,List<String> > hostmacro) {
			this.hostmacro = hostmacro;
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
		private Boolean persistent = false;
		private int		queryTimeout;
		private String itemGroupName=null;
		
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

		public String getItemGroupName() {
			return itemGroupName;
		}

		public void setItemGroupName(String itemGroupName) {
			this.itemGroupName = itemGroupName;
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
	private String					sspDir		 	= "./temp/";
	private static final String		SET_LOGFILESIZE	= GLOBAL_NAME + ".LogFileSize";
	private String					logFileSize		= "1MB";
	
	private static final String		SET_POOL_MAXACTIVE	= GLOBAL_POOL + ".MaxActive";
	private int						maxActive		= 10;
	private static final String		SET_POOL_MAXIDLE	= GLOBAL_POOL + ".TimeOut";

	private static final String 	SET_QUERY_TIMEOUT = GLOBAL_POOL+ ".QueryTimeOut";
	private int			queryTimeout= 60;
	private int						maxIdle			= 15;
	
	private static final String ZBXPRX_REQUEST_CONFIG = "{\"request\":\"proxy config\",\"host\":\"DBFORBIX\"}";
	private static final String ZBX_HEADER_PREFIX="ZBXD\1";
	private static final String SET_ZBX_PROXY_NAME=GLOBAL_NAME + ".ProxyName";
	private String zbxPrxName=null;
	
	private List<Map<String, String> > confList=new ArrayList< Map<String,String> >();
	
	private String zabbixItemConfigSuffix="DBFORBIX.config";
	
	private Map<Integer, Scheduler>	scheduler = new HashMap<Integer, Scheduler>();
	
	public Map<Integer, Scheduler> getScheduler() {
		return scheduler;
	}

	public void setScheduler(Map<Integer, Scheduler> scheduler) {
		this.scheduler = scheduler;
	}

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
		int queryTimeout = 60;
		
		try (FileReader reader = new FileReader(file)){
	     		PropertiesConfiguration pcfg = new PropertiesConfiguration();
		     	pcfg.read(reader);
			if (pcfg.containsKey(SET_LOGLEVEL))
				logLevel = Level.toLevel(pcfg.getString(SET_LOGLEVEL), Level.INFO);
			if (pcfg.containsKey(SET_LOGFILE))
				logFile = pcfg.getString(SET_LOGFILE);
			if (pcfg.containsKey(SET_LOGFILESIZE))
				logFileSize = pcfg.getString(SET_LOGFILESIZE);
			if (pcfg.containsKey(SET_ZBX_PROXY_NAME)) zbxPrxName  = pcfg.getString(SET_ZBX_PROXY_NAME);
			else throw new ConfigurationException(SET_ZBX_PROXY_NAME + " was not set in config file!");
			
			
			
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
		else if ("Persistence".equalsIgnoreCase(key))
			dbsrv.setPersistence(value);
		else
			LOG.info("Invalid config item: " + group + "." + name + "." + key);
		databases.put(name, dbsrv);
	}
	
	
	public byte[] getZabbixProxyRequest(String json){
		//in: json string
		//out: zabbix request byte array
		String str=new String(ZBX_HEADER_PREFIX + "________"+json);
		byte[] data=str.getBytes();		
		
		//size of json request in little-endian format
		byte[] leSize=null;
		leSize=getNumberInLEFormat8B(json.length());
		if(leSize.length != 8) {
			LOG.error("getZabbixProxyRequest():leSize has "+leSize.length +" != 8 bytes!");
			return null;
		}
		
		for(int i=0;i<8;++i) data[i+5]=leSize[i];
		LOG.debug("getZabbixProxyRequest(): data: "+ new String(data));
		return data;
		
	}
	
	
	private byte[] getNumberInLEFormat8B(long l) {
		byte[] leL=new byte[8];
		for(int i=0;i<8;++i){
			leL[i]=(byte) (l & 0xFF);
			l>>=8;			
		}
		return leL;
	}

	
	
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
	
	public String requestZabbix(String host, int port, String json){
		byte[] response = new byte[1024];
		Socket zabbix = null;
		OutputStreamWriter out = null;
		InputStream in = null;			
		byte[] data=null;
		String resp=new String();
		try {
			zabbix = new Socket();
			zabbix.setSoTimeout(5000);
			
			zabbix.connect(new InetSocketAddress(host, port));
			OutputStream os = zabbix.getOutputStream();
			
			data=getZabbixProxyRequest(json);
			
			//send request
			os.write(data);
			os.flush();

			//read response
			in = zabbix.getInputStream();
			
			while(true){
				final int read = in.read(response);
				if(read<=0) break;					
				resp+=new String(response).substring(0, read);
			}
			LOG.debug("requestZabbix(): resp: "+ resp);
			
		}
		catch (Exception ex) {
			LOG.error("requestZabbix(): Error contacting Zabbix server zabdomis02 - " + ex.getMessage());
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

	private void getItemConfigFromZabbix(){
			
		Collection<ZServer> zServers=null;		
		try{
			zServers = getZabbixServers();
		}catch (Exception ex) {
			LOG.error("Error getting Zabbix server config - " + ex.getMessage());
		}
		
		for (ZServer zs: zServers){
			
			String resp=new String();
			resp=requestZabbix(zs.host, zs.port,ZBXPRX_REQUEST_CONFIG);
			
			zs.setItemsJSON(resp);
			
			Map<String,List<String> > hosts=zs.getHosts();
			Map<String,List<String> > items=zs.getItems();
			Map<String,List<String> > hostmacro=zs.getHostmacro();
			
			try{//parse json
				resp=resp.substring(resp.indexOf("{"));
				LOG.debug(resp);
				JSONObject o=JSON.parseObject(resp);
				
				//result for hosts:
				//{ipmi_privilege=[2], tls_psk_identity=[], tls_accept=[1], hostid=[11082], tls_issuer=[], 
				//ipmi_password=[], ipmi_authtype=[-1], ipmi_username=[], host=[APISQL], name=[APISQL], tls_connect=[1], 
				//tls_psk=[], tls_subject=[], status=[0]}
				hosts=zJSONObject2Map(o.getJSONObject("hosts"));
				
				//result for items:
				//{trapper_hosts=[, ], snmpv3_authprotocol=[0, 0], snmpv3_securityname=[, ], flags=[1, 2], password=[, ], interfaceid=[null, null], snmpv3_authpassphrase=[, ], snmpv3_privprotocol=[0, 0],snmp_oid=[, ], delay_flex=[, ], publickey=[, ], ipmi_sensor=[, ], logtimefmt=[, ],  
				//authtype=[0, 0], mtime=[0, 0], snmp_community=[, ], snmpv3_securitylevel=[0, 0], privatekey=[, ], lastlogsize=[0, 0], port=[, ], data_type=[0, 0], snmpv3_privpassphrase=[, ], snmpv3_contextname=[, ], username=[, ]}
				//status=[0, 0], 
				//type=[11, 11], value_type=[4, 3],
				//hostid=[11082, 11082], itemid=[143587, 143588],  
				//key_=[db.odbc.discovery[sessions,{$DSN}], db.odbc.select[sessions,{$DSN}]], 
				//params=[select machine, count(1) N from v$session, sessions], 
				//delay=[120, 120],
				items=zJSONObject2Map(o.getJSONObject("items"));
				
				//{macro=[{$DSN}], hostmacroid=[450], hostid=[11082], value=[TEST_DATABASE]}
				hostmacro=zJSONObject2Map(o.getJSONObject("hostmacro"));
				
			}
			catch (Exception ex){
				System.out.println("Error parsing json objects - " + ex.getMessage());
			}
			try{// substitute macro
				for(int hm=0;hm<hostmacro.get("macro").size();++hm){
					for(int it=0;it<items.get("key_").size();++it){
						if(items.get("hostid").get(it).equals(hostmacro.get("hostid").get(hm))){
							String key=items.get("key_").get(it);
							String params=items.get("params").get(it);
							String macro=hostmacro.get("macro").get(hm);
							String value=hostmacro.get("value").get(hm);
							items.get("key_").set(it, key.replace(macro, value));
							items.get("params").set(it, params.replace(macro, value));
						}
					}
				}
				System.out.println(items.get("key_"));
				System.out.println(items.get("params"));
			}
			catch (Exception ex){
				LOG.error("Error substituting hostmacro - " + ex.getMessage());
			}
			try{
				//result for hosts:
				// hostid=[11082], host=[APISQL], name=[APISQL], status=[0]
				//
				//result for items:
				//status=[0, 0], 
				//type=[11, 11], value_type=[4, 3],
				//hostid=[11082, 11082], itemid=[143587, 143588],  
				//key_=[DBFORBIX.config[mysql.database.discovery], db.odbc.select[sessions,{$DSN}]], 
				//params=[<XML config>, sessions], 
				//delay=[120, 120],
				for(int it=0;it<items.get("key_").size();++it){
					if(items.get("key_").get(it).contains(zabbixItemConfigSuffix)){
						Map<String,String> m = zs.getItemConfig();
						m.put("params", items.get("params").get(it));
						m.put("hostid", items.get("hostid").get(it));
						m.put("key_", items.get("key_").get(it));
					}
				}
			}
			catch (Exception ex){
				LOG.error("Error configuring requests - " + ex.getMessage());
			}
		}		
	}


		
	public void loadItemConfigFromZabbix() {
		
		getItemConfigFromZabbix();
		
		//result for hosts:
		// hostid=[11082], host=[APISQL], name=[APISQL], status=[0]
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
			LOG.error("Error getting Zabbix server config - " + ex.getMessage());
		}
		
		for (ZServer zs: zServers){
			LOG.debug("loadItemConfigFromZabbix:"+zs);
			try {				
				String param=zs.getItemConfig().get("params");
				String hostid=zs.getItemConfig().get("hostid");
				String key=zs.getItemConfig().get("key_");
				param="<!DOCTYPE parms SYSTEM \""+getBasedir()+"\\items\\param.dtd\">"+param;
				Document doc = DocumentHelper.parseText(param);
				Element root = doc.getRootElement();
				String prefix = root.attributeValue("prefix");
				String itemGroupName=new String(zs.toString()+"."+zbxPrxName+"."+hostid+"."+key);
				//fill Dbs config with itemGroupName
				for(Entry<String, Database> db:databases.entrySet()){
					db.getValue().setItemGroupName(itemGroupName);
				}
				for (Object srv: root.elements("server")) {
					if (srv instanceof Element) buildServerElements((Element) srv, itemGroupName, prefix , zs);
				}
//				for (Object db: root.elements("database")) {
//					if (db instanceof Element) buildDatabaseElements((Element) db, itemGroupName, prefix);
//				}
			}
			catch (Exception ex) {
				LOG.error("Error while loading ", ex);
			}
		}
	}
	
	
	private void buildServerElements(Element e, String itemGroupName, String prefix,ZServer zs) {
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
				if (!scheduler.containsKey(time)) {
					LOG.debug("creating item scheduler with time " + time);
					scheduler.put(time, new Scheduler(time));
				}
				Scheduler itemSch = scheduler.get(time);
				switch (itmE.getName()) {
					case "discovery": {
						Discovery item = new Discovery(prefix + itmE.attributeValue("item"), itmE.getTextTrim(), zs);
						String nameList = itmE.attributeValue("names", "");
						String names[] = nameList.split("\\|");
						if (names != null && names.length > 0)
							item.setAltNames(names);
						//if(!item.setZServer(zs)) LOG.debug("buildServerElements: we haven't not actually registered ZServer "+zs.toString()+" for item "+item.getName());
						itemSch.addItem(itemGroupName, item);
					}
					break;
					
					case "query": {
						Item item = new SimpleItem(prefix + itmE.attributeValue("item"), itmE.getTextTrim(),itmE.attributeValue("nodata"), zs);
						itemSch.addItem(itemGroupName, item);
					}
					break;
					
					case "multiquery": {
						String itemList = itmE.attributeValue("items", "");
						String items[] = itemList.split("\\|");
						Item item;
						if (itmE.attributeValue("type", "column").equalsIgnoreCase("column"))
							item = new MultiColumnItem(prefix, items, itmE.getTextTrim(), zs);
						else
							item = new MultiRowItem(prefix, items, itmE.getTextTrim(), zs);
						itemSch.addItem(itemGroupName, item);
					}
					break;
				}
			}
		}
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

	public void addItemConfig(Map<String, String> m) {
		confList.add(m);
	}
	
	public List<Map<String,String>> getItemConfig(){
		return  confList;
	}

}
