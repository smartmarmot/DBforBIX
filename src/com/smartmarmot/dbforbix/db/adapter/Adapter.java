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

import com.smartmarmot.dbforbix.db.DBType;


public interface Adapter {

	public DBType getType();
	public String getName();
	public String getURL();
	public String getUser();
	public String  getPassword();
	public Integer getMaxActive();
	public Integer getMaxIdle();
	public String getItemFile();
	
	public String[] getDiscoveryItems();
	public Object getDiscovery(String key);
	
	public boolean hasServerItems();
	public boolean hasUserItems();
	public boolean hasDatabaseItems();
	public boolean hasSchemaItems();
	public boolean hasTablespaceItems();
	public boolean getPersistence();
	
	public Connection getConnection() throws SQLException, ClassNotFoundException;
	
	
}
