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

package com.smartmarmot.dbforbix.db.adapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.dbcp2.cpdsadapter.DriverAdapterCPDS;
import org.apache.commons.dbcp2.datasources.SharedPoolDataSource;
import org.apache.log4j.Logger;

import com.smartmarmot.dbforbix.config.Config;

abstract class AbstractAdapter implements Adapter {

	private static final Logger		LOG	= Logger.getLogger(AbstractAdapter.class);

	private SharedPoolDataSource	datasrc;
	
	protected String name;
	protected String url;
	protected String user;
	protected String passwd;
	protected int 	 maxactive;
	protected int    maxidle;
	protected int    maxwaitmillis;
	protected boolean persistence;
	protected Set<String> itemGroupName=new HashSet<String>();

	public Set<String> getItemGroupNames() {
		return itemGroupName;
	}

	public void addItemGroupName(Set<String> itemGroupName) {
		this.itemGroupName = itemGroupName;
	}

	@Override
	public Connection getConnection() throws SQLException, ClassNotFoundException {
		if (datasrc == null) {
			LOG.info("Creating new pool for database " + getName());
			Config cfg=Config.getInstance();
			DriverAdapterCPDS cpds = new DriverAdapterCPDS();
			cpds.setDriver(getType().getJDBCDriverClass());
			cpds.setUrl(getURL());
			cpds.setUser(getUser());
			cpds.setPassword(getPassword());
			datasrc = new SharedPoolDataSource();
			datasrc.setConnectionPoolDataSource(cpds);
			datasrc.setLoginTimeout(15);
			
			datasrc.setMaxTotal(cfg.getMaxActive());
			datasrc.setDefaultMaxIdle(cfg.getMaxIdle());
			datasrc.setDefaultMaxWaitMillis(getMaxWaitMillis());

			datasrc.setValidationQuery(getType().getAliveSQL());
		}
		return datasrc.getConnection();
	}
	
	@Override
	public void abort(){
		try {
			datasrc.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		datasrc=null;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getURL() {
		return url;
	}
	
	@Override
	public String getUser() {
		return user;
	}
	
	@Override
	public String getPassword() {
		return passwd;
	}
	public Integer getMaxActive() {
		return maxactive;
	}
	public Integer getMaxIdle() {
		return maxidle;
	}
	public Integer getMaxWaitMillis() {
		return maxwaitmillis;
	}
	public boolean getPersistence() {
		return persistence;
	}

	
	@Override
	public String[] getDiscoveryItems() {
		return new String[0];
	}

	@Override
	public Object getDiscovery(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasServerItems() {
		return false;
	}

	@Override
	public boolean hasUserItems() {
		return false;
	}
	
	public boolean hasPersistence() {
		return this.persistence;
	}
	@Override
	public boolean hasDatabaseItems() {
		return false;
	}

	@Override
	public boolean hasSchemaItems() {
		return false;
	}

	@Override
	public boolean hasTablespaceItems() {
		return false;
	}
}
