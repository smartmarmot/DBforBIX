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

package com.vagabondan.db4bix.db.adapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;

import com.vagabondan.db4bix.db.DBType;


public interface Adapter {

	public DBType getType();
	public String getName();
	public String getURL();
	public String getUser();
	public String  getPassword();
	public Integer getMaxActive();
	public Integer getMaxIdle();
	public Set<String> getItemGroupNames();
	
	public String[] getDiscoveryItems();
	public Object getDiscovery(String key);
	
	public boolean hasServerItems();
	public boolean hasUserItems();
	public boolean hasDatabaseItems();
	public boolean hasSchemaItems();
	public boolean hasTablespaceItems();
	public boolean getPersistence();
	
	public void createConnection() throws SQLException, ClassNotFoundException;
	public Connection getConnection() throws SQLException, ClassNotFoundException;
	public void abort();
	
	
}
