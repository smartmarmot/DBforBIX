package com.smartmarmot.dbforbix.config.item;

import java.util.HashSet;
import java.util.Set;

import com.smartmarmot.dbforbix.config.ZabbixServer;
import com.smartmarmot.dbforbix.config.element.IConfigurationElement;

public class ConfigurationItem implements IConfigurationItem {

	private ZabbixServer zabbixServer;
	private String configurationUID, host, hostid, db, key, param, hashParam;
	private Set<IConfigurationElement> configurationElements = new HashSet<>();
	
	public ConfigurationItem(String configurationUID, ZabbixServer zabbixServer, String host, String hostid, String db, String key, String param,
			String hashParam) {
		setConfigurationUID(configurationUID);
		setZabbixServer(zabbixServer);
		setHost(host);
		setHostid(hostid);
		setDb(db);
		setKey(key);
		setParam(param);
		setHashParam(hashParam);
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#getConfigurationUID()
	 */
	@Override
	public String getConfigurationUID() {
		return configurationUID;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#setConfigurationUID(java.lang.String)
	 */
	@Override
	public void setConfigurationUID(String _configurationUID) {
		this.configurationUID = _configurationUID;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#getHost()
	 */
	@Override
	public String getHost() {
		return host;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#setHost(java.lang.String)
	 */
	@Override
	public void setHost(String _host) {
		this.host = _host;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#getHostid()
	 */
	@Override
	public String getHostid() {
		return hostid;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#setHostid(java.lang.String)
	 */
	@Override
	public void setHostid(String _hostid) {
		this.hostid = _hostid;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#getDb()
	 */
	@Override
	public String getDb() {
		return db;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#setDb(java.lang.String)
	 */
	@Override
	public void setDb(String _db) {
		db = _db;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#getKey()
	 */
	@Override
	public String getKey() {
		return key;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#setKey(java.lang.String)
	 */
	@Override
	public void setKey(String _key) {
		key = _key;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#getParam()
	 */
	@Override
	public String getParam() {
		return param;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#setParam(java.lang.String)
	 */
	@Override
	public void setParam(String _param) {
		param = _param;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#getHashParam()
	 */
	@Override
	public String getHashParam() {
		return hashParam;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#setHashParam(java.lang.String)
	 */
	@Override
	public void setHashParam(String _hashParam) {
		hashParam = _hashParam;
	}
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#getZabbixServer()
	 */
	@Override
	public ZabbixServer getZabbixServer() {
		return zabbixServer;
	}
	
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#setZabbixServer(com.smartmarmot.dbforbix.config.Config.ZabbixServer)
	 */
	@Override
	public void setZabbixServer(ZabbixServer _zabbixServer) {
		zabbixServer = _zabbixServer;
	}
	
	
	/**
	 * Make two way links  
	 */
	@Override
	public void addConfigurationElements(Set<IConfigurationElement> _configurationElements) {
		for(IConfigurationElement configurationElement:_configurationElements){
			configurationElement.setConfigurationItem(this);
		}
		configurationElements.addAll(configurationElements);
	}
	
	/* (non-Javadoc)
	 * @see com.smartmarmot.dbforbix.scheduler.IConfigurationItem#getConfigurationElements()
	 */
	@Override
	public Set<IConfigurationElement> getConfigurationElements(){
		return configurationElements;
	}
	
	

}
