package com.smartmarmot.dbforbix.config;

import java.util.HashSet;
import java.util.Set;

import com.smartmarmot.dbforbix.config.Config.Validable;
import com.smartmarmot.dbforbix.db.DBType;

/**
 * Monitored database config entry
 */
public class Database implements Validable {
	
	DBType	type;
	private String	nameFC;
	String	url;
	String	user;
	String	password;
	String	instance;
	private Integer maxactive = new Integer(15);
	private Integer maxidle = new Integer(2);
	private Integer maxwaitmillis = new Integer (10000);
	private Boolean persistent = false;
	private Integer	queryTimeout = new Integer(15);
	private Set<String>  configurationUIDs=new HashSet<String>();
	
	public DBType getType() {
		return type;
	}
	
	public String getDBNameFC() {
		return nameFC;
	}
	
	public void setDBNameFC(String nameFC) {
		this.nameFC=nameFC;
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
		return "DSN:"+this.getDBNameFC()+",\ttype:"+getType() + ",\tURL:" + getURL() + ",\tInstance:" + getInstance();
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

	public Set<String> getConfigurationUIDs() {
		return configurationUIDs;
	}

	public void addConfigurationUID(String configurationUID) {
		configurationUIDs.add(configurationUID);
	}
	
	public void removeConfigurationUID(String configurationUID) {
		configurationUIDs.remove(configurationUID);
	}

}