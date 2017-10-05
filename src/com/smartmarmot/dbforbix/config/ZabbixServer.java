package com.smartmarmot.dbforbix.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.smartmarmot.dbforbix.config.Config.Validable;
import com.smartmarmot.dbforbix.config.item.IConfigurationItem;
import com.smartmarmot.dbforbix.zabbix.ZabbixSender.PROTOCOL;

/**
 * Zabbix server config entry
 */
public class ZabbixServer implements Validable {
	
	String		zbxServerHost		= null;
	int			zbxServerPort		= 10051;
	private String 		zbxServerNameFC		= null;
	String		proxy		= null;
	private PROTOCOL	protocol	= PROTOCOL.V32;		
	private Collection<String> definedDBNames  = null;
	
	
	/**
	 * reinit 
	 */
	private Map<String,List<String> > hosts=null;
	private Map<String,List<String> > items=null;
	private Map<String,List<String> > hostmacro=null;
	private Map<String,List<String>> hostsTemplates=null;
	private Map<String,IConfigurationItem> configurationItems = new HashMap<>();
	private String hashZabbixConfig		=null;
	String zabbixConfigurationItemSuffix = "DBforBIX.config";
	
	
	
	/////////////////////////////////////////////////////////
	public String getZbxServerNameFC() {
		return zbxServerNameFC;
	}

	public void setZbxServerNameFC(String zbxServerNameFC) {
		this.zbxServerNameFC = zbxServerNameFC;
	}
	
	
	public Map<String, IConfigurationItem> getConfigurationItems() {
		return configurationItems;
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
	

	public void setConfigurationItems(Map<String,IConfigurationItem> configurationItems) {
		this.configurationItems=configurationItems;
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

	public void addConfigurationItem(IConfigurationItem configurationItem) {
		configurationItems.put(configurationItem.getConfigurationUID(), configurationItem);
	}
	
	public void removeConfigurationItem(String configurationUID) {
		configurationItems.remove(configurationUID);
	}

	/**
	 * 
	 * @return set of item group names of this Zabbix Server
	 */
	public Collection<String> getConfigurationUIDs() {			
		return configurationItems.keySet();
	}

	/**
	 * 
	 * @param configurationUID
	 * @return configuration item
	 * @throws NullPointerException
	 */
	public IConfigurationItem getConfigurationItemByConfigurationUID(String configurationUID){
		return configurationItems.get(configurationUID);
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

	public String getConfigurationItemSuffix() {
		return zabbixConfigurationItemSuffix;
	}	
}