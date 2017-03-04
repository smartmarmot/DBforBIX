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

package com.smartmarmot.dbforbix.scheduler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.smartmarmot.dbforbix.config.Config.ZServer;
import com.smartmarmot.dbforbix.zabbix.ZabbixItem;


public interface Item {

	public String getName();
		
	
	/**
	 * 
	 * @param con - Connection object
	 * @param timeout
	 * @return
	 * @throws SQLException
	 */
	public ZabbixItem[] getItemData(Connection con, int timeout) throws SQLException;

	public boolean setZServer(ZServer zs);
	public ZServer getZServer();
	public Map<String,String> getItemConfig();
}
