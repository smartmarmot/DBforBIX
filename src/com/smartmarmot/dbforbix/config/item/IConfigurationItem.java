package com.smartmarmot.dbforbix.config.item;

import java.util.Set;

import com.smartmarmot.dbforbix.config.ZabbixServer;
import com.smartmarmot.dbforbix.config.element.IConfigurationElement;

public interface IConfigurationItem {

	String getConfigurationUID();
	void setConfigurationUID(String _configurationUID);
	String getHost();
	void setHost(String _host);
	String getHostid();
	void setHostid(String _hostid);
	String getDb();
	void setDb(String _db);
	String getKey();
	void setKey(String _key);
	String getParam();
	void setParam(String _param);
	String getHashParam();
	void setHashParam(String _hashParam);
	ZabbixServer getZabbixServer();
	void setZabbixServer(ZabbixServer _zabbixServer);
	Set<IConfigurationElement> getConfigurationElements();
	void addConfigurationElements(Set<IConfigurationElement> configurationElements);

}