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

package com.smartmarmot.dbforbix.config.element;

import java.sql.Connection;
import java.sql.SQLException;

import com.smartmarmot.dbforbix.config.ZabbixServer;
import com.smartmarmot.dbforbix.config.item.IConfigurationItem;
import com.smartmarmot.dbforbix.zabbix.ZabbixItem;

/**
 * Configuration element is part of configuration item.
 * One configuration item can consists of several configuration elements.
 * ConfigurationItem : configurationElement -> 1:N
 */
public interface IConfigurationElement {
	String getElementID();
	String getPrefix();
	ZabbixItem[] getZabbixItemsData(Connection con, int timeout) throws SQLException;	
	ZabbixServer getZabbixServer();
	IConfigurationItem getConfigurationItem();
	void setConfigurationItem(IConfigurationItem configurationItem);
	String getQuery();
	String getNoData();
	int getTime();
	String getConfigurationUID() throws NullPointerException;
}
